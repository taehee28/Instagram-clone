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

class ProfileViewFragment : Fragment() {
    private val TAG = AccountFragment::class.simpleName
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val args: ProfileViewFragmentArgs by navArgs()
    private val uid: String? by lazy { args.uid.ifBlank { Firebase.auth.currentUser?.uid } }
    private val userId: String? by lazy { args.userId.ifBlank { Firebase.auth.currentUser?.email } }

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
        getPostListFromFirestore()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupProfileButton() = uid?.also {
        binding.btnProfile.apply {
            if (it == Firebase.auth.currentUser?.uid) {
                setText(R.string.signout)
                setOnClickListener { Firebase.auth.signOut() }
            } else {
                setText(R.string.follow)
                setOnClickListener { /*todo: follow*/ }
            }
        }
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