package com.thk.instagram_clone

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.thk.instagram_clone.databinding.ActivityAddPhotoBinding
import com.thk.instagram_clone.util.LoadingDialog
import com.thk.instagram_clone.viewmodel.AddPhotoViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddPhotoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPhotoBinding

    private val viewModel: AddPhotoViewModel by viewModels()

    private val albumLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 사진을 선택했을 때
            val photoUri = result.data?.data
            viewModel.uri.value = photoUri
        } else {
            // 선택하지 않고 취소했을 때
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_photo)
        setContentView(binding.root)

        binding.apply {
            lifecycleOwner = this@AddPhotoActivity
            vm = viewModel
            btnUpload.setOnClickListener {
                viewModel.uploadContent(etDescription.text?.toString()) {
                    finish()
                }
            }
        }

        val imagePickerIntent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        albumLauncher.launch(imagePickerIntent)
    }
}