package hr.ferit.mirjamercegovac.sweetcookiesworldapp.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.R

class ProfileEditComposeActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private var progressDialog: ProgressDialog? = null
    private var imageUri: Uri? = null

    private val nameState = mutableStateOf("")
    private val profileImageState = mutableStateOf("")
    private val showImageDialogState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this).apply {
            setTitle("Molimo pričekajte")
            setCanceledOnTouchOutside(false)
        }
        window.statusBarColor = resources.getColor(R.color.pink02, theme)

        loadUserInfo()

        setContent {
            if (showImageDialogState.value) {
                AlertDialog(
                    onDismissRequest = { showImageDialogState.value = false },
                    title = { Text("Odaberi sliku") },
                    buttons = {
                        Column(modifier = Modifier.padding(16.dp)) {
                            TextButton(onClick = {
                                showImageDialogState.value = false
                                pickImageCamera()
                            }) { Text("Kamera", color = Color(0xFFFF51D9)) }
                            TextButton(onClick = {
                                showImageDialogState.value = false
                                pickImageGallery()
                            }) { Text("Galerija", color = Color(0xFFFF51D9)) }
                        }
                    }
                )
            }
            ProfileEditScreen(
                name = nameState.value,
                profileImage = profileImageState.value,
                onBackClick = { onBackPressed() },
                onImageClick = { showImageDialogState.value = true },
                onUpdateClick = { name -> validateData(name) }
            )
        }
    }

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    nameState.value = "${snapshot.child("name").value}"
                    profileImageState.value = "${snapshot.child("profileImage").value}"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun validateData(name: String) {
        if (name.isEmpty()) {
            Toast.makeText(this, "Unesi ime", Toast.LENGTH_SHORT).show()
        } else {
            if (imageUri == null) {
                updateProfile(name, "")
            } else {
                uploadImage(name)
            }
        }
    }

    private fun uploadImage(name: String) {
        progressDialog?.setMessage("Učitavanje slike profila")
        progressDialog?.show()
        val filePathAndName = "ProfileImages/" + firebaseAuth.uid
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                updateProfile(name, "${uriTask.result}")
            }
            .addOnFailureListener { e ->
                progressDialog?.dismiss()
                Toast.makeText(this, "Neuspješno ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfile(name: String, uploadedImageUrl: String) {
        progressDialog?.setMessage("Ažuriranje profila...")
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["name"] = name
        if (imageUri != null) {
            hashMap["profileImage"] = uploadedImageUrl
        }
        val reference = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog?.dismiss()
                Toast.makeText(this, "Profil ažuriran", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog?.dismiss()
                Toast.makeText(this, "Neuspješno ${e.message}", Toast.LENGTH_SHORT).show()
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
            if (result.resultCode == Activity.RESULT_OK) {
                profileImageState.value = imageUri.toString()
            } else {
                Toast.makeText(this, "Otkazano", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri = result.data!!.data
                profileImageState.value = imageUri.toString()
            } else {
                Toast.makeText(this, "Otkazano", Toast.LENGTH_SHORT).show()
            }
        }
    )
}

@Composable
fun ProfileEditScreen(
    name: String,
    profileImage: String,
    onBackClick: () -> Unit,
    onImageClick: () -> Unit,
    onUpdateClick: (String) -> Unit
) {
    var nameValue by remember { mutableStateOf(name) }
    val pink02 = Color(0xFFFF51D9)
    val gray01 = Color(0xFFC5C5C5)
    val nunitoRegular = FontFamily(Font(R.font.nunito_regular))

    LaunchedEffect(name) {
        nameValue = name
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.back04),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(
                    color = pink02,
                    shape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)
                )
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back_white),
                    contentDescription = "Natrag",
                    tint = Color.White
                )
            }

            Text(
                text = "Uredi profil",
                fontSize = 20.sp,
                fontFamily = nunitoRegular,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
            )

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-60).dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .clickable { onImageClick() }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_person_gray),
                    contentDescription = "Profilna slika",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp)
                .padding(top = 225.dp)
        ) {
            TextField(
                value = nameValue,
                onValueChange = { nameValue = it },
                placeholder = { Text("Ime", color = gray01, fontFamily = nunitoRegular) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person_gray),
                        contentDescription = null,
                        tint = gray01
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor = gray01,
                    unfocusedIndicatorColor = gray01,
                    cursorColor = pink02,
                    textColor = Color.Black
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onUpdateClick(nameValue) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = pink02),
                elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Ažuriraj",
                    color = Color.White,
                    fontFamily = nunitoRegular,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}