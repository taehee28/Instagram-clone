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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.thk.instagram_clone.R
import com.thk.instagram_clone.databinding.FragmentAlarmBinding
import com.thk.instagram_clone.databinding.ItemCommentBinding
import com.thk.data.model.ALARM_COMMENT
import com.thk.data.model.ALARM_FOLLOW
import com.thk.data.model.ALARM_LIKE
import com.thk.data.model.AlarmDto
import com.thk.data.util.Firebase
import com.thk.instagram_clone.util.GlideApp
import com.thk.instagram_clone.viewmodel.AlarmViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class AlarmFragment : Fragment() {
    private val TAG = AlarmFragment::class.simpleName
    private var _binding: FragmentAlarmBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AlarmViewModel by viewModels()
    private val listAdapter = AlarmListAdapter()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvAlarmList.adapter = listAdapter

        lifecycleScope.launch {
            viewModel.alarmsFlow
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

internal class AlarmListAdapter : ListAdapter<AlarmDto, AlarmListAdapter.AlarmViewHolder>(AlarmDiffUtil()) {
    inner class AlarmViewHolder(private val binding: ItemCommentBinding) : ViewHolder(binding.root) {
        init {
            binding.tvContent.visibility = View.INVISIBLE
        }

        internal fun bind(item: AlarmDto) {
            Firebase.firestore
                .collection("profileImages")
                .document(item.uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val url = task.result["image"]

                        GlideApp.with(binding.ivProfile)
                            .load(url)
                            .circleCrop()
                            .into(binding.ivProfile)
                    }
                }

            val text = when (item.kind) {
                ALARM_LIKE -> binding.root.context.getString(R.string.alarm_favorite)
                ALARM_COMMENT -> "${binding.root.context.getString(R.string.alarm_comment)} of ${item.message}"
                ALARM_FOLLOW -> binding.root.context.getString(R.string.alarm_follow)
                else -> ""
            }
            binding.tvId.text = "${item.userId} $text"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

internal class AlarmDiffUtil : DiffUtil.ItemCallback<AlarmDto>() {
    override fun areItemsTheSame(oldItem: AlarmDto, newItem: AlarmDto): Boolean {
        return (oldItem.uid == newItem.uid) and (oldItem.timestamp == newItem.timestamp)
    }

    override fun areContentsTheSame(oldItem: AlarmDto, newItem: AlarmDto): Boolean {
        return oldItem.kind == newItem.kind
    }
}