package hr.ferit.mirjamercegovac.sweetcookiesworldapp.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.R
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.databinding.ActivityRecipeAddBinding
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.models.ModelCategory
import java.util.*

class RecipeAddActivity : AppCompatActivity() {


    private lateinit var binding: ActivityRecipeAddBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    private var recipeUri: Uri? = null

    private val TAG = "Recipe_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadRecipeCategories()
        window.statusBarColor = resources.getColor(R.color.pink02, theme)
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Molimo pričekajte")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle click, show category pick dialog
        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }

        //handle click, pick recipe intent
        binding.attachRecipeBtn.setOnClickListener {
            recipePickIntent()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        //STEP1: Validate Data
        Log.d(TAG, "validateData: validating data")

        //get data
        title = binding.titleEt.text.toString()
        description = binding.descriptionEt.text.toString()
        category = binding.categoryTv.text.toString()

        //validate data
        if (title.isEmpty()){
            Toast.makeText(this, "Unesite naziv...", Toast.LENGTH_SHORT).show()
        }
        else if (description.isEmpty()){
            Toast.makeText(this, "Unesite opis...", Toast.LENGTH_SHORT).show()

        }
        else if(category.isEmpty()){
            Toast.makeText(this, "Odaberite kategoriju...", Toast.LENGTH_SHORT).show()
        }
        else if(recipeUri == null){
            Toast.makeText(this, "Odaberite PDF recept...", Toast.LENGTH_SHORT).show()
        }
        else{
            uploadRecipeToStorage()
        }
    }

    private fun uploadRecipeToStorage() {
        //STEP2: Upload recipe to firebase storage
        Log.d(TAG, "uploadRecipeToStorage: uploading to storage...")

        progressDialog.setMessage("Učitavanje recepta...")
        progressDialog.show()

        val timestamp = System.currentTimeMillis()

        val filePathAndName = "Recipes/$timestamp"

        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(recipeUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "uploadRecipeToStorage: recipe uploaded now getting uri...")

                //STEP3: Get uri of firebase storage
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedRecipeUrl = "${uriTask.result}"

                uploadRecipeInfoToDb(uploadedRecipeUrl, timestamp)
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "uploadRecipeToStorage: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Nuespješno učitavanje ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadRecipeInfoToDb(uploadedRecipeUrl: String, timestamp: Long) {
        //STEP4: Upload Recipe info to firebase db
        Log.d(TAG, "uploadRecipeInfoToDb: uploading to db")
        progressDialog.setMessage("Učitavanje informacija o receptu...")

        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadedRecipeUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0

        val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Recipes")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "uploadRecipeInfoToDb: uploaded to db")
                progressDialog.dismiss()
                Toast.makeText(this, "Učitavanje...", Toast.LENGTH_SHORT).show()
                recipeUri = null
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "uploadRecipeInfoToDb: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Nuespješno učitavanje ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadRecipeCategories() {
        Log.d(TAG, "loadRecipeCategories: Loading recipe categories")
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelCategory::class.java)
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""


    private fun categoryPickDialog(){
        Log.d(TAG,"categoryPickDialog: Showing recipe category pick dialog")

        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices){
            categoriesArray[i] = categoryArrayList[i].category
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray){dialog, which->
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id
                binding.categoryTv.text = selectedCategoryTitle

                Log.d(TAG, "categoryPickDialog: Selected Category ID: $selectedCategoryId")
                Log.d(TAG, "categoryPickDialog: Selected Category Title: $selectedCategoryTitle")
            }
            .show()
    }

    private fun recipePickIntent(){
        Log.d(TAG, "recipePickIntent: starting recipe pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        recipeActivityResultLauncher.launch(intent)
    }

    val recipeActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{ result ->
            if (result.resultCode == RESULT_OK){
                Log.d(TAG, "PDF Picked ")
                recipeUri = result.data!!.data
            }else{
                Log.d(TAG, "PDF Pick cancelled ")
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )


}