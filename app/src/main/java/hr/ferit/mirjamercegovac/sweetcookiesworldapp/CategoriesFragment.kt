package hr.ferit.mirjamercegovac.sweetcookiesworldapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.activities.CategoryAddActivity
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.adapters.AdapterCategory
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.databinding.FragmentCategoriesBinding
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.models.ModelCategory
import java.util.*


class CategoriesFragment : Fragment() {
    //view binding
    private lateinit var binding: FragmentCategoriesBinding
    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var adapterCategory: AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentCategoriesBinding.inflate(layoutInflater)


        firebaseAuth = FirebaseAuth.getInstance()
        loadCategories()

        //search
        binding.searchEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int){

            }
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int){
                try {
                    adapterCategory.filter?.filter(s)
                }
                catch (e: Exception){

                }
            }
            override fun afterTextChanged(p0: Editable?){

            }
        })

        //handle click, add category page
        binding.addCategoryBtn.setOnClickListener {
            val intent = Intent(getActivity(), CategoryAddActivity::class.java)
            getActivity()?.startActivity(intent)
        }
        //handle click, logout
        binding.logoutBtn.setOnClickListener{
            firebaseAuth.signOut()
        }
    }



    private fun loadCategories() {
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for (ds in snapshot.children){
                    val model = ds.getValue(ModelCategory::class.java)
                    categoryArrayList.add(model!!)
                }
               adapterCategory = AdapterCategory(context!!, categoryArrayList)

                binding.categoryRv.adapter = adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root
    }

}

