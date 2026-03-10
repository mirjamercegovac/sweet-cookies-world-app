package hr.ferit.mirjamercegovac.sweetcookiesworldapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.R
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.adapters.AdapterRecipeAdmin
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.databinding.ActivityRecipeListAdminBinding
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.models.ModelRecipe
import java.util.*

class RecipeListAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeListAdminBinding

    private companion object{
        const val TAG = "PDF_LIST_ADMIN_TAG"
    }

    //category id and title
    private var categoryId = ""
    private var category = ""

    private lateinit var recipeArrayList: ArrayList<ModelRecipe>
    private lateinit var adapterRecipeAdmin: AdapterRecipeAdmin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeListAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = resources.getColor(R.color.pink02, theme)
        //get from intent, that we passed from adapter
        val intent = intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        //handle click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //set category
        binding.subTitleTv.text = category
        //load recipe
        loadRecipeList()
        //search
        binding.searchEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterRecipeAdmin.filter!!.filter(s)
                }
                catch (e: Exception){
                    Log.d(TAG, "onTextChanged: ${e.message}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        } )
    }

    private fun loadRecipeList() {
        recipeArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Recipes")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    recipeArrayList.clear()
                    for(ds in snapshot.children){
                        val model = ds.getValue(ModelRecipe::class.java)

                        if (model != null){
                            recipeArrayList.add(model)
                            Log.d(TAG, "onDataChange: ${model.title} ${model.categoryId}")
                        }
                    }
                    adapterRecipeAdmin = AdapterRecipeAdmin(this@RecipeListAdminActivity, recipeArrayList)
                    binding.recipesRv.adapter = adapterRecipeAdmin
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}