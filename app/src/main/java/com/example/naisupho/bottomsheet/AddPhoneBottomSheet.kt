package com.example.naisupho.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import com.example.naisupho.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddPhoneBottomSheet : BottomSheetDialogFragment() {

    private var listener: AddPhoneListener? = null

    interface AddPhoneListener {
        fun onSend(countryCode: String, phoneNumber: String)
    }

    fun setListener(listener: AddPhoneListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_add_phone_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val closeButton = view.findViewById<ImageView>(R.id.close_icon)
        val countryCodeSpinner = view.findViewById<Spinner>(R.id.country_code_spinner)
        val phoneNumberEditText = view.findViewById<EditText>(R.id.phone_number_edit_text)
        val sendButton = view.findViewById<Button>(R.id.send_button)

        sendButton.setOnClickListener {
            val countryCode = countryCodeSpinner.selectedItem.toString()
            val phoneNumber = phoneNumberEditText.text.toString()
            listener?.onSend(countryCode, phoneNumber)
            dismiss()
        }

        closeButton.setOnClickListener {
            dismiss()
        }
    }
}
