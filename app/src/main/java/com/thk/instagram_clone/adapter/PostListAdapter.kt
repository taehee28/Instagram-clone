package com.thk.instagram_clone.adapter

import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thk.data.model.ContentDto
import com.thk.instagram_clone.util.GlideApp

class PostListAdapter : ListAdapter<ContentDto, PostListAdapter.PostViewHolder>(PostDiffUtil()) {
    inner class PostViewHolder(private val view: ImageView) : RecyclerView.ViewHolder(view) {
        internal fun bind(item: ContentDto) {
            GlideApp.with(view)
                .load(item.imageUrl)
                .centerCrop()
                .into(view)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val size = parent.resources.displayMetrics.widthPixels / 3
        val imageView = ImageView(parent.context).apply {
            layoutParams = LinearLayoutCompat.LayoutParams(size, size)
        }

        return PostViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}

class PostDiffUtil : DiffUtil.ItemCallback<ContentDto>() {
    override fun areItemsTheSame(oldItem: ContentDto, newItem: ContentDto): Boolean {
        return (oldItem.timestamp == newItem.timestamp) and (oldItem.uid == newItem.uid)
    }

    override fun areContentsTheSame(oldItem: ContentDto, newItem: ContentDto): Boolean {
        return oldItem.imageUrl == newItem.imageUrl
    }
}