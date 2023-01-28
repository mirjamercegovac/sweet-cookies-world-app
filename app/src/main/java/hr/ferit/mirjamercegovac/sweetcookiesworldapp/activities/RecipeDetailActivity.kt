package hr.ferit.mirjamercegovac.sweetcookiesworldapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.MyApplication
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.R
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.databinding.ActivityRecipeDetailBinding
import java.util.*

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeDetailBinding

    private companion object {
        const val TAG = "RECIPE_DETAILS_TAG"
    }

    private var recipeId = ""

    //private var recipeTitle = ""
    //private var recipeUrl = ""

    //boolean value false/true
    private var isInMyFavorite = false

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recipeId = intent.getStringExtra("recipeId")!!

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null){
            checkIsFavorite()
        }

        MyApplication.incrementRecipeViewCount(recipeId)

        loadRecipeDetails()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.readRecipeBtn.setOnClickListener {
            val intent = Intent(this,RecipeViewActivity::class.java)
            intent.putExtra("recipeId", recipeId);
            startActivity(intent)
        }

        binding.favouriteBtn.setOnClickListener {
            //add only if user is logged in
            //1) check if user is logged
            if (firebaseAuth.currentUser == null){
                Toast.makeText(this, "Nista logirani", Toast.LENGTH_SHORT).show()
            }
            else{
                if (isInMyFavorite){
                    MyApplication.removeFromFavorite(this, recipeId)
                }else{
                    addToFavorite()
                }
            }
        }
    }

    private fun loadRecipeDetails() {
        val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Recipes")
        ref.child(recipeId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    MyApplication.loadCategory(categoryId, binding.categoryTv)

                    MyApplication.loadPdfFromUrlSinglePage(
                        "$url",
                        "$title",
                        binding.pdfView,
                        binding.progressBar
                    )

                    binding.titleTv.text = title
                    binding.descriptionTv.text = description
                    binding.viewsTv.text = viewsCount
                    binding.dateTv.text = date

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun checkIsFavorite(){
        Log.d(TAG, "checkIsFavorite: Provjera je li recept u favoritima ili ne")

        val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favourites").child(recipeId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists()
                    if (isInMyFavorite){
                        Log.d(TAG,"onDataChange: dostupno u omiljenim ")
                        binding.favouriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.ic_baseline_favorite_white, 0, 0)
                        binding.favouriteBtn.text = "Ukloni iz favorita"
                    }else{
                        Log.d(TAG,"onDataChange: nije dostupno u omiljenim ")
                        binding.favouriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.ic_favorite_border_white, 0, 0)
                        binding.favouriteBtn.text = "Dodaj u favorite"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun addToFavorite(){
        Log.d(TAG, "addToFavorite: Dodavanje u favorite")
        val timestamp = System.currentTimeMillis()

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["recipeId"] = recipeId
        hashMap["timestamp"] = timestamp

        val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favourites").child(recipeId)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "addToFavourite: Dodano u favorite")
                Toast.makeText(this, "Dodano u favorite", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Log.d(TAG, "addToFavorite: Dodavanje nije uspjelo ${e.message}")
                Toast.makeText(this, "Dodavanje nije uspjelo ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}