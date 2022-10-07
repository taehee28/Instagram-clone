package com.thk.instagram_clone.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.ListenerRegistration
import com.thk.instagram_clone.adapter.PostListAdapter
import com.thk.instagram_clone.databinding.FragmentGridBinding
import com.thk.instagram_clone.model.ContentDto
import com.thk.instagram_clone.util.Firebase

class GridFragment : Fragment() {
    private val TAG = GridFragment::class.simpleName
    private var _binding: FragmentGridBinding? = null
    private val binding get() = _binding!!

    private var postListLis: ListenerRegistration? = null

    private val listAdapter = PostListAdapter()

    companion object {
        @JvmStatic
        fun newInstance() = GridFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGridBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvPostList.adapter = listAdapter

        getPostListFromFirestore()
    }

    override fun onDestroyView() {
        _binding = null
        postListLis?.remove()
        super.onDestroyView()
    }

    private fun getPostListFromFirestore() {
        postListLis = Firebase.firestore
            .collection("images")
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