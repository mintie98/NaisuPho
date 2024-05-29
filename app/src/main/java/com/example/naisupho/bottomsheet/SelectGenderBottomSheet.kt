package com.example.naisupho.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.naisupho.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SelectGenderBottomSheet : BottomSheetDialogFragment() {

    private var listener: SelectGenderListener? = null

    interface SelectGenderListener {
        fun onGenderSelected(gender: String)
    }

    fun setListener(listener: SelectGenderListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_select_gender_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.gender_female).setOnClickListener {
            listener?.onGenderSelected("Female")
            dismiss()
        }

        view.findViewById<TextView>(R.id.gender_male).setOnClickListener {
            listener?.onGenderSelected("Male")
            dismiss()
        }

        view.findViewById<TextView>(R.id.gender_not_say).setOnClickListener {
            listener?.onGenderSelected("Other")
            dismiss()
        }

        view.findViewById<TextView>(R.id.cancel).setOnClickListener {
            dismiss()
        }
    }
}
