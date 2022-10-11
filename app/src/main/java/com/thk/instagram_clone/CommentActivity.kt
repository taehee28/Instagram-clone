package com.thk.instagram_clone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.thk.instagram_clone.databinding.ActivityCommentBinding
import com.thk.instagram_clone.databinding.ItemCommentBinding
import com.thk.instagram_clone.model.ALARM_COMMENT
import com.thk.instagram_clone.model.AlarmDto
import com.thk.instagram_clone.model.ContentDto
import com.thk.instagram_clone.util.FcmPush
import com.thk.instagram_clone.util.Firebase
import com.thk.instagram_clone.util.GlideApp

class CommentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCommentBinding

    private val args: CommentActivityArgs by navArgs()
    private val contentUid: String by lazy { args.contentUid }
    private val destinationUid: String by lazy { args.destinationUid }

    private val listAdapter = CommentListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSend.setOnClickListener {
            val text = binding.etContent.text.toString()
            binding.etContent.setText("")
            sendComment(text)
            registerCommentAlarm(destinationUid, text)
        }
        binding.rvCommentList.adapter = listAdapter

        getCommentsFromFirestore()
    }

    private fun sendComment(text: String) {
        val comment = ContentDto.Comment (
            userId = Firebase.auth.currentUser?.email ?: "",
            uid = Firebase.auth.currentUser?.uid ?: "",
            text = text,
            timestamp = System.currentTimeMillis()
        )

        Firebase.firestore
            .collection("images")
            .document(contentUid)
            .collection("comments")
            .document()
            .set(comment)
    }

    private fun registerCommentAlarm(destinationUid: String?, message: String) {
        if (!destinationUid.isNullOrBlank()) {
            val alarmDto = AlarmDto(
                destinationUid = destinationUid,
                userId = Firebase.auth.currentUser?.email ?: "",
                uid = Firebase.auth.currentUser?.uid ?: "",
                message = message,
                kind = ALARM_COMMENT,
                timestamp = System.currentTimeMillis()
            )

            Firebase.firestore
                .collection("alarms")
                .document()
                .set(alarmDto)

            val msg = "${Firebase.auth.currentUser?.email} ${getString(R.string.alarm_comment)} of $message"
            FcmPush.sendMessage(destinationUid, "Instagram-clone", msg)
        }
    }

    private fun getCommentsFromFirestore() {
        Firebase.firestore
            .collection("images")
            .document(contentUid)
            .collection("comments")
            .orderBy("timestamp")
            .addSnapshotListener { value, error ->
                kotlin.runCatching {
                    value?.documents?.map {
                        it.toObject(ContentDto.Comment::class.java)
                    } ?: throw IllegalArgumentException("Failed to get list(null returned)")
                }.onSuccess {
                    Log.d("TAG", "getCommentsFromFirestore: list = ${it.toString()}")
                    listAdapter.submitList(it)
                }.onFailure { e ->
                    e.printStackTrace()
                    error?.printStackTrace()
                }
            }
    }
}

class CommentListAdapter : ListAdapter<ContentDto.Comment, CommentListAdapter.CommentViewHolder>(CommentDiffUtil()) {
    inner class CommentViewHolder(private val binding: ItemCommentBinding) : ViewHolder(binding.root) {

        internal fun bind(item: ContentDto.Comment) {
            binding.apply {
                tvId.text = item.userId
                tvContent.text = item.text
            }

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
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class CommentDiffUtil : DiffUtil.ItemCallback<ContentDto.Comment>() {
    override fun areItemsTheSame(
        oldItem: ContentDto.Comment,
        newItem: ContentDto.Comment,
    ): Boolean {
        return (oldItem.userId == newItem.userId) and (oldItem.timestamp == newItem.timestamp)
    }

    override fun areContentsTheSame(
        oldItem: ContentDto.Comment,
        newItem: ContentDto.Comment,
    ): Boolean {
        return oldItem.text == newItem.text
    }
}