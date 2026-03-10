package hr.ferit.mirjamercegovac.sweetcookiesworldapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.R
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.adapters.AdapterCategory
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.databinding.ActivityProfileAdminBinding
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.models.ModelCategory
import java.util.*

class ProfileAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileAdminBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //arraylist to hold categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    //adapter
    private lateinit var adapterCategory: AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()
        window.statusBarColor = resources.getColor(R.color.pink02, theme)
        //search
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int){

            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int){
                try {
                    adapterCategory.filter?.filter(s)
                }
                catch (e: Exception){

                }
            }
            override fun afterTextChanged(p0: Editable?){

            }
        })

        //handle click, logout
        binding.logoutBtn.setOnClickListener{
            firebaseAuth.signOut()
            checkUser()
        }

        binding.addCategoryBtn.setOnClickListener {
            startActivity(Intent(this, CategoryAddActivity::class.java))
        }

        //handle click, start add recipe
        binding.addPdfFab.setOnClickListener {
            startActivity(Intent(this, RecipeAddActivity::class.java))
        }

        //handle click, open profile
        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun loadCategories() {
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelCategory::class.java)
                    val categoryImage = "${snapshot.child("categoryImage").value}"
                    categoryArrayList.add(model!!)

                }
                adapterCategory = AdapterCategory(this@ProfileAdminActivity, categoryArrayList)

                binding.categoriesRv.layoutManager = GridLayoutManager(this@ProfileAdminActivity, 3)
                binding.categoriesRv.adapter = adapterCategory

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null){
           startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        else
        {
            val email = firebaseUser.email
            binding.subTitleTv.text=email
        }
    }
}