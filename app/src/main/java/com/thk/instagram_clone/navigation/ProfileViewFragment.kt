package com.thk.instagram_clone.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.thk.instagram_clone.MainActivity
import com.thk.instagram_clone.R
import com.thk.instagram_clone.adapter.PostListAdapter
import com.thk.data.util.Firebase
import com.thk.instagram_clone.databinding.FragmentAccountBinding
import com.thk.data.model.ContentDto
import com.thk.data.model.FollowDto
import com.thk.instagram_clone.util.GlideApp
import com.thk.instagram_clone.util.logd
import com.thk.instagram_clone.viewmodel.AccountViewModel
import com.thk.instagram_clone.viewmodel.AccountViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class ProfileViewFragment : Fragment() {
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val args: ProfileViewFragmentArgs by navArgs()
    private val uid: String? by lazy { args.uid.ifBlank { Firebase.auth.currentUser?.uid } }
    private val userId: String? by lazy { args.userId.ifBlank { Firebase.auth.currentUser?.email } }

    private val viewModel: AccountViewModel by viewModels { AccountViewModelFactory(uid) }

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

    companion object {
        @JvmStatic
        fun newInstance() = ProfileViewFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).supportActionBar?.subtitle = userId

        setupProfileButton()

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            adapter = PostListAdapter()
            vm = viewModel
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupProfileButton() = uid?.also {
        binding.btnProfile.apply {
            setText(R.string.follow)
            setOnClickListener { viewModel.requestFollow(followDto) }
            isEnabled = it != Firebase.auth.currentUser?.uid
        }
    }
}