package com.example.naisupho.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.example.naisupho.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EditNameBottomSheet : BottomSheetDialogFragment() {

    private var listener: EditNameListener? = null

    interface EditNameListener {
        fun onSave(name: String)
    }

    fun setListener(listener: EditNameListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_edit_name_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editName = view.findViewById<EditText>(R.id.edit_name)
        val saveButton = view.findViewById<Button>(R.id.save_button)
        val closeButton = view.findViewById<ImageView>(R.id.close_icon)

        saveButton.setOnClickListener {
            val name = editName.text.toString()
            listener?.onSave(name)
            dismiss()
        }

        closeButton.setOnClickListener {
            dismiss()
        }
    }
}