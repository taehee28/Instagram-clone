package com.thk.instagram_clone.ui.binding

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thk.instagram_clone.R
import com.thk.data.util.Firebase
import com.thk.instagram_clone.util.GlideApp
import com.thk.data.util.PathString
import com.thk.instagram_clone.util.LoadingDialog

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
    fun bindImageUrl(view: ImageView, url: String?) {
        GlideApp.with(view)
            .load(url)
            .into(view)
    }

    @JvmStatic
    @BindingAdapter("imageUri")
    fun bindImageUri(view: ImageView, uri: Uri?) {
        GlideApp.with(view)
            .load(uri)
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
    @BindingAdapter("accountProfileUrl")
    fun bindAccountProfileUrl(view: ImageView, url: String?) {
        GlideApp.with(view)
            .load(url)
            .circleCrop()
            .error(R.drawable.ic_account)
            .into(view)
    }

    @JvmStatic
    @BindingAdapter("isLiked")
    fun bindIsLiked(view: View, map: Map<String, Boolean>) {
        view.isSelected = map.containsKey(Firebase.auth.currentUser?.uid ?: "")
    }

    @JvmStatic
    @BindingAdapter("isLoading")
    fun bindLoadingDialog(view: View, isLoading: Boolean) {
        if (isLoading) {
            LoadingDialog.show(view.context)
        } else {
            LoadingDialog.dismiss()
        }
    }

    @JvmStatic
    @BindingAdapter("toast")
    fun bindToast(view: View, message: String?) {
        if (message.isNullOrBlank()) return
        Toast.makeText(view.context, message, Toast.LENGTH_LONG).show()
    }
}