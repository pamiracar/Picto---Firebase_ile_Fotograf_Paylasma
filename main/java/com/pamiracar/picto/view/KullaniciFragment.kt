package com.pamiracar.picto.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pamiracar.picto.databinding.FragmentKullaniciBinding

class KullaniciFragment : Fragment() {

    private var _binding: FragmentKullaniciBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKullaniciBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.kayTOlButton.setOnClickListener {
            val email = binding.mailEdittext.text.toString()
            val password = binding.passwordEdittext.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()){
                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{ task ->
                    if (task.isSuccessful){
                        //kullanıcı oluşturma tamamlandı
                        println("kayıt oluştu")
                        val action = KullaniciFragmentDirections.actionKullaniciFragmentToFeedFragment2()
                        Navigation.findNavController(view).navigate(action)
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
                }

            }

        }

        binding.girisYapButton.setOnClickListener {
            val email = binding.mailEdittext.text.toString()
            val password = binding.passwordEdittext.text.toString()
            if (email.isNotEmpty()&&password.isNotEmpty()){
                auth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
                    println("girdi")
                    val action = KullaniciFragmentDirections.actionKullaniciFragmentToFeedFragment2()
                    Navigation.findNavController(view).navigate(action)
                }.addOnFailureListener { exception ->
                    Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
                }
            }
        }

        val guncelKullanici = auth.currentUser
        if (guncelKullanici != null){
            val action = KullaniciFragmentDirections.actionKullaniciFragmentToFeedFragment2()
            Navigation.findNavController(view).navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}