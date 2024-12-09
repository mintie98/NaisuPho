package com.example.naisupho.bottomsheet

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.naisupho.BaseBottomSheetFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.naisupho.databinding.ActivityPhotoOptionsBottomSheetBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PhotoOptionsBottomSheet : BaseBottomSheetFragment() {

    private var listener: PhotoOptionsListener? = null
    private lateinit var binding: ActivityPhotoOptionsBottomSheetBinding

    private val CAMERA_PERMISSION_REQUEST_CODE = 100

    interface PhotoOptionsListener {
        fun onImageSelected(imageUri: Uri)
    }

    private val pickImageResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                if (imageUri != null) {
                    listener?.onImageSelected(imageUri)
                }
            }
            dismiss()
        }
    private fun saveImageToStorage(bitmap: Bitmap): Uri? {
        val imagesFolder = File(requireContext().cacheDir, "images")
        var uri: Uri? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "shared_image.png")

            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()
            uri = FileProvider.getUriForFile(requireContext(), "com.example.naisupho.fileprovider", file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return uri
    }

    private val takePhotoResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val photoBitmap = result.data?.extras?.get("data") as Bitmap
                val imageUri = saveImageToStorage(photoBitmap)
                if (imageUri != null) {
                    listener?.onImageSelected(imageUri)
                }
            }
            dismiss()
        }

    fun setListener(listener: PhotoOptionsListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityPhotoOptionsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectExistingPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageResultLauncher.launch(intent)
        }

        binding.takeNewPhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, request for permission
                requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            } else {
                // Permission has already been granted
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                takePhotoResultLauncher.launch(intent)
            }
        }

        binding.cancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission was granted, start the camera intent
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    takePhotoResultLauncher.launch(intent)
                } else {
                    // Permission denied, show an appropriate message to the user
                    Toast.makeText(requireContext(), "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
                }
                return
            }
            // Add other 'when' lines to check for other permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }
}