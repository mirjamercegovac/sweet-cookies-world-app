package hr.ferit.mirjamercegovac.sweetcookiesworldapp.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.R
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding:ActivityLoginBinding
    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        //init progress dialog - show while creating account Register user
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Molimo pričekajte")
        progressDialog.setCanceledOnTouchOutside(false)
        window.statusBarColor = resources.getColor(R.color.pink02, theme)
        //handle click, not have account - go to register
        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        //handle click, begin login
        binding.loginBtn.setOnClickListener {
            //steps like register
            validateData()
        }

        binding.forgotTv.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private var email = ""
    private var password = ""

    private fun validateData() {
        email = binding.emailEt.text.toString()
        password = binding.passwordEt.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Pogrešan format email-a...", Toast.LENGTH_SHORT).show()
        }
        else if(password.isEmpty()){
            Toast.makeText(this, "Unesi lozinku...", Toast.LENGTH_SHORT).show()
        }
        else{
            loginUser()
        }
    }

    private fun loginUser() {
        progressDialog.setMessage("Logiranje...")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                checkUser()
                /*progressDialog.dismiss()
                Toast.makeText(this, "Uspješno ste se ulogirali", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                finish()*/
            }
            .addOnFailureListener { e->
                //failed creating account
                progressDialog.dismiss()
                Toast.makeText(this, "Neuspješno logiranje ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        progressDialog.setMessage("Provjera korisnika...")

        val firebaseUser = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app").getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                progressDialog.dismiss()

                val userType = snapshot.child("userType").value
                if (userType == "user"){
                    startActivity(Intent(this@LoginActivity, ProfileUserActivity::class.java))
                    finish()
                }else if(userType == "admin"){
                    startActivity(Intent(this@LoginActivity, ProfileAdminActivity::class.java))
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

}