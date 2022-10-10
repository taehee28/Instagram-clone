package com.thk.instagram_clone.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.thk.instagram_clone.R
import com.thk.instagram_clone.databinding.FragmentAlarmBinding
import com.thk.instagram_clone.databinding.ItemCommentBinding
import com.thk.instagram_clone.model.ALARM_COMMENT
import com.thk.instagram_clone.model.ALARM_FOLLOW
import com.thk.instagram_clone.model.ALARM_LIKE
import com.thk.instagram_clone.model.AlarmDto
import com.thk.instagram_clone.util.Firebase
import com.thk.instagram_clone.util.GlideApp

class AlarmFragment : Fragment() {
    private val TAG = AlarmFragment::class.simpleName
    private var _binding: FragmentAlarmBinding? = null
    private val binding get() = _binding!!

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

        getAlarmsFromFirestore()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun getAlarmsFromFirestore() {
        Firebase.firestore
            .collection("alarms")
            .whereEqualTo("destinationUid", Firebase.auth.currentUser?.uid)
            .addSnapshotListener { value, error ->
                kotlin.runCatching {
                    value?.documents?.map {
                        it.toObject(AlarmDto::class.java)
                    } ?: throw IllegalArgumentException("null returned")
                }.onSuccess {
                    listAdapter.submitList(it)
                }.onFailure { e ->
                    e.printStackTrace()
                    error?.printStackTrace()
                }
            }
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