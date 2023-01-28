package hr.ferit.mirjamercegovac.sweetcookiesworldapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.activities.MainActivity
import hr.ferit.mirjamercegovac.sweetcookiesworldapp.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    //view binding
    private lateinit var binding: FragmentProfileBinding
    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentProfileBinding.inflate(layoutInflater)


        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()


        //handle click, logout
        binding.logoutBtn.setOnClickListener{
            firebaseAuth.signOut()
            val intent = Intent (getActivity(), MainActivity::class.java)
            getActivity()?.startActivity(intent)

        }
    }
    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null){
            binding.subTitleTv.text="Niste ulogirani"
        }
        else
        {
            val email = firebaseUser.email
            binding.subTitleTv.text=email
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root
    }



}