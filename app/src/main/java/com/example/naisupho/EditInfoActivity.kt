package com.example.naisupho

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.naisupho.bottomsheet.AddPhoneBottomSheet
import com.example.naisupho.bottomsheet.EditNameBottomSheet
import com.example.naisupho.bottomsheet.PhotoOptionsBottomSheet
import com.example.naisupho.bottomsheet.SelectGenderBottomSheet
import com.example.naisupho.databinding.ActivityEditInfoBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

class EditInfoActivity : AppCompatActivity(), PhotoOptionsBottomSheet.PhotoOptionsListener {
    private lateinit var binding: ActivityEditInfoBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        val currentUser = auth.currentUser
        val userEmail = currentUser?.email.toString()
        if (currentUser != null) {
            binding.email.text = userEmail

            database.child("Users").child(currentUser.uid).child("photoUrl")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val photoUrl = dataSnapshot.getValue(String::class.java)
                        if (photoUrl != null && !isFinishing && !isDestroyed) {
                            // Use the photo URL from Firebase
                            Glide.with(this@EditInfoActivity)
                                .load(photoUrl)
                                .into(binding.profilePhoto)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle error
                    }
                })
            database.child("Users").child(currentUser.uid).child("name")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val userName = dataSnapshot.getValue(String::class.java)
                        binding.name.text = userName
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle error
                    }
                })
            database.child("Users").child(currentUser.uid).child("phoneNumber")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val phoneNumber = dataSnapshot.getValue(String::class.java)
                        if (phoneNumber != null) {
                            // If the phone number exists in the database, display it
                            binding.phoneNumber.text = phoneNumber
                        } else {
                            // If the phone number does not exist in the database, display default text
                            binding.phoneNumber.text = "Add Phone Number"
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle error
                    }
                })
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }
        //profile picture
        binding.arrowRightPicture.setOnClickListener {
            val bottomSheet = PhotoOptionsBottomSheet()
            bottomSheet.setListener(this)
            bottomSheet.show(supportFragmentManager, "PhotoOptionsBottomSheet")
        }
        //name
        binding.arrowRightName.setOnClickListener {
            val bottomSheet = EditNameBottomSheet()
            bottomSheet.setListener(object : EditNameBottomSheet.EditNameListener {
                override fun onSave(name: String) {
                    binding.name.text = name
                }
            })
            bottomSheet.show(supportFragmentManager, "EditNameBottomSheet")
        }
        //gender
        binding.arrowRightGender.setOnClickListener {
            val bottomSheet = SelectGenderBottomSheet()
            bottomSheet.setListener(object : SelectGenderBottomSheet.SelectGenderListener {
                override fun onGenderSelected(gender: String) {
                    binding.gender.text = gender
                }
            })
            bottomSheet.show(supportFragmentManager, "SelectGenderBottomSheet")
        }
        //phone number
        binding.arrowRightPhoneNumber.setOnClickListener {
            val bottomSheet = AddPhoneBottomSheet()
            bottomSheet.setListener(object : AddPhoneBottomSheet.AddPhoneListener {
                override fun onSend(countryCode: String, phoneNumber: String) {
                    binding.phoneNumber.text = "$countryCode $phoneNumber"
                }
            })
            bottomSheet.show(supportFragmentManager, "AddPhoneBottomSheet")
        }

        //save button
        binding.updateBtn.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // Get the values from the fields
                val name = binding.name.text.toString()
                val profile = binding.profile.text.toString()
                val gender = binding.gender.text.toString()
                val email = binding.email.text.toString()
                val phoneNumber = binding.phoneNumber.text.toString()

                // Update the values in Firebase Database
                val userRef = database.child("Users").child(currentUser.uid)
                userRef.child("name").setValue(name)
                userRef.child("profile").setValue(profile)
                userRef.child("gender").setValue(gender)
                userRef.child("email").setValue(email)
                userRef.child("phoneNumber").setValue(phoneNumber)
            }
            finish()
        }
    }

    override fun onImageSelected(imageUri: Uri) {
        // Handle image selection
        Glide.with(this@EditInfoActivity).load(imageUri).into(binding.profilePhoto)

        // Upload the new photo to Firebase Storage
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val storageRef = FirebaseStorage.getInstance().reference
            val photoRef = storageRef.child("Users/${currentUser.uid}/photo.jpg")

            val uploadTask = photoRef.putFile(imageUri)
            uploadTask.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                photoRef.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    // Save the new photo URL to Firebase Database
                    database.child("Users").child(currentUser.uid).child("photoUrl")
                        .setValue(downloadUri.toString())
                } else {
                    // Handle failure
                    // ...
                }
            }
        }
    }
}