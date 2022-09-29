package com.thk.instagram_clone.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thk.instagram_clone.databinding.FragmentAlarmBinding

class AlarmFragment : Fragment() {
    private val TAG = AlarmFragment::class.simpleName
    private var _binding: FragmentAlarmBinding? = null
    private val binding get() = _binding!!

    companion object {
        @JvmStatic
        fun newInstance() = AlarmFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAlarmBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}