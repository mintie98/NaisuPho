package com.example.naisupho

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.naisupho.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase



class LoginActivity : AppCompatActivity() {
    private val binding : ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private lateinit var email : String
    private lateinit var password : String
    private lateinit var auth : FirebaseAuth
    private lateinit var  mGoogleSignInClient: GoogleSignInClient
    private val Req_Code : Int = 333
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val passwordToggle: ImageView = findViewById(R.id.password_toggle)
        val edtPass : EditText = findViewById(R.id.edtPassword)
        var isPasswordVisible = false
        passwordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            val inputType = if (isPasswordVisible){
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }else{
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            edtPass.inputType = inputType
            edtPass.setSelection(edtPass.text.length)
            val drawableId = if (isPasswordVisible) R.drawable.eye else R.drawable.eye_hide
            passwordToggle.setImageResource(drawableId)
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        FirebaseApp.initializeApp(this)
        binding.loginButton.setOnClickListener{
            email = binding.edtEmail.toString()
            password = binding.edtPassword.toString()
            if (email.isBlank() || password.isBlank()){
                Toast.makeText(this@LoginActivity,"Please Enter Email or Password",Toast.LENGTH_SHORT).show()
            }else{
                loginUser()
            }
        }
        binding.signupBtn.setOnClickListener {
            val intent = Intent(this,SignUpActivity::class.java)
            startActivity(intent)
        }
        binding.googleButton.setOnClickListener {
            signInGoogle()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso)

    }
    private fun loginUser(){
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener(this) {task ->
                if (task.isSuccessful){
                    val intent = Intent(this@LoginActivity,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    Log.w(ContentValues.TAG,"Sign with email failed",task.exception)
                    Toast.makeText(baseContext,"Authentication failed. Please try again",Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signInGoogle(){
        val signInIntent : Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent,Req_Code)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Req_Code){
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult(task)
        }
    }

    private fun handleResult(task: Task<GoogleSignInAccount>){
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            if (account!= null){
                UpdateUI(account)
            }
        } catch (e:ApiException){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
            Log.d(TAG, "handleResult: login")
        }
    }

    private fun UpdateUI(account: GoogleSignInAccount){
        val credential = GoogleAuthProvider.getCredential(account.idToken,null)
        auth.signInWithCredential(credential).addOnCompleteListener{task ->
            if (task.isSuccessful){
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    // Check if user data already exists in Firebase Database
                    database.child("Users").child(currentUser.uid).get().addOnSuccessListener {
                        if (!it.exists()) {
                            // If user data does not exist, save the new user data
                            database.child("Users").child(currentUser.uid).child("photoUrl")
                                .setValue(account.photoUrl.toString())
                            database.child("Users").child(currentUser.uid).child("name")
                                .setValue(account.displayName)
                        }
                    }
                }
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }


    override fun onStart() {
        super.onStart()
        if(auth.currentUser != null)
        {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}