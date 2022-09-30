package com.thk.instagram_clone.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.firebase.firestore.FirebaseFirestore
import com.thk.instagram_clone.GlideApp
import com.thk.instagram_clone.R
import com.thk.instagram_clone.databinding.FragmentDetailViewBinding
import com.thk.instagram_clone.databinding.ItemDetailViewBinding
import com.thk.instagram_clone.model.ContentDto

class DetailViewFragment : Fragment() {
    private val TAG = DetailViewFragment::class.simpleName
    private var _binding: FragmentDetailViewBinding? = null
    private val binding get() = _binding!!

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val listAdapter = DetailListAdapter()

    companion object {
        @JvmStatic
        fun newInstance() = DetailViewFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailViewBinding.inflate(inflater, container, false)

        binding.rvDetailList.adapter = listAdapter

        getItemListFromFirestore()

        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    /**
     * Firestore로부터 post 목록을 가져와 list dapter에 전달 
     */
    private fun getItemListFromFirestore() {
        firestore
            .collection("images")
            .orderBy("timestamp")
            .addSnapshotListener { value, error ->
                kotlin.runCatching {
                    value?.documents?.map { it.toObject(ContentDto::class.java) } ?:
                        throw IllegalArgumentException("Failed to get list(null returned)")
                }.onSuccess { list ->
                    listAdapter.submitList(list)
                }.onFailure { e ->
                    e.printStackTrace()
                    error?.printStackTrace()
                }
            }
    }
}

/**
 * RecyclerView Adapter class
 */
class DetailListAdapter : ListAdapter<ContentDto, DetailListAdapter.DetailViewHolder>(DetailDiffUtil()) {

    inner class DetailViewHolder(private val binding: ItemDetailViewBinding) : ViewHolder(binding.root) {
        init {
            // onClick 설정하기

        }

        internal fun bind(item: ContentDto) {
            binding.apply {
                tvUserName.text = item.userId ?: "null"

                // TODO: 추후 진짜 프로필 사진 받아오는 것으로 변경 예정
                GlideApp.with(ivProfile)
                    .load(item.imageUrl)
                    .into(ivProfile)

                GlideApp.with(ivPhoto)
                    .load(item.imageUrl)
                    .into(ivPhoto)

                tvDescription.text = item.description

                tvLikeCount.text = item.favoriteCount.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val binding = ItemDetailViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class DetailDiffUtil : DiffUtil.ItemCallback<ContentDto>() {
    override fun areItemsTheSame(oldItem: ContentDto, newItem: ContentDto): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ContentDto, newItem: ContentDto): Boolean {
        return oldItem == newItem
    }
}