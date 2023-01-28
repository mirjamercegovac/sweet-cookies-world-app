package hr.ferit.mirjamercegovac.sweetcookiesworldapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.MyApplication
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.activities.RecipeDetailActivity
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.databinding.RowRecipeFavoriteBinding
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.models.ModelRecipe
import java.util.*

class AdapterRecipeFavorite : RecyclerView.Adapter<AdapterRecipeFavorite.HolderRecipeFavorite> {

    private val context: Context

    private var recipeArrayList: ArrayList<ModelRecipe>

    private lateinit var binding: RowRecipeFavoriteBinding

    constructor(context: Context, recipeArrayList: ArrayList<ModelRecipe>) {
        this.context = context
        this.recipeArrayList = recipeArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderRecipeFavorite {
        binding = RowRecipeFavoriteBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderRecipeFavorite(binding.root)
    }

    override fun onBindViewHolder(holder: HolderRecipeFavorite, position: Int) {
        val model = recipeArrayList[position]
        
        loadRecipeDetails(model, holder)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, RecipeDetailActivity::class.java)
            intent.putExtra("recipeId", model.id)
            context.startActivity(intent)
        }

        holder.removeFavBtn.setOnClickListener {
            MyApplication.removeFromFavorite(context, model.id)
        }
    }

    private fun loadRecipeDetails(model: ModelRecipe, holder: AdapterRecipeFavorite.HolderRecipeFavorite) {
        val recipeId = model.id

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

                    //set data to model
                    model.isFavorite = true
                    model.title = title
                    model.description = description
                    model.categoryId = categoryId
                    model.timestamp = timestamp.toLong()
                    model.uid = uid
                    model.url = url
                    model.viewsCount = viewsCount.toLong()

                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    MyApplication.loadCategory("$categoryId", holder.categoryTv)
                    MyApplication.loadPdfFromUrlSinglePage("$url", "$title", holder.pdfView, holder.progressBar)

                    holder.titleTv.text = title
                    holder.descriptionTv.text = description
                    holder.dateTv.text = date

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    override fun getItemCount(): Int {
        return recipeArrayList.size
    }

    inner class HolderRecipeFavorite(itemView: View) : RecyclerView.ViewHolder(itemView){
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var removeFavBtn = binding.removeFavBtn
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        var dateTv = binding.dateTv
    }
}