package com.thk.instagram_clone

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.thk.instagram_clone.databinding.ActivityAddPhotoBinding
import com.thk.instagram_clone.util.LoadingDialog
import com.thk.instagram_clone.viewmodel.AddPhotoViewModel
import kotlinx.coroutines.launch

class AddPhotoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPhotoBinding

    private val viewModel: AddPhotoViewModel by viewModels()

    private val albumLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 사진을 선택했을 때
            val photoUri = result.data?.data
            viewModel.setUri(photoUri)
            binding.ivPhoto.setImageURI(photoUri)
        } else {
            // 선택하지 않고 취소했을 때
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnUpload.setOnClickListener { uploadContent() }

        val imagePickerIntent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        albumLauncher.launch(imagePickerIntent)
    }

    private fun uploadContent() = lifecycleScope.launch {
        LoadingDialog.show(this@AddPhotoActivity)

        val text = binding.etDescription.text?.toString() ?: ""
        viewModel.uploadContent(text)
            .fold(
                onSuccess = {
                    setResult(Activity.RESULT_OK)
                    finish()
                },
                onFailure = {
                    it.printStackTrace()
                    Toast.makeText(this@AddPhotoActivity, it.message, Toast.LENGTH_SHORT).show()
                }
            ).also {
                LoadingDialog.dismiss()
            }
    }
}