package hr.ferit.mirjamercegovac.sweetcookiesworldapp.filters

import android.widget.Filter
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.adapters.AdapterRecipeAdmin
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.models.ModelRecipe
import java.util.*

class FilterRecipeAdmin : Filter {

    var filterList: ArrayList<ModelRecipe>

    var adapterRecipeAdmin: AdapterRecipeAdmin

    //constructor
    constructor(filterList: ArrayList<ModelRecipe>, adapterRecipeAdmin: AdapterRecipeAdmin) {
        this.filterList = filterList
        this.adapterRecipeAdmin = adapterRecipeAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint:CharSequence? = constraint
        val results = FilterResults()

        if (constraint != null && constraint.isNotEmpty()){
            constraint = constraint.toString().lowercase()
            var filteredModels = ArrayList<ModelRecipe>()
            for (i in filterList.indices){
                if (filterList[i].title.lowercase().contains(constraint)){
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }
        else{
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        adapterRecipeAdmin.recipeArrayList = results!!.values as ArrayList<ModelRecipe>

        adapterRecipeAdmin.notifyDataSetChanged()
    }
}