package hr.ferit.mirjamercegovac.sweetcookiesworldapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.MyApplication
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.R
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.adapters.AdapterRecipeFavorite
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.databinding.ActivityProfileBinding
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.models.ModelRecipe
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var recipeArrayList: ArrayList<ModelRecipe>

    private lateinit var adapterRecipeFavorite: AdapterRecipeFavorite


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()
        loadFavoriteRecipes()
        window.statusBarColor = resources.getColor(R.color.pink02, theme)
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.profileEditBtn.setOnClickListener {
            startActivity(Intent(this, ProfileEditActivity::class.java))
        }
    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val userType = "${snapshot.child("userType").value}"

                    //convert timestamp to date
                    val formattedDate = MyApplication.formatTimeStamp(timestamp.toLong())

                    //set data
                    binding.nameTv.text = name
                    binding.emailTv.text = email
                    binding.memberDateTv.text = formattedDate
                    binding.accountTypeTv.text = userType
                    //set image
                    try {
                        Glide.with(this@ProfileActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileIv)
                    }catch (e: Exception){

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadFavoriteRecipes(){
        recipeArrayList = ArrayList();
        val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favourites")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    recipeArrayList.clear()
                    for (ds in snapshot.children){
                        val recipeId = "${ds.child("recipeId").value}"

                        val modelRecipe = ModelRecipe()
                        modelRecipe.id = recipeId

                        recipeArrayList.add(modelRecipe)
                    }
                    binding.favoriteRecipeCountTv.text = "${recipeArrayList.size}"

                    adapterRecipeFavorite = AdapterRecipeFavorite(this@ProfileActivity, recipeArrayList)

                    binding.favoriteRv.adapter = adapterRecipeFavorite
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }
}