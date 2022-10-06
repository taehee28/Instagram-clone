package com.thk.instagram_clone.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.thk.instagram_clone.util.Firebase
import com.thk.instagram_clone.util.GlideApp
import com.thk.instagram_clone.databinding.FragmentDetailViewBinding
import com.thk.instagram_clone.databinding.ItemDetailViewBinding
import com.thk.instagram_clone.model.ContentDto

class DetailViewFragment : Fragment() {
    private val TAG = DetailViewFragment::class.simpleName
    private var _binding: FragmentDetailViewBinding? = null
    private val binding get() = _binding!!

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
            likeClickEvent = onLikeClicked
            profileClickEvent = onProfileClicked
        }

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
        Firebase.firestore
            .collection("images")
            .orderBy("timestamp")
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

    private val onLikeClicked = { countentUid: String?, isSelected: Boolean ->
        if (!countentUid.isNullOrBlank()) {
            // uid로 저장된 doc(업로드한 글) 찾아와서
            // doc을 ContentDto로 변경하고
            // ContentDto를 수정해서
            // 수정된 Dto를 바탕으로 doc이 수정될 수 있게
            // transaction으로 set 해줌

            val tsDoc = Firebase.firestore.collection("images").document(countentUid)

            Firebase.firestore.runTransaction { transaction ->
                val item = transaction.get(tsDoc).toObject(ContentDto::class.java) ?: return@runTransaction

                val isContains = Firebase.auth.currentUser?.uid?.let {
                    if (isSelected) item.likedUsers.put(it, true) else item.likedUsers.remove(it)
                    item.likedUsers.contains(it)
                }

                // null이라는 것은 로그인된 유저의 uid가 없다는것 == 비정상
                isContains ?: return@runTransaction

                transaction.set(
                    tsDoc,
                    item.copy(
                        likeCount = item.likeCount + if (isContains) 1 else -1
                    )
                )
            }
        }
    }

    private val onProfileClicked = { userUid: String?, userId: String? ->
        if (!(userUid.isNullOrBlank() || userId.isNullOrBlank())) {
            val action = DetailViewFragmentDirections.actionDetailViewFragmentToProfileViewFragment(userUid, userId)
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
    var profileClickEvent: ((String?, String?) -> Unit)? = null

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
                }

                ivProfile.setOnClickListener {
                    profileClickEvent?.invoke(userUid, userId)
                }
            }
        }

        internal fun bind(item: ContentDto) {
            contentUid = item.contentUid
            userUid = item.uid
            userId = item.userId

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