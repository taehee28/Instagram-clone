package com.thk.instagram_clone.navigation

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
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
import com.thk.instagram_clone.viewmodel.AccountViewModel
import com.thk.instagram_clone.viewmodel.AccountViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class ProfileViewFragment : Fragment() {
    private val TAG = ProfileViewFragment::class.simpleName
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
                            followDto = it
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

    private fun setupProfileButton() = uid?.also {
        binding.btnProfile.apply {
            setText(R.string.follow)
            setOnClickListener { viewModel.requestFollow(followDto) }
            isEnabled = it != Firebase.auth.currentUser?.uid
        }
    }
}