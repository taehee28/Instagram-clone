package com.thk.instagram_clone.navigation

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.firebase.firestore.ListenerRegistration
import com.thk.instagram_clone.MainActivity
import com.thk.instagram_clone.R
import com.thk.instagram_clone.adapter.PostListAdapter
import com.thk.instagram_clone.util.Firebase
import com.thk.instagram_clone.databinding.FragmentAccountBinding
import com.thk.instagram_clone.model.ALARM_FOLLOW
import com.thk.instagram_clone.model.AlarmDto
import com.thk.instagram_clone.model.ContentDto
import com.thk.instagram_clone.model.FollowDto
import com.thk.instagram_clone.util.FcmPush
import com.thk.instagram_clone.util.GlideApp
import kotlin.properties.Delegates

class ProfileViewFragment : Fragment() {
    private val TAG = ProfileViewFragment::class.simpleName
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val args: ProfileViewFragmentArgs by navArgs()
    private val uid: String? by lazy { args.uid.ifBlank { Firebase.auth.currentUser?.uid } }
    private val userId: String? by lazy { args.userId.ifBlank { Firebase.auth.currentUser?.email } }

    private var profileImageLis: ListenerRegistration? = null
    private var postListLis: ListenerRegistration? = null
    private var followListLis: ListenerRegistration? = null

    private var followDto: FollowDto by Delegates.observable(FollowDto()) { _, _, newValue ->
        kotlin.runCatching {
            binding.apply {
                tvFollowingCount.text = newValue.followingCount.toString()
                tvFollowerCount.text = newValue.followerCount.toString()
                if (newValue.followers.contains(Firebase.auth.currentUser?.uid)) {
                    btnProfile.setText(R.string.follow_cancel)
                } else {
                    btnProfile.setText(R.string.follow)
                }
            }
        }
    }

    private val listAdapter = PostListAdapter()

    companion object {
        @JvmStatic
        fun newInstance() = ProfileViewFragment()
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
        (activity as MainActivity).supportActionBar?.subtitle = userId
        setupProfileButton()

        getProfileImageFromFirestore()
        getFollowDataFromFirestore()
        getPostListFromFirestore()
    }

    override fun onDestroyView() {
        _binding = null
        profileImageLis?.remove()
        followListLis?.remove()
        postListLis?.remove()
        super.onDestroyView()
    }

    private fun setupProfileButton() = uid?.also {
        binding.btnProfile.apply {
            setText(R.string.follow)
            isEnabled = it != Firebase.auth.currentUser?.uid
            setOnClickListener { requestFollow() }
        }
    }

    private fun requestFollow() = kotlin.runCatching {
        val tsDocMyFollowing = Firebase.firestore.collection("users").document(Firebase.auth.currentUser?.uid!!)
        Firebase.firestore.runTransaction {
            val myFollow = it.get(tsDocMyFollowing).toObject(FollowDto::class.java) ?: FollowDto()
            if (myFollow.followings.containsKey(uid!!)) {
                it.set(
                    tsDocMyFollowing,
                    myFollow.copy(followingCount = myFollow.followingCount - 1).apply { followings.remove(uid!!) }
                )
            } else {
                it.set(
                    tsDocMyFollowing,
                    myFollow.copy(followingCount = myFollow.followingCount + 1).apply { followings[uid!!] = true }
                )
            }
        }

        val tsDocOthersFollower = Firebase.firestore.collection("users").document(uid!!)
        Firebase.firestore.runTransaction {
            if (followDto.followers.containsKey(Firebase.auth.currentUser?.uid)) {
                it.set(
                    tsDocOthersFollower,
                    followDto.copy(followerCount = followDto.followerCount - 1).apply { followers.remove(Firebase.auth.currentUser?.uid) }
                )
            } else {
                it.set(
                    tsDocOthersFollower,
                    followDto.copy(followerCount = followDto.followerCount + 1).apply { followers[Firebase.auth.currentUser?.uid!!] = true }
                )

                registerFollowAlarm()
            }
        }
    }

    private fun registerFollowAlarm() = uid?.let {
        val alarmDto = AlarmDto(
            destinationUid = it,
            userId = Firebase.auth.currentUser?.email ?: "",
            uid = Firebase.auth.currentUser?.uid ?: "",
            kind = ALARM_FOLLOW,
            timestamp = System.currentTimeMillis()
        )

        Firebase.firestore
            .collection("alarms")
            .document()
            .set(alarmDto)

        val msg = "${Firebase.auth.currentUser?.email} ${getString(R.string.alarm_follow)}"
        FcmPush.sendMessage(it, "Instagram-clone", msg)
    }

    private fun getProfileImageFromFirestore() = kotlin.runCatching {
        profileImageLis = Firebase.firestore
            .collection("profileImages")
            .document(uid!!)
            .addSnapshotListener { value, error ->
                kotlin.runCatching {
                    value?.data?.get("image") ?: throw IllegalArgumentException("Failed to get profile image(null returned)")
                }.onSuccess {
                    GlideApp.with(binding.ivProfile)
                        .load(it)
                        .circleCrop()
                        .into(binding.ivProfile)
                }.onFailure {
                    it.printStackTrace()
                    error?.printStackTrace()
                }
            }
    }

    private fun getFollowDataFromFirestore() = kotlin.runCatching {
        followListLis = Firebase.firestore
            .collection("users")
            .document(uid!!)
            .addSnapshotListener { value, error ->
                kotlin.runCatching {
                    value?.toObject(FollowDto::class.java)
                        ?: throw IllegalArgumentException("Failed to get follow list(null returned)")
                }.onSuccess {
                    followDto = it
                }.onFailure {
                    it.printStackTrace()
                    error?.printStackTrace()
                }
            }
    }

    private fun getPostListFromFirestore() {
        postListLis = Firebase.firestore
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