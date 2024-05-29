package com.example.naisupho

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.naisupho.databinding.ActivitySignUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class SignUpActivity : AppCompatActivity() {
    private val binding : ActivitySignUpBinding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }
    lateinit var edtEmail: EditText
    private lateinit var edtPass: EditText
    private lateinit var edtRePass: EditText
    private lateinit var edtName: EditText
    private lateinit var btnSignUp: Button
    //private lateinit var ggSignUpBtn : Button
    lateinit var mGoogleSignInClient: GoogleSignInClient
    val Req_Code: Int = 123
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)
        database = FirebaseDatabase.getInstance().reference

        btnSignUp = binding.createButton
        edtEmail = binding.edtEmail
        edtPass  = binding.edtPassword
        edtRePass  = binding.edtRePassword
        edtName = binding.edtName
        // Initialising auth object
        auth = Firebase.auth
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        handleBtnClick()
    }

    private fun handleBtnClick() {
        btnSignUp.setOnClickListener {
            signUpUser()
        }
        binding.signin.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }

        binding.prvBtn.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }
        //password toggle visible

        var isPasswordVisible = false
        binding.passwordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            val inputType = if (isPasswordVisible){
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }else{
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            edtPass.inputType = inputType
            edtRePass.inputType = inputType
            edtPass.setSelection(edtPass.text.length)
            edtRePass.setSelection(edtRePass.text.length)
            val drawableId = if (isPasswordVisible) R.drawable.eye else R.drawable.eye_hide
            binding.passwordToggle.setImageResource(drawableId)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Req_Code) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }

    private fun handleResult(task: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            if (account != null) {
                val name = account.displayName
                val email = account.email
                val photoUrl = account.photoUrl?.toString()
                firebaseAuthWithGoogle(account.idToken!!, name, email, photoUrl)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, name: String?, email: String?, photoUrl: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        database.child("Users").child(userId).child("name").setValue(name)
                        database.child("Users").child(userId).child("email").setValue(email)
                        if (photoUrl != null) {
                            database.child("Users").child(userId).child("photoUrl").setValue(photoUrl)
                        }                    }
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun UpdateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }


    private fun signUpUser() {
        val email = edtEmail.text.toString()
        val pass = edtPass.text.toString()
        val rePass = edtRePass.text.toString()
        val name = edtName.text.toString()

        // check if fields are blank
        if (email.isBlank() || pass.isBlank() || name.isBlank()) {
            Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
            return
        }

        if (pass != rePass) {
            Toast.makeText(this, "Password and Re-entered Password do not match", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    database.child("Users").child(userId).child("name").setValue(name)
                }
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "createUserWithEmail:failure", task.exception)
                Toast.makeText(baseContext, "Authentication failed.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

}