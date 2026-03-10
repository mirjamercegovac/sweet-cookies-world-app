package hr.ferit.mirjamercegovac.sweetcookiesworldapp.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.R
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.databinding.ActivityRecipeEditBinding
import java.util.*

class RecipeEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeEditBinding

    private companion object{
        private const val TAG = "RECIPE_EDIT_TAG"
    }

    private var recipeId = ""

    private lateinit var progressDialog: ProgressDialog

    private lateinit var categoryTitleArrayList: ArrayList<String>

    private lateinit var categoryIdArrayList: ArrayList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //get recipe id to edit info
        recipeId = intent.getStringExtra("recipeId")!!
        window.statusBarColor = resources.getColor(R.color.pink02, theme)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Molimo pričekajte")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadRecipeInfo()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.categoryTv.setOnClickListener {
            categoryDialog()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private fun loadRecipeInfo() {
        Log.d(TAG, "loadRecipeInfo: Učitavanje info recepta")

        val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Recipes")
        ref.child(recipeId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get recipe info
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()
                    //set to views
                    binding.titleEt.setText(title)
                    binding.descriptionEt.setText(description)

                    Log.d(TAG, "onDataChange: Učitavanje info kategorije recepta")
                    val refRecipeCategory = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Categories")
                    refRecipeCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                //get category
                                val category = snapshot.child("category").value
                                //set to textView
                                binding.categoryTv.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private var title = ""
    private var description = ""

    private fun validateData() {

        //get data
        title = binding.titleEt.text.toString()
        description = binding.descriptionEt.text.toString()

        //validate data
        if (title.isEmpty()){
            Toast.makeText(this, "Unesite naziv", Toast.LENGTH_SHORT).show()
        }
        else if (description.isEmpty()){
            Toast.makeText(this, "Unesite opis", Toast.LENGTH_SHORT).show()
        }
        else if(selectedCategoryId.isEmpty()) {
            Toast.makeText(this, "Odaberite kategoriju", Toast.LENGTH_SHORT).show()
        }
        else{
            updateRecipe()
        }
    }

    private fun updateRecipe() {
        Log.d(TAG, "updateRecipe: Počinje ažuriranje info o receptu...")
        progressDialog.setMessage("Ažuriranje informacija o receptu...")

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"

        val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Recipes")
        ref.child(recipeId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG, "updateRecipe: Uspješno ažuriranje...")
                Toast.makeText(this, "Uspješno ažuriranje...", Toast.LENGTH_SHORT).show()

            }
            .addOnFailureListener { e ->
                Log.d(TAG, "updateRecipe: Nuespješno ažuriranje ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Nuespješno ažuriranje ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryDialog() {

        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for (i in categoryTitleArrayList.indices){
            categoriesArray[i] = categoryTitleArrayList[i]
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Odaberi kategoriju")
            .setItems(categoriesArray){ dialog, position ->
                selectedCategoryId = categoryIdArrayList[position]
                selectedCategoryTitle = categoryTitleArrayList[position]

                binding.categoryTv.text = selectedCategoryTitle
            }
            .show()
    }

    private fun loadCategories() {
        Log.d(TAG, "loadCategories: učitavanje kategorija...")

        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()

                for (ds in snapshot.children){
                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)

                    Log.d(TAG, "onDataChange: Category ID $id")
                    Log.d(TAG, "onDataChange: Category ID $category")

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }
}