package com.example.naisupho

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
import com.example.wavesoffood.SharedPreference.ProfileSavedPreferences
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

    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)

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
            if (account!= null){
                UpdateUI(account)
            }
        } catch (e: ApiException){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }

    }

    private fun UpdateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                saveDetailsToSharedPreference(account.displayName.toString(),account.email.toString())
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun saveDetailsToSharedPreference(name : String , email : String)
    {
        ProfileSavedPreferences.setName(this, name)
        ProfileSavedPreferences.setEmail(this, email)
    }

    private fun signUpUser() {
        val email = edtEmail.text.toString()
        val pass = edtPass.text.toString()
        val rePass = edtRePass.text.toString()
        val name = edtName.text.toString()

        Log.d("min", "Email: $email, Password: $pass, Name: $name")

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
                Toast.makeText(this, "Successfully Signed Up", Toast.LENGTH_SHORT).show()
                saveDetailsToSharedPreference(name,email)
                startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
            } else {
                val errorMessage = task.exception?.message ?: "Unknown error occurred"
                Toast.makeText(this, "Sign Up Failed: $errorMessage", Toast.LENGTH_SHORT).show()
                Log.e("min", "Sign Up Failed: $errorMessage")
            }
        }
    }

}