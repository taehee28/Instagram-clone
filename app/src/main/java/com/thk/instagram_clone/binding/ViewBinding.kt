package com.thk.instagram_clone.binding

import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.thk.instagram_clone.R
import com.thk.instagram_clone.util.Firebase
import com.thk.instagram_clone.util.GlideApp
import com.thk.instagram_clone.util.PathString

object ViewBinding {
    @JvmStatic
    @BindingAdapter("adapter")
    fun bindAdapter(view: RecyclerView, adapter: RecyclerView.Adapter<*>) {
        view.adapter = adapter
    }

    @JvmStatic
    @BindingAdapter("submitList")
    fun bindSubmitList(view: RecyclerView, list: List<Any>?) {
        (view.adapter as? ListAdapter<Any, *>)?.submitList(list)
    }

    @JvmStatic
    @BindingAdapter("imageUrl")
    fun bindImageUrl(view: ImageView, url: String) {
        GlideApp.with(view)
            .load(url)
            .into(view)

    }

    @JvmStatic
    @BindingAdapter("profileUrl")
    fun bindProfileImage(view: ImageView, uid: String?) {
        Firebase.firestore
            .collection(PathString.profileImages)
            .document(uid ?: "")
            .get()
            .addOnCompleteListener { task ->
                val url = task.result.data?.get("image")

                GlideApp.with(view)
                    .load(url)
                    .circleCrop()
                    .error(R.drawable.ic_account)
                    .into(view)
            }
    }

    @JvmStatic
    @BindingAdapter("isLiked")
    fun bindIsLiked(view: View, map: Map<String, Boolean>) {
        view.isSelected = map.containsKey(Firebase.auth.currentUser?.uid ?: "")
    }
}