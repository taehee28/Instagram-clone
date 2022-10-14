package com.thk.instagram_clone.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.thk.instagram_clone.adapter.PostListAdapter
import com.thk.instagram_clone.databinding.FragmentGridBinding
import com.thk.instagram_clone.viewmodel.DetailViewViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class GridFragment : Fragment() {
    private val TAG = GridFragment::class.simpleName
    private var _binding: FragmentGridBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailViewViewModel by viewModels()
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

        lifecycleScope.launch {
            viewModel.itemsFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .collectLatest {
                    listAdapter.submitList(it)
                }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}