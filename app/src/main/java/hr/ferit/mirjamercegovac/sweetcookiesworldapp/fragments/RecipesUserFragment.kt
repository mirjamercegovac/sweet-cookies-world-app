package hr.ferit.mirjamercegovac.sweetcookiesworldapp.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.adapters.AdapterRecipeUser
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.databinding.FragmentRecipesUserBinding
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.models.ModelRecipe
import java.util.*

class RecipesUserFragment : Fragment {

    private lateinit var binding: FragmentRecipesUserBinding

    public companion object{
        private const val TAG = "RECIPES_USER_TAG"

        public fun newInstance(categoryId: String, category: String, uid: String): RecipesUserFragment {
            val fragment = RecipesUserFragment()
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""

    private lateinit var recipeArrayList: ArrayList<ModelRecipe>
    private lateinit var adapterRecipeUser: AdapterRecipeUser

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //get arguments
        val args = arguments
        if (args != null){
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentRecipesUserBinding.inflate(LayoutInflater.from(context), container, false)

        Log.d(TAG, "onCreateView: Kategorija: $category")
        if (category == "Svi recepti"){
            loadAllRecipes()
        }else if(category == "Popularno"){
            loadMostViewedRecipes("viewsCount")
        }else{
            loadCategorizedRecipes()
        }
        //search
        binding.searchEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterRecipeUser.filter.filter(s)
                }catch (e: Exception){
                    Log.d(TAG, "onTextChanged: Pretraživanje iznimke ${e.message}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        } )

        return binding.root
    }
    private fun loadAllRecipes() {
        recipeArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Recipes")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                recipeArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelRecipe::class.java)
                    recipeArrayList.add(model!!)
                }
                adapterRecipeUser = AdapterRecipeUser(context!!, recipeArrayList)
                binding.recipesRv.adapter = adapterRecipeUser
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun loadMostViewedRecipes(orderBy: String) {
        recipeArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Recipes")
        ref.orderByChild(orderBy).limitToLast(10) //load 10 most viewed
            .addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                recipeArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelRecipe::class.java)
                    recipeArrayList.add(model!!)
                }
                adapterRecipeUser = AdapterRecipeUser(context!!, recipeArrayList)
                binding.recipesRv.adapter = adapterRecipeUser
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun loadCategorizedRecipes() {
        recipeArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Recipes")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    recipeArrayList.clear()
                    for (ds in snapshot.children){
                        val model = ds.getValue(ModelRecipe::class.java)
                        recipeArrayList.add(model!!)
                    }
                    adapterRecipeUser = AdapterRecipeUser(context!!, recipeArrayList)
                    binding.recipesRv.adapter = adapterRecipeUser
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

}