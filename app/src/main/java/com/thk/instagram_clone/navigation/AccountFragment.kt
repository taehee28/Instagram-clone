package com.thk.instagram_clone.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thk.instagram_clone.MainActivity
import com.thk.instagram_clone.R
import com.thk.instagram_clone.util.Firebase
import com.thk.instagram_clone.util.GlideApp
import com.thk.instagram_clone.databinding.FragmentAccountBinding
import com.thk.instagram_clone.model.ContentDto

class AccountFragment : Fragment() {
    private val TAG = AccountFragment::class.simpleName
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val uid: String? get() = Firebase.auth.currentUser?.uid

    private val listAdapter = PostListAdapter()

    companion object {
        @JvmStatic
        fun newInstance() = AccountFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)

        binding.rvPostList.adapter = listAdapter.apply { /*todo: onClick*/ }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getPostListFromFirestore()
        binding.btnProfile.setOnClickListener { Firebase.auth.signOut() }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun getPostListFromFirestore() {
        Firebase.firestore
            .collection("images")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { value, error ->
                kotlin.runCatching {
                    value?.documents?.map {
                        it.toObject(ContentDto::class.java)?.copy(contentUid = it.id)
                    } ?: throw IllegalArgumentException("Failed to get list(null returned)")
                }.onSuccess { list ->
                    listAdapter.submitList(list)
                }.onFailure { e ->
                    e.printStackTrace()
                    error?.printStackTrace()
                }
            }
    }
}

class PostListAdapter : ListAdapter<ContentDto, PostListAdapter.PostViewHolder>(PostDiffUtil()) {
    inner class PostViewHolder(private val view: ImageView) : RecyclerView.ViewHolder(view) {
        init {
            // TODO: setOnClick
        }

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