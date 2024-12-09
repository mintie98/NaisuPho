package com.example.naisupho.userInfo

import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.naisupho.BaseActivity
import com.example.naisupho.bottomsheet.AddPhoneBottomSheet
import com.example.naisupho.bottomsheet.EditNameBottomSheet
import com.example.naisupho.bottomsheet.PhotoOptionsBottomSheet
import com.example.naisupho.bottomsheet.SelectGenderBottomSheet
import com.example.naisupho.databinding.ActivityEditInfoBinding
import com.example.naisupho.viewmodel.PayOutViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditInfoActivity : BaseActivity(), PhotoOptionsBottomSheet.PhotoOptionsListener {
    private lateinit var binding: ActivityEditInfoBinding
    private val viewModel: PayOutViewModel by viewModels()

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var database: DatabaseReference

    @Inject
    lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userEmail = currentUser.email.toString()
            binding.email.text = userEmail

            database.child("Users").child(currentUser.uid).child("photoUrl")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val photoUrl = dataSnapshot.getValue(String::class.java)
                        if (photoUrl != null && !isFinishing && !isDestroyed) {
                            Glide.with(this@EditInfoActivity)
                                .load(photoUrl)
                                .into(binding.profilePhoto)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })

            database.child("Users").child(currentUser.uid).child("name")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val userName = dataSnapshot.getValue(String::class.java)
                        binding.name.text = userName
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })

            database.child("Users").child(currentUser.uid).child("phoneNumber")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val phoneNumber = dataSnapshot.getValue(String::class.java)
                        binding.phoneNumber.text =
                            phoneNumber ?: "Add Phone Number"
                    }

                    override fun onCancelled(databaseError: DatabaseError) {}
                })
        }

        binding.backButton.setOnClickListener { onBackPressed() }

        // Profile picture
        binding.arrowRightPicture.setOnClickListener {
            val bottomSheet = PhotoOptionsBottomSheet()
            bottomSheet.setListener(this)
            bottomSheet.show(supportFragmentManager, "PhotoOptionsBottomSheet")
        }

        // Name
        binding.arrowRightName.setOnClickListener {
            val bottomSheet = EditNameBottomSheet()
            bottomSheet.setListener(object : EditNameBottomSheet.EditNameListener {
                override fun onSave(name: String) {
                    binding.name.text = name
                }
            })
            bottomSheet.show(supportFragmentManager, "EditNameBottomSheet")
        }

        // Gender
        binding.arrowRightGender.setOnClickListener {
            val bottomSheet = SelectGenderBottomSheet()
            bottomSheet.setListener(object : SelectGenderBottomSheet.SelectGenderListener {
                override fun onGenderSelected(gender: String) {
                    binding.gender.text = gender
                }
            })
            bottomSheet.show(supportFragmentManager, "SelectGenderBottomSheet")
        }

        // Phone number
        binding.arrowRightPhoneNumber.setOnClickListener {
            val bottomSheet = AddPhoneBottomSheet(viewModel)
            bottomSheet.show(supportFragmentManager, "AddPhoneBottomSheet")
        }

        // Save button
        binding.updateBtn.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userRef = database.child("Users").child(currentUser.uid)
                userRef.child("name").setValue(binding.name.text.toString())
                userRef.child("profile").setValue(binding.profile.text.toString())
                userRef.child("gender").setValue(binding.gender.text.toString())
                userRef.child("email").setValue(binding.email.text.toString())
                userRef.child("phoneNumber").setValue(binding.phoneNumber.text.toString())
            }
            finish()
        }
    }

    override fun onImageSelected(imageUri: Uri) {
        Glide.with(this@EditInfoActivity).load(imageUri).into(binding.profilePhoto)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val photoRef = storage.reference.child("Users/${currentUser.uid}/photo.jpg")

            val uploadTask = photoRef.putFile(imageUri)
            uploadTask.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                photoRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    database.child("Users").child(currentUser.uid).child("photoUrl")
                        .setValue(downloadUri.toString())
                }
            }
        }
    }
}