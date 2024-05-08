package com.fyp.spotnbook.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fyp.spotnbook.R
import com.fyp.spotnbook.model.Comment
import com.google.android.material.imageview.ShapeableImageView

class CommentsAdapter(private var comments: List<Comment>) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    // ViewHolder class
    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize views here
        val userImage: ShapeableImageView = itemView.findViewById(R.id.commentUserImage)
        val commentText: TextView = itemView.findViewById(R.id.commentText)
    }

    // Create new ViewHolders (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    // Replace the contents of a ViewHolder (invoked by the layout manager)
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        // Bind data to views here
        Glide.with(holder.itemView.context)
            .load(comment.userImage)
            .placeholder(R.drawable.anonymous)
            .error(R.drawable.anonymous)
            .into(holder.userImage)
        holder.commentText.text = comment.comment
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return comments.size
    }

    // Update the comments list and notify the adapter of the changes
    fun updateComments(newComments: List<Comment>) {
        comments = newComments
        notifyDataSetChanged()
    }
}
