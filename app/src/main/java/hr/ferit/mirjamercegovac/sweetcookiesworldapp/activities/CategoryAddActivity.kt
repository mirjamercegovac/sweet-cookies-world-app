package hr.ferit.mirjamercegovac.sweetcookiesworldapp.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.collection.arraySetOf
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.R
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.databinding.ActivityCategoryAddBinding
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.databinding.ActivityRegisterBinding
import java.util.*

class CategoryAddActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityCategoryAddBinding
    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    //image uri
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        window.statusBarColor = resources.getColor(R.color.pink02, theme)
        //configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Molimo pričekajte")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle click, go back
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        //handle click, pick image from camera/gallery
        binding.categoryIv.setOnClickListener {
            showImageAttachMenu()
        }

        //handle click, upload category
        binding.submitBtn.setOnClickListener{
            validateData()
        }
    }

    private var category = ""

    private fun validateData() {
        //validate data

        //get data
        category = binding.categoryEt.text.toString()

        if (category.isEmpty()){
            Toast.makeText(this, "Unesite kategoriju...", Toast.LENGTH_SHORT).show()
        }
        else{

            if (imageUri == null){
                //update without image
                addCategoryFirebase("")
            }
            else{
                //update with image
                uploadImage()
            }
        }
    }

    private fun uploadImage(){
        progressDialog.setMessage("Učitavanje slike kategorije")
        progressDialog.show()

        val filePathAndName = "CategoryImages/"+firebaseAuth.uid

        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"

                addCategoryFirebase(uploadedImageUrl)
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Neuspješno dodavanje slike ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addCategoryFirebase(uploadedImageUrl: String) {
        progressDialog.show()

        val timestamp = System.currentTimeMillis()

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"
        if (imageUri != null){
            hashMap["categoryImage"] = uploadedImageUrl
        }

        val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Categories")
        ref.child("$timestamp").setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Uspješno dodana...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Neuspješno dodana ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun showImageAttachMenu(){
        val popupMenu = PopupMenu(this, binding.categoryIv)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Kamera")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Galerija")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item->
            val id = item.itemId
            if (id == 0){
                pickImageCamera()
            }
            else if (id == 1){
                pickImageGallery()
            }

            true
        }
    }

    private fun pickImageCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp_Naslov")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Opis")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)

    }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == Activity.RESULT_OK){
                val data = result.data
                //imageUri = data!!.data
            }
            else{
                Toast.makeText(this, "Otkazano", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data
            }
            else{
                Toast.makeText(this, "Otkazano", Toast.LENGTH_SHORT).show()
            }
        }
    )
}

