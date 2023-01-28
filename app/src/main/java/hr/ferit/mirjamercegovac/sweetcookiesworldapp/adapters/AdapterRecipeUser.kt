package hr.ferit.mirjamercegovac.sweetcookiesworldapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.filters.FilterRecipeUser
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.MyApplication
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.activities.RecipeDetailActivity
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.databinding.RowRecipeUserBinding
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.models.ModelRecipe
import java.util.*

class AdapterRecipeUser : RecyclerView.Adapter<AdapterRecipeUser.HolderRecipeUser>, Filterable{

    //context
    private var context: Context
    //arrayList to hold pdfs recipes
    public var recipeArrayList: ArrayList<ModelRecipe>
    //arrayList to hold filtered recipes
    public var filterList: ArrayList<ModelRecipe>
    //viewBinding
    private lateinit var binding: RowRecipeUserBinding

    private var filter: FilterRecipeUser? = null

    constructor(context: Context, recipeArrayList: ArrayList<ModelRecipe>) {
        this.context = context
        this.recipeArrayList = recipeArrayList
        this.filterList = recipeArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderRecipeUser {
        binding = RowRecipeUserBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderRecipeUser(binding.root)
    }

    override fun onBindViewHolder(holder: HolderRecipeUser, position: Int) {
        //get - set data, handle click

        //get data
        val model = recipeArrayList[position]
        val recipeId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val uid = model.uid
        val url = model.url
        val timestamp = model.timestamp

        //convert timestamp to dd/MM/yyyy format
        val date = MyApplication.formatTimeStamp(timestamp)

        //set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = date

        MyApplication.loadPdfFromUrlSinglePage(url, title, holder.pdfView, holder.progressBar)

        MyApplication.loadCategory(categoryId, holder.categoryTv)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, RecipeDetailActivity::class.java)
            intent.putExtra("recipeId", recipeId)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return recipeArrayList.size
    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterRecipeUser(filterList, this)
        }
        return filter as FilterRecipeUser
    }

    inner class HolderRecipeUser(itemView: View) : RecyclerView.ViewHolder(itemView){
        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val dateTv = binding.dateTv
    }




}