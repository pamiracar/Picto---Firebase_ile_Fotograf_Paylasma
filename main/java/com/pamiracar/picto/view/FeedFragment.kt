package com.pamiracar.picto.view

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.pamiracar.picto.R
import com.pamiracar.picto.adapter.PostAdapter
import com.pamiracar.picto.databinding.FragmentFeedBinding
import com.pamiracar.picto.model.Post

class FeedFragment : Fragment(),PopupMenu.OnMenuItemClickListener{

    private var _binding: FragmentFeedBinding? = null
    private lateinit var popup: PopupMenu
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private var adapter : PostAdapter? = null
    val postList : ArrayList<Post> = arrayListOf()
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        popup = PopupMenu(requireContext(),binding.floatingActionButton)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.my_popup_menu,popup.menu)
        popup.setOnMenuItemClickListener(this)
        binding.floatingActionButton.setOnClickListener {
            popup.show()
        }
        fireStoreVerileriAl()
        adapter = PostAdapter(postList)
        binding.feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.feedRecyclerView.adapter = adapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fireStoreVerileriAl(){
        db.collection("Posts").orderBy("date",
            Query.Direction.DESCENDING).addSnapshotListener { value, error ->
            if (error != null){
                Toast.makeText(requireContext(),error.localizedMessage, Toast.LENGTH_LONG).show()
            } else{
                if (value != null){
                    if (!value.isEmpty){
                        //veriler boş değilse
                        postList.clear()
                        val documents = value.documents
                        for (document in documents){
                            val kullaniciEmail = document.get("email") as String // casting
                            val kullaniciYorum = document.get("comment") as String
                            val gorselUrl = document.get("downloadUrl") as String

                            val post = Post(kullaniciEmail,kullaniciYorum,gorselUrl)
                            postList.add(post)
                        }
                        adapter?.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.yuklemeItem){
            val action = FeedFragmentDirections.actionFeedFragmentToYuklemeFragment()
            Navigation.findNavController(requireView()).navigate(action)
        }else if (item?.itemId == R.id.cikisItem){
            auth.signOut()
            val action = FeedFragmentDirections.actionFeedFragmentToKullaniciFragment()
            Navigation.findNavController(requireView()).navigate(action)
        }
        return true
}

}