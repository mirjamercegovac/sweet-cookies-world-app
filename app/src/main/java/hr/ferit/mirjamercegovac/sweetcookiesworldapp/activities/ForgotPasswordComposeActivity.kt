package hr.ferit.mirjamercegovac.sweetcookiesworldapp.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.R

class ForgotPasswordComposeActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this).apply {
            setTitle("Molimo pričekajte")
            setCanceledOnTouchOutside(false)
        }
        window.statusBarColor = resources.getColor(R.color.pink02, theme)

        setContent {
            ForgotPasswordScreen(
                onBackClick = { onBackPressed() },
                onSubmitClick = { email -> recoverPassword(email) }
            )
        }
    }

    private fun recoverPassword(email: String) {
        if (email.isEmpty()) {
            Toast.makeText(this, "Unesite email...", Toast.LENGTH_SHORT).show()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Neispravan email...", Toast.LENGTH_SHORT).show()
            return
        }
        progressDialog?.setMessage("Slanje uputa na $email")
        progressDialog?.show()
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog?.dismiss()
                Toast.makeText(this, "Upute poslane na $email", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog?.dismiss()
                Toast.makeText(this, "Neuspješno ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onSubmitClick: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    val pink02 = Color(0xFFFF51D9)
    val gray01 = Color(0xFFC5C5C5)

    val nunitoRegular = FontFamily(Font(R.font.nunito_regular))

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
                .height(270.dp)
                .background(
                    color = pink02,
                    shape = RoundedCornerShape(
                        bottomStart = 40.dp,
                        bottomEnd = 40.dp
                    )
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo01),
                    contentDescription = "Logo",
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Zaboravljena lozinka",
                    fontSize = 28.sp,
                    color = Color.White,
                    fontFamily = nunitoRegular,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Unesite svoj registrirani email kako biste dobili upute za ponovno postavljanje lozinke",
                    fontSize = 13.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontFamily = nunitoRegular,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp)
                .padding(top = 240.dp)
        ) {

            TextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Email", color = gray01, fontFamily = nunitoRegular) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_email_gray),
                        contentDescription = null,
                        tint = gray01
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor = pink02,
                    unfocusedIndicatorColor = gray01,
                    cursorColor = pink02,
                    textColor = Color.Black
                ),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontFamily = nunitoRegular,
                    fontWeight = FontWeight.Normal
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onSubmitClick(email) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = pink02),
                elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "PODNESI",
                    color = Color.White,
                    fontFamily = nunitoRegular,
                    fontSize = 14.sp
                )
            }
        }
    }
}