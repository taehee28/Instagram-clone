package com.thk.instagram_clone.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thk.instagram_clone.R
import com.thk.instagram_clone.databinding.FragmentDetailViewBinding

class DetailViewFragment : Fragment() {
    private val TAG = DetailViewFragment::class.simpleName
    private var _binding: FragmentDetailViewBinding? = null
    private val binding get() = _binding!!

    companion object {
        @JvmStatic
        fun newInstance() = DetailViewFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailViewBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}