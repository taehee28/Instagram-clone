package com.thk.instagram_clone.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thk.instagram_clone.databinding.FragmentGridBinding

class GridFragment : Fragment() {
    private val TAG = GridFragment::class.simpleName
    private var _binding: FragmentGridBinding? = null
    private val binding get() = _binding!!

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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}