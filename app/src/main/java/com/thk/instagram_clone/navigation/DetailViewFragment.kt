package com.thk.instagram_clone.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.thk.instagram_clone.R
import com.thk.instagram_clone.databinding.FragmentDetailViewBinding
import com.thk.instagram_clone.databinding.ItemDetailViewBinding
import com.thk.data.model.ContentDto
import com.thk.instagram_clone.viewmodel.DetailViewViewModel

class DetailViewFragment : Fragment() {
    private var _binding: FragmentDetailViewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailViewViewModel by viewModels()
    private val listAdapter = DetailListAdapter().apply { setHasStableIds(true) }

    companion object {
        @JvmStatic
        fun newInstance() = DetailViewFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail_view, container, false)

        listAdapter.apply {
            likeClickEvent = viewModel::onLikeClicked
            likeAlarmEvent = viewModel::registerLikeAlarm
            profileClickEvent = onProfileClicked
            commentClickEvent = onCommentClicked
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.apply {
            adapter = listAdapter
            viewModel = this@DetailViewFragment.viewModel
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private val onProfileClicked = { userUid: String?, userId: String? ->
        if (!(userUid.isNullOrBlank() || userId.isNullOrBlank())) {
            val action = DetailViewFragmentDirections.actionDetailViewFragmentToProfileViewFragment(userUid, userId)
            findNavController().navigate(action)
        }
    }

    private val onCommentClicked = { contentUid: String?, userUid: String? ->
        if (!(contentUid.isNullOrBlank() || userUid.isNullOrBlank())) {
            val action = DetailViewFragmentDirections.actionDetailViewFragmentToCommentActivity(contentUid, userUid)
            findNavController().navigate(action)
        }
    }
}

/**
 * RecyclerView Adapter class
 */
class DetailListAdapter : ListAdapter<ContentDto, DetailListAdapter.DetailViewHolder>(DetailDiffUtil()) {
    var likeClickEvent: ((String?, Boolean) -> Unit)? = null
    var likeAlarmEvent: ((String?) -> Unit)? = null
    var profileClickEvent: ((String?, String?) -> Unit)? = null
    var commentClickEvent: ((String?, String?) -> Unit)? = null

    inner class DetailViewHolder(private val binding: ItemDetailViewBinding) : ViewHolder(binding.root) {
        private var contentUid: String? = null
        private var userUid: String? = null
        private var userId: String? = null

        init {
            // onClick 설정하기
            binding.apply {
                btnLike.setOnClickListener { view ->
                    view.isSelected = !view.isSelected
                    likeClickEvent?.invoke(contentUid, view.isSelected)
                    if (view.isSelected) likeAlarmEvent?.invoke(userUid)
                }

                ivProfile.setOnClickListener {
                    profileClickEvent?.invoke(userUid, userId)
                }

                btnComment.setOnClickListener {
                    commentClickEvent?.invoke(contentUid, userUid)
                }
            }
        }

        internal fun bind(item: ContentDto) {
            contentUid = item.contentUid
            userUid = item.uid
            userId = item.userId

            binding.data = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val binding = DataBindingUtil.inflate<ItemDetailViewBinding>(LayoutInflater.from(parent.context), R.layout.item_detail_view, parent, false)
        return DetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}

class DetailDiffUtil : DiffUtil.ItemCallback<ContentDto>() {
    override fun areItemsTheSame(oldItem: ContentDto, newItem: ContentDto): Boolean {
        return (oldItem.uid == newItem.uid) and (oldItem.timestamp == newItem.timestamp)
    }

    override fun areContentsTheSame(oldItem: ContentDto, newItem: ContentDto): Boolean {
        return (oldItem.likeCount == newItem.likeCount) or (oldItem.likedUsers == newItem.likedUsers)
    }
}