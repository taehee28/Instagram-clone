package com.thk.instagram_clone.ui.home.feed

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.thk.instagram_clone.databinding.ActivityCommentBinding
import com.thk.instagram_clone.databinding.ItemCommentBinding
import com.thk.data.model.ContentDto
import com.thk.data.util.Firebase
import com.thk.instagram_clone.R
import com.thk.instagram_clone.util.GlideApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCommentBinding

    private val viewModel: CommentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_comment)
        setContentView(binding.root)

        binding.apply {
            lifecycleOwner = this@CommentActivity
            vm = viewModel
            adapter = CommentListAdapter()
        }
    }
}

class CommentListAdapter : ListAdapter<ContentDto.Comment, CommentListAdapter.CommentViewHolder>(CommentDiffUtil()) {
    inner class CommentViewHolder(private val binding: ItemCommentBinding) : ViewHolder(binding.root) {

        internal fun bind(item: ContentDto.Comment) {
            binding.apply {
                tvId.text = item.userId
                tvContent.text = item.text
            }

            Firebase.firestore
                .collection("profileImages")
                .document(item.uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val url = task.result["image"]
                        GlideApp.with(binding.ivProfile)
                            .load(url)
                            .circleCrop()
                            .into(binding.ivProfile)
                    }
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class CommentDiffUtil : DiffUtil.ItemCallback<ContentDto.Comment>() {
    override fun areItemsTheSame(
        oldItem: ContentDto.Comment,
        newItem: ContentDto.Comment,
    ): Boolean {
        return (oldItem.userId == newItem.userId) and (oldItem.timestamp == newItem.timestamp)
    }

    override fun areContentsTheSame(
        oldItem: ContentDto.Comment,
        newItem: ContentDto.Comment,
    ): Boolean {
        return oldItem.text == newItem.text
    }
}