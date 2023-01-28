package hr.ferit.mirjamercegovac.sweetcookiesworldapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.SyncStateContract
import android.util.Log
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.Constants
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.databinding.ActivityRecipeViewBinding

class RecipeViewActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityRecipeViewBinding

    private companion object{
        const val TAG = "RECIPE_VIEW_TAG"
    }

    //recipe id
    var recipeId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recipeId = intent.getStringExtra("recipeId")!!
        loadRecipeDetails()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadRecipeDetails() {
        Log.d(TAG, "loadRecipeDetails: Dohvati pdf/recepte url iz baze")

        val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Recipes")
        ref.child(recipeId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val recipeUrl = snapshot.child("url").value
                    Log.d(TAG, "onDataChange: RECIPE_URL: $recipeUrl")

                    loadRecipeFromUrl("$recipeUrl")
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadRecipeFromUrl(recipeUrl: String){
        Log.d(TAG, "loadRecipeFromUrl: Dohvati recept/pdf iz skladišta koristeći URL")

        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(recipeUrl)
        reference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes->
                Log.d(TAG, "loadRecipeFromUrl: Uspješno dohvaćanje recepta")

                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onError { t->
                        Log.d(TAG, "loadRecipeFromUrl: ${t.message}")
                    }
                    .onPageError { page, t ->
                        Log.d(TAG, "loadRecipeFromUrl: ${t.message}")
                    }
                    .load()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e->
                Log.d(TAG, "loadRecipeFromUrl: Neuspješno dohvaćanje pdf/recepta ${e.message}")
                binding.progressBar.visibility = View.GONE

            }
    }
}