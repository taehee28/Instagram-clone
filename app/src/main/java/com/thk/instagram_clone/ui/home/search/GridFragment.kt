package com.thk.instagram_clone.ui.home.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.thk.instagram_clone.R
import com.thk.instagram_clone.databinding.FragmentGridBinding
import com.thk.instagram_clone.ui.home.common.PostListAdapter
import com.thk.instagram_clone.ui.home.feed.DetailViewViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GridFragment : Fragment() {
    private var _binding: FragmentGridBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailViewViewModel by viewModels()

    companion object {
        @JvmStatic
        fun newInstance() = GridFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_grid, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = viewModel
            adapter = PostListAdapter()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}