package com.thk.instagram_clone.ui.home.account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.thk.instagram_clone.ui.login.LoginActivity
import com.thk.instagram_clone.R
import com.thk.data.util.Firebase
import com.thk.instagram_clone.databinding.FragmentAccountBinding
import com.thk.instagram_clone.ui.home.common.PostListAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountFragment : Fragment() {
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountViewModel by viewModels()

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
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnProfile.setOnClickListener {
                activity?.run {
                    Firebase.auth.signOut()
                    startActivity(Intent(activity, LoginActivity::class.java))
                    finish()
                }
            }

            ivProfile.setOnClickListener {
                val imagePickerIntent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
                albumLauncher.launch(imagePickerIntent)
            }

            lifecycleOwner = viewLifecycleOwner
            adapter = PostListAdapter()
            vm = viewModel
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}