package com.thk.instagram_clone.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.ListenerRegistration
import com.thk.instagram_clone.adapter.PostListAdapter
import com.thk.instagram_clone.util.Firebase
import com.thk.instagram_clone.databinding.FragmentAccountBinding
import com.thk.instagram_clone.model.ContentDto
import com.thk.instagram_clone.model.FollowDto
import com.thk.instagram_clone.util.GlideApp
import com.thk.instagram_clone.viewmodel.AccountData
import com.thk.instagram_clone.viewmodel.AccountViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class AccountFragment : Fragment() {
    private val TAG = AccountFragment::class.simpleName
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val uid: String? get() = Firebase.auth.currentUser?.uid

    private val viewModel: AccountViewModel by viewModels()
    private val listAdapter = PostListAdapter()

    private val albumLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 사진을 선택했을 때
            viewModel.uploadProfileImage(result.data?.data)
        }
    }

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

        binding.btnProfile.setOnClickListener { Firebase.auth.signOut() }
        binding.ivProfile.setOnClickListener {
            val imagePickerIntent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            albumLauncher.launch(imagePickerIntent)
        }

        lifecycleScope.launch {
            merge(
                viewModel.profileImageUrl,
                viewModel.followData,
                viewModel.postList
            )
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collectLatest { data ->
                    when (data) {
                        is AccountData.ProfileImageData -> {
                            GlideApp.with(binding.ivProfile)
                                .load(data.url)
                                .circleCrop()
                                .into(binding.ivProfile)
                        }
                        is AccountData.FollowData -> {
                            binding.apply {
                                tvFollowingCount.text = data.data.followingCount.toString()
                                tvFollowerCount.text = data.data.followerCount.toString()
                            }
                        }
                        is AccountData.PostsData -> {
                            listAdapter.submitList(data.list)
                        }
                    }
                }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}