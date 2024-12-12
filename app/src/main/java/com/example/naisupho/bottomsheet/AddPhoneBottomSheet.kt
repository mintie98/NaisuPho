package com.example.naisupho.bottomsheet

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.naisupho.R
import com.example.naisupho.BaseBottomSheetFragment
import com.example.naisupho.databinding.ActivityAddPhoneBottomSheetBinding
import com.example.naisupho.databinding.OtpPopUpBinding
import com.example.naisupho.viewmodel.PayOutViewModel
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
        // dùng để gỡ số điện thoại đã liên kết
//        val user = FirebaseAuth.getInstance().currentUser
//        user?.providerData?.forEach { userInfo ->
//            if (userInfo.providerId == "phone" && user.phoneNumber != null) {
//                // Gỡ liên kết số điện thoại nếu đã tồn tại
//                user.unlink("phone")
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            Log.d("FirebaseAuth", "Phone number unlinked successfully.")
//                            Toast.makeText(this, "Phone number unlinked", Toast.LENGTH_SHORT).show()
//                        } else {
//                            Log.e("FirebaseAuth", "Failed to unlink phone number", task.exception)
//                            Toast.makeText(this, "Failed to unlink phone number", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//            }
//        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.sending_verification_code))
            setCancelable(false)
        }

        binding.sendButton.setOnClickListener {
            val countryCode = binding.countryCodeSpinner.selectedItem.toString()
            var phoneNumber = binding.phoneNumberEditText.text.toString().trim()

            if (phoneNumber.startsWith("0")) {
                phoneNumber = phoneNumber.substring(1)
            }

            val fullPhoneNumber = "$countryCode$phoneNumber"
            sendVerificationCode(fullPhoneNumber)
        }

        binding.closeIcon.setOnClickListener { dismiss() }
    }

    private fun sendVerificationCode(phoneNumber: String) {
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
                    progressDialog.dismiss()
                    //Toast.makeText(context, getString(R.string.failed_to_send_code, e.message), Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    this@AddPhoneBottomSheet.verificationId = verificationId
                    progressDialog.dismiss()
                    showOtpPopup()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun showOtpPopup() {
        val dialog = Dialog(requireContext())
        val otpBinding = OtpPopUpBinding.inflate(LayoutInflater.from(requireContext()))
        dialog.setContentView(otpBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        otpBinding.btnVerify.setOnClickListener {
            val code = otpBinding.etOtpCode.text.toString().trim()
            if (code.isNotEmpty()) {
                verifyCode(code)
                dialog.dismiss()
            } else {
                //Toast.makeText(requireContext(), "Please Enter OPT Code", Toast.LENGTH_SHORT).show()
            }
        }

        otpBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun verifyCode(code: String) {
        verificationId?.let {
            val credential = PhoneAuthProvider.getCredential(it, code)
            signInWithCredential(credential)
        } ?: Toast.makeText(context, "", Toast.LENGTH_SHORT).show()
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        progressDialog.setMessage(getString(R.string.verifying_code))
        progressDialog.show()

        val currentUser = auth.currentUser
        currentUser?.let { user ->
            if (user.phoneNumber.isNullOrEmpty()) {
                // Nếu chưa có số điện thoại, tiến hành liên kết
                user.linkWithCredential(credential)
                    .addOnCompleteListener { task ->
                        progressDialog.dismiss()
                        if (task.isSuccessful) {
                            val phoneNumber = task.result?.user?.phoneNumber
                            if (phoneNumber != null) {
                                savePhoneNumberToRTDB(phoneNumber)
                            }
                        } else {
                            // Kiểm tra nếu số điện thoại đã được sử dụng
                            if (task.exception is FirebaseAuthUserCollisionException) {
                                Toast.makeText(
                                    context,
                                    getString(R.string.phone_already_in_use),
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                handleFirebaseError(task.exception)
                            }
                        }
                    }
            } else {
                // Nếu đã có số điện thoại, tiến hành cập nhật
                user.updatePhoneNumber(credential)
                    .addOnCompleteListener { task ->
                        progressDialog.dismiss()
                        if (task.isSuccessful) {
                            val phoneNumber = user.phoneNumber
                            if (phoneNumber != null) {
                                savePhoneNumberToRTDB(phoneNumber)
                            }
                        } else {
                            handleFirebaseError(task.exception)
                        }
                    }
            }
        } ?: run {
            progressDialog.dismiss()
            Log.e("AddPhoneBottomSheet", "No user logged in.")
        }
    }

    private fun savePhoneNumberToRTDB(phoneNumber: String) {
        viewModel.updatePhone(phoneNumber)
        viewModel.updatePhoneResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                //Toast.makeText(context, getString(R.string.phone_saved_success), Toast.LENGTH_SHORT).show()
                dismiss()
            }.onFailure {
                //Toast.makeText(context, getString(R.string.failed_to_save_phone, it.message), Toast.LENGTH_SHORT).show()
                unlinkPhoneNumber()
            }
        }
    }

    private fun unlinkPhoneNumber() {
        auth.currentUser?.delete()?.addOnSuccessListener {
            Log.d("AddPhoneBottomSheet", "Phone number unlinked successfully.")
        }?.addOnFailureListener {
            Log.e("AddPhoneBottomSheet", "Failed to unlink phone number: ${it.message}")
        }
    }

    private fun handleFirebaseError(exception: Exception?) {
        if (exception is FirebaseAuthUserCollisionException) {
            //Toast.makeText(context, getString(R.string.phone_already_in_use), Toast.LENGTH_LONG).show()
        } else {
            //Toast.makeText(context, getString(R.string.failed_to_link_phone, exception?.message), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}