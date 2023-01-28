package hr.ferit.mirjamercegovac.sweetcookiesworldapp

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.activities.RecipeDetailActivity
import java.util.*

class MyApplication:Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object{

        fun formatTimeStamp(timestamp: Long) : String{
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp

            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            //pagesTv: TextView?
        ){
            val TAG = "PDF_THUMBNNAIL_TAG"

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->
                    Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                    pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onPageError { page, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onLoad { nbPages ->
                            Log.d(TAG,  "loadRecipeFromUrlSinglePage: Pages: $nbPages")
                            progressBar.visibility = View.INVISIBLE

                            /*if (pagesTv != null){
                                pagesTv.text = "$nbPages"
                            }*/
                        }
                        .load()
                }
                .addOnFailureListener {  e ->
                    Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")
                }
        }


        fun loadCategory(categoryId: String, categoryTv: TextView){
            //load category using category id from firebase
            val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get category
                        val category = "${snapshot.child("category").value}"
                        //set category
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }

        fun deleteRecipe(context: Context, recipeId: String, recipeUrl: String, recipeTitle: String){
           val TAG = "DELETE_RECIPE_TAG"

           Log.d(TAG, "deleteRecipe: brisanje...")

           val progressDialog = ProgressDialog(context)
           progressDialog.setTitle("Molimo pričekajte")
           progressDialog.setMessage("Brisanje $recipeTitle...")
           progressDialog.setCanceledOnTouchOutside(false)
           progressDialog.show()


           Log.d(TAG, "deleteRecipe: Brisanje iz storage...")
           val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(recipeUrl)
           storageReference.delete()
               .addOnSuccessListener {
                   Log.d(TAG, "deleteRecipe: Obrisano iz storage")
                   Log.d(TAG, "deleteRecipe: Brisnanje iz db...")

                   val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Recipes")
                   ref.child(recipeId)
                       .removeValue()
                       .addOnSuccessListener {
                           progressDialog.dismiss()
                           Toast.makeText(context, "Uspješno obrisano...", Toast.LENGTH_SHORT).show()
                           Log.d(TAG, "deleteRecipe: Brisnanje iz db...")
                       }
                       .addOnFailureListener { e->
                           progressDialog.dismiss()
                           Log.d(TAG, "deleteRecipe: Neuspješno brisanje iz db ${e.message}")
                           Toast.makeText(context, "Neuspješno brisanje ${e.message}", Toast.LENGTH_SHORT).show()
                       }
               }
               .addOnFailureListener { e->
                   progressDialog.dismiss()
                   Log.d(TAG, "deleteRecipe: Neuspješno brisanje iz storage ${e.message}")
                   Toast.makeText(context, "Neuspješno brisanje ${e.message}", Toast.LENGTH_SHORT).show()
               }
        }

        fun incrementRecipeViewCount(recipeId: String){
            //1) Get current recipe views count
            val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Recipes")
            ref.child(recipeId)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get views count
                        var viewsCount = "${snapshot.child("viewsCount").value}"

                        if (viewsCount=="" || viewsCount=="null"){
                            viewsCount = "0";
                        }
                        //2) Increment views count
                        val newViewsCount = viewsCount.toLong() + 1

                        val hashMap = HashMap<String, Any>()
                        hashMap["viewsCount"] = newViewsCount

                        val dbRef = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Recipes")
                        dbRef.child(recipeId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }

        public fun removeFromFavorite(context: Context, recipeId: String){
            val TAG = "REMOVE_FAV_TAG"
            Log.d(TAG, "removeFromFavorite: Uklanjenje iz favorita")

            val firebaseAuth = FirebaseAuth.getInstance();

            val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favourites").child(recipeId)
                .removeValue()
                .addOnSuccessListener {
                    Log.d(TAG, "removeFromFavorite: Uklonjeno iz favorite")
                    Toast.makeText(context, "Uklonjeno iz favorita", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e->
                    Log.d(TAG, "removeFromFavorite: Uklanjanje nije uspjelo ${e.message}")
                    Toast.makeText(context, "Uklanjanje nije uspjelo ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }


}