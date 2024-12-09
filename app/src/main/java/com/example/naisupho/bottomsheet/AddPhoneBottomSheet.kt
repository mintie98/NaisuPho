package com.example.naisupho.bottomsheet

import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.example.naisupho.BaseBottomSheetFragment
import com.example.naisupho.databinding.ActivityAddPhoneBottomSheetBinding
import com.example.naisupho.databinding.LayoutCustomDialogBinding
import com.example.naisupho.viewmodel.PayOutViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class AddPhoneBottomSheet(private val viewModel: PayOutViewModel) : BaseBottomSheetFragment() {

    private var _binding: ActivityAddPhoneBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    private var verificationId: String? = null
    private lateinit var progressDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityAddPhoneBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Sending verification code...")
            setCancelable(false)
        }

        binding.sendButton.setOnClickListener {
            val countryCode = binding.countryCodeSpinner.selectedItem.toString()
            var phoneNumber = binding.phoneNumberEditText.text.toString().trim()

            if (phoneNumber.startsWith("0")) {
                phoneNumber = phoneNumber.substring(1) // Xóa số 0 đầu
            }

            val fullPhoneNumber = "$countryCode$phoneNumber"
            sendVerificationCode(fullPhoneNumber)
        }

        binding.closeIcon.setOnClickListener { dismiss() }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        Log.d("AddPhoneBottomSheet", "Sending verification code to: $phoneNumber")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    progressDialog.dismiss()
                    signInWithCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e("AddPhoneBottomSheet", "Verification failed: ${e.message}")
                    progressDialog.dismiss()
                    if (isAdded) {
                        Toast.makeText(context, "Failed to send code: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Log.d("AddPhoneBottomSheet", "Code sent successfully. Verification ID: $verificationId")
                    this@AddPhoneBottomSheet.verificationId = verificationId
                    progressDialog.dismiss()
                    if (isAdded) {
                        dismiss() // Đóng BottomSheet
                        showOtpPopup() // Hiển thị popup nhập OTP
                    }
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun showOtpPopup() {
        // Tạo dialog
        val dialog = Dialog(requireContext())

        // Sử dụng View Binding
        val binding = LayoutCustomDialogBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Tạo EditText cho OTP nhập vào
        val otpEditText = EditText(requireContext()).apply {
            hint = "Enter OTP"
            textSize = 16f
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        // Cập nhật nội dung tin nhắn
        binding.tvMessage.text = "Enter the OTP sent to your phone"

        // Thêm EditText vào dưới `tvMessage`
        val parentLayout = binding.tvMessage.parent as LinearLayout
        parentLayout.addView(otpEditText, 1)

        // Xử lý khi bấm Verify
        binding.btnYes.text = "Verify"
        binding.btnYes.setOnClickListener {
            val code = otpEditText.text.toString().trim()
            if (code.isNotEmpty()) {
                verifyCode(code) // Xác minh OTP
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Please enter the OTP", Toast.LENGTH_SHORT).show()
            }
        }

        // Xử lý khi bấm Cancel
        binding.btnCancel.text = "Cancel"
        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Hiển thị dialog
        dialog.show()
    }

    private fun verifyCode(code: String) {
        verificationId?.let {
            val credential = PhoneAuthProvider.getCredential(it, code)
            signInWithCredential(credential)
        } ?: run {
            if (isAdded) {
                Toast.makeText(context, "Error: Verification ID is null", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        progressDialog.setMessage("Verifying code...")
        progressDialog.show()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            if (currentUser.phoneNumber.isNullOrEmpty()) {
                // Nếu tài khoản chưa có số điện thoại -> liên kết lần đầu
                currentUser.linkWithCredential(credential)
                    .addOnCompleteListener { task ->
                        progressDialog.dismiss()
                        if (task.isSuccessful) {
                            val phoneNumber = currentUser.phoneNumber ?: "Unknown"
                            Log.d("AddPhoneBottomSheet", "Phone updated successfully: $phoneNumber")
                            viewModel.updatePhone(phoneNumber)
                            dismiss()
                        } else {
                            handleLinkingError(task.exception)
                        }
                    }
            } else {
                // Nếu tài khoản đã có số điện thoại -> cập nhật số mới
                currentUser.updatePhoneNumber(credential)
                    .addOnCompleteListener { task ->
                        progressDialog.dismiss()
                        if (task.isSuccessful) {
                            val phoneNumber = currentUser.phoneNumber ?: "Unknown"
                            Log.d("AddPhoneBottomSheet", "Phone updated successfully: $phoneNumber")
                            viewModel.updatePhone(phoneNumber)
                            dismiss()
                        } else {
                            handleUpdateError(task.exception)
                        }
                    }
            }
        } else {
            progressDialog.dismiss()
            Log.e("AddPhoneBottomSheet", "No user logged in.")
            Toast.makeText(requireContext(), "Error: No user logged in", Toast.LENGTH_SHORT).show()
        }
    }
    private fun handleLinkingError(exception: Exception?) {
        if (exception is FirebaseAuthUserCollisionException) {
            Log.e("AddPhoneBottomSheet", "Phone number already in use.")
            Toast.makeText(requireContext(), "This phone number is already linked to another account.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), "Failed to link phone number: ${exception?.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleUpdateError(exception: Exception?) {
        Log.e("AddPhoneBottomSheet", "Failed to update phone number: ${exception?.message}")
        Toast.makeText(requireContext(), "Failed to update phone number: ${exception?.message}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}