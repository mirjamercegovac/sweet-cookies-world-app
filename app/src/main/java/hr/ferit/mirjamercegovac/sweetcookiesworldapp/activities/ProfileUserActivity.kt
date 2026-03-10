package hr.ferit.mirjamercegovac.sweetcookiesworldapp.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.R
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.fragments.RecipesUserFragment
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.databinding.ActivityProfileUserBinding
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.models.ModelCategory
import java.util.*

class ProfileUserActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityProfileUserBinding
    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList: ArrayList<ModelCategory>
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        window.statusBarColor = resources.getColor(R.color.pink02, theme)
        setupWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        //handle click, logout
        binding.logoutBtn.setOnClickListener{
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        //handle click, open profile
        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupWithViewPagerAdapter(viewPager: ViewPager){
        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
            this
        )
        //init list
        categoryArrayList = ArrayList()

        //load categories from db
        val ref = FirebaseDatabase.getInstance("https://sweet-cookies-world-app-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()

                val modelAll = ModelCategory("01", "Svi recepti", 1, "")
                val modelMostViewed = ModelCategory("01", "Popularno", 1, "")
                //add to list
                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)
                viewPagerAdapter.addFragment(
                    RecipesUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.category}",
                        "${modelAll.uid}"
                    ), modelAll.category
                )
                viewPagerAdapter.addFragment(
                    RecipesUserFragment.newInstance(
                        "${modelMostViewed.id}",
                        "${modelMostViewed.category}",
                        "${modelMostViewed.uid}"
                    ), modelMostViewed.category
                )
                //refresh list
                viewPagerAdapter.notifyDataSetChanged()

                for (ds in snapshot.children){
                    val model = ds.getValue(ModelCategory::class.java)
                    categoryArrayList.add(model!!)
                    viewPagerAdapter.addFragment(
                        RecipesUserFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}"
                        ), model.category
                    )
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        viewPager.adapter = viewPagerAdapter
    }

    class ViewPagerAdapter(fm: FragmentManager, behavior: Int, context: Context): FragmentPagerAdapter(fm, behavior){
        private val fragmentsList: ArrayList<RecipesUserFragment> = ArrayList()
        private val fragmentTitleList: ArrayList<String> = ArrayList();

        private val context: Context

        init {
            this.context = context
        }

        override fun getCount(): Int {
            return fragmentsList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentsList[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }

        public fun addFragment(fragment: RecipesUserFragment, title: String){
            fragmentsList.add(fragment)
            fragmentTitleList.add(title)
        }

    }

    private fun checkUser() {
        //get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null){
            binding.subTitleTv.text="Niste ulogirani"

            //hide profile, logout
            binding.profileBtn.visibility = View.GONE
            binding.logoutBtn.visibility = View.GONE
        }
        else
        {
            val email = firebaseUser.email
            binding.subTitleTv.text=email

            //show profile, logout
            binding.profileBtn.visibility = View.VISIBLE
            binding.logoutBtn.visibility = View.VISIBLE
        }
    }
}