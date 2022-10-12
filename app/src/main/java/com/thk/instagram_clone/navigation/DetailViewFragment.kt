package com.thk.instagram_clone.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.firebase.firestore.ktx.snapshots
import com.thk.instagram_clone.R
import com.thk.instagram_clone.util.Firebase
import com.thk.instagram_clone.util.GlideApp
import com.thk.instagram_clone.databinding.FragmentDetailViewBinding
import com.thk.instagram_clone.databinding.ItemDetailViewBinding
import com.thk.instagram_clone.model.ALARM_LIKE
import com.thk.instagram_clone.model.AlarmDto
import com.thk.instagram_clone.model.ContentDto
import com.thk.instagram_clone.util.FcmPush
import com.thk.instagram_clone.util.PathString
import com.thk.instagram_clone.viewmodel.DetailViewViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class DetailViewFragment : Fragment() {
    private val TAG = DetailViewFragment::class.simpleName
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
        _binding = FragmentDetailViewBinding.inflate(inflater, container, false)

        binding.rvDetailList.adapter = listAdapter.apply {
            likeClickEvent = viewModel::onLikeClicked
            likeAlarmEvent = viewModel::registerLikeAlarm
            profileClickEvent = onProfileClicked
            commentClickEvent = onCommentClicked
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel.itemsFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collectLatest {
                    listAdapter.submitList(it)
                }
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
    private val TAG = DetailListAdapter::class.simpleName

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

            binding.apply {
                tvUserName.text = item.userId ?: "null"

                Firebase.firestore
                    .collection(PathString.profileImages)
                    .document(item.uid ?: "")
                    .get()
                    .addOnCompleteListener { task ->
                        val url = task.result.data?.get("image")

                        GlideApp.with(ivProfile)
                            .load(url)
                            .circleCrop()
                            .error(R.drawable.ic_account)
                            .into(ivProfile)
                    }

                GlideApp.with(ivPhoto)
                    .load(item.imageUrl)
                    .into(ivPhoto)

                tvDescription.text = item.description

                tvLikeCount.text = item.likeCount.toString()

                Firebase.auth.currentUser?.uid?.also {
                    btnLike.isSelected = item.likedUsers.contains(it)
                }
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