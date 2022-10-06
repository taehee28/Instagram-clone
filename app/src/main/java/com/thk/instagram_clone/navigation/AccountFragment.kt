package com.thk.instagram_clone.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.thk.instagram_clone.adapter.PostListAdapter
import com.thk.instagram_clone.util.Firebase
import com.thk.instagram_clone.databinding.FragmentAccountBinding
import com.thk.instagram_clone.model.ContentDto
import com.thk.instagram_clone.util.GlideApp

class AccountFragment : Fragment() {
    private val TAG = AccountFragment::class.simpleName
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val uid: String? get() = Firebase.auth.currentUser?.uid

    private val listAdapter = PostListAdapter()

    private val albumLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 사진을 선택했을 때
            uploadProfileImage(result.data?.data)
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

        getProfileImageFromFirestore()
        getPostListFromFirestore()

        binding.btnProfile.setOnClickListener { Firebase.auth.signOut() }
        binding.ivProfile.setOnClickListener {
            val imagePickerIntent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            albumLauncher.launch(imagePickerIntent)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun getProfileImageFromFirestore() = kotlin.runCatching {
        Firebase.firestore
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

    private fun uploadProfileImage(imageUri: Uri?) = kotlin.runCatching {
        Firebase.storage.reference.child("userProfileImages").child(uid!!).also { ref ->
            ref.putFile(imageUri!!)
                .continueWithTask { ref.downloadUrl }
                .addOnSuccessListener { uri ->
                    val map = mapOf("image" to uri.toString())
                    Firebase.firestore.collection("profileImages").document(uid!!).set(map)
                }
        }

    }
}