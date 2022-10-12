package com.thk.instagram_clone.navigation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.thk.instagram_clone.R
import com.thk.instagram_clone.adapter.PostListAdapter
import com.thk.instagram_clone.util.Firebase
import com.thk.instagram_clone.databinding.FragmentAccountBinding
import com.thk.instagram_clone.model.ContentDto
import com.thk.instagram_clone.model.FollowDto
import com.thk.instagram_clone.util.GlideApp
import com.thk.instagram_clone.util.LoadingDialog
import com.thk.instagram_clone.viewmodel.AccountViewModel
import com.thk.instagram_clone.viewmodel.AccountViewModelFactory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {
    private val TAG = AccountFragment::class.simpleName
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountViewModel by viewModels { AccountViewModelFactory(Firebase.auth.currentUser?.uid) }
    private val listAdapter = PostListAdapter()

    private val albumLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 사진을 선택했을 때
            LoadingDialog.show(requireContext())
            viewModel.uploadProfileImage(result.data?.data) {
                LoadingDialog.dismiss()
            }
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

        binding.rvPostList.adapter = listAdapter

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
            viewModel.accountDataFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collectLatest {
                    when (it) {
                        is String -> {
                            GlideApp.with(binding.ivProfile)
                                .load(it)
                                .circleCrop()
                                .error(R.drawable.ic_account)
                                .into(binding.ivProfile)
                        }
                        is FollowDto -> {
                            binding.apply {
                                tvFollowingCount.text = it.followingCount.toString()
                                tvFollowerCount.text = it.followerCount.toString()
                            }
                        }
                        is List<*> -> {
                            val list = it.filterIsInstance<ContentDto>()
                            listAdapter.submitList(list)
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