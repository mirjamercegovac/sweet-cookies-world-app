package hr.ferit.mirjamercegovac.sweetcookiesworldapp.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.filters.FilterRecipeAdmin
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.MyApplication
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.activities.RecipeDetailActivity
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.activities.RecipeEditActivity
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.databinding.RowRecipeAdminBinding
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.models.ModelRecipe
import java.util.*

class AdapterRecipeAdmin : RecyclerView.Adapter<AdapterRecipeAdmin.HolderRecipeAdmin>,  Filterable{

    //context
    private var context: Context
    //arrayList to hold pdfs recipes
    public var recipeArrayList: ArrayList<ModelRecipe>
    private val filterList: ArrayList<ModelRecipe>
    //viewBinding
    private lateinit var binding: RowRecipeAdminBinding

    private var filter : FilterRecipeAdmin? = null

    //constructor
    constructor(context: Context, recipeArrayList: ArrayList<ModelRecipe>) : super() {
        this.context = context
        this.recipeArrayList = recipeArrayList
        this.filterList = recipeArrayList
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderRecipeAdmin {
        binding = RowRecipeAdminBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderRecipeAdmin(binding.root)
    }

    override fun onBindViewHolder(holder: HolderRecipeAdmin, position: Int) {
        //get - set data, handle click

        //get data
        val model = recipeArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp
        //convert timestamp to dd/MM/yyyy format
        val formattedDate = MyApplication.formatTimeStamp(timestamp)

        //set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formattedDate

        //load category
        MyApplication.loadCategory(categoryId, holder.categoryTv)

        //load thumbnail
        MyApplication.loadPdfFromUrlSinglePage(pdfUrl, title, holder.pdfView, holder.progressBar)

        //handle click, edit and delete recipe
        holder.moreBtn.setOnClickListener {
            moreOptionsDialog(model, holder)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, RecipeDetailActivity::class.java)
            intent.putExtra("recipeId", pdfId)
            context.startActivity(intent)
        }

    }

    private fun moreOptionsDialog(model: ModelRecipe, holder: HolderRecipeAdmin) {
        //get id, url, title
        val recipeId = model.id
        val recipeUrl = model.url
        val recipeTitle = model.title

        //options to show in dialog
        val options = arrayOf("Uredi", "Obriši")

        //alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Odaberi")
            .setItems(options){ dialog, position ->
                if (position == 0){
                    val intent = Intent(context, RecipeEditActivity::class.java)
                    intent.putExtra("recipeId", recipeId)
                    context.startActivity(intent)
                }
                else if (position == 1){
                    MyApplication.deleteRecipe(context, recipeId, recipeUrl, recipeTitle)
                }
            }
            .show()
    }

    override fun getItemCount(): Int {
        return recipeArrayList.size
    }


    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterRecipeAdmin(filterList, this)
        }

        return filter as FilterRecipeAdmin
    }

    inner class HolderRecipeAdmin(itemView: View) : RecyclerView.ViewHolder(itemView){
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn
    }
}