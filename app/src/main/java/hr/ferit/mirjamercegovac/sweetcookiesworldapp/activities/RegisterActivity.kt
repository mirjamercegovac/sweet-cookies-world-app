package hr.ferit.mirjamercegovac.sweetcookiesworldapp.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.R
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.activities.ProfileUserActivity
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.databinding.ActivityRegisterBinding
import java.util.*


class RegisterActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityRegisterBinding
    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    //progress dialog
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        //init progress dialog - show while creating account Register user
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Molimo pričekajte")
        progressDialog.setCanceledOnTouchOutside(false)
        window.statusBarColor = resources.getColor(R.color.pink02, theme)
        //handle back button click
        binding.backBtn.setOnClickListener{
            onBackPressed() //back on previous screen
        }
        //handle click, begin register
        binding.registerBtn.setOnClickListener {
            /*Steps
            * 1) Input Data
            * 2) Validate Data
            * 3) Create Account - Firebase Auth
            * 4) Save User Info - Firebase Realtime Database
             */
            validateDate()
        }
    }


    private var name = ""
    private var email = ""
    private var password = ""


    private fun validateDate() {
        //1) Input Data
        name = binding.nameEt.text.toString()
        email = binding.emailEt.text.toString()
        password = binding.passwordEt.text.toString()
        val cPaassword = binding.cPasswordEt.text.toString()

        //2) Validate Data
        if (name.isEmpty()){
            Toast.makeText(this, "Unesite svoje ime...", Toast.LENGTH_SHORT).show()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Pogrešan email...", Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()){
            Toast.makeText(this, "Unesite lozinku...", Toast.LENGTH_SHORT).show()
        }
        else if (cPaassword.isEmpty()){
            Toast.makeText(this, "Ponovi lozinku...", Toast.LENGTH_SHORT).show()
        }
        else if (password != cPaassword){
            Toast.makeText(this, "Lozinka se ne poklapa", Toast.LENGTH_SHORT).show()
        }
        else{
            createUserAccount()
        }
    }

    private fun createUserAccount() {
        //3) Create Account - Firebase Auth

        //show progress
        progressDialog.setMessage("Stvaranje računa...")
        progressDialog.show()

        //create user in firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //account created
                /*progressDialog.dismiss()
                Toast.makeText(this, "Račun stvoren", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                finish()*/
                updateUserInfo()
            }
            .addOnFailureListener { e->
                //failed creating account
                progressDialog.dismiss()
                Toast.makeText(this, "Neuspješno stvaranje računa ${e.message}", Toast.LENGTH_SHORT).show()
        }

    }

    private fun updateUserInfo() {
        //4) Save User Info - Firebase Realtime Database
        progressDialog.setMessage("Spremanje informacija...")      // NE RADI....kasnije popravit

        //timestamp
        val timestamp = System.currentTimeMillis()

        //get current user uid, since user is registered so we can get it now
        val uid = firebaseAuth.uid
        //setup data to add in db
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"]=uid
        hashMap["email"]=email
        hashMap["name"]=name
        hashMap["profileImage"]=""
        hashMap["userType"] = "user"
        hashMap["timestamp"]=timestamp



        //set data to db
        val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        ref.child(uid!!).setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Račun stvoren", Toast.LENGTH_SHORT).show()
                //startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                startActivity(Intent(this@RegisterActivity, ProfileUserActivity::class.java))
                finish()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Neuspješno spremanje informacija ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }
}



