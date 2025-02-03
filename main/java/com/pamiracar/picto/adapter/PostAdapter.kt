package com.pamiracar.picto.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pamiracar.picto.databinding.RecyclerRowBinding
import com.pamiracar.picto.model.Post
import com.squareup.picasso.Picasso

class PostAdapter(val postListesi : ArrayList<Post>) : RecyclerView.Adapter<PostAdapter.PostHolder>() {
    class PostHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PostHolder(recyclerRowBinding)

    }

    override fun getItemCount(): Int {
        return postListesi.size
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        holder.binding.Postmail.text = postListesi[position].email
        holder.binding.postAciklama.text = postListesi[position].comment
        Picasso.get().load(postListesi[position].downloadUrl).into(holder.binding.PostGorsel)


    }

}