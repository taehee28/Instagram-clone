package com.thk.instagram_clone.ui.home.addphoto

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import com.thk.instagram_clone.R
import com.thk.instagram_clone.databinding.ActivityAddPhotoBinding
import dagger.hilt.android.AndroidEntryPoint

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
            finish = this@AddPhotoActivity::finish
        }

        val imagePickerIntent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        albumLauncher.launch(imagePickerIntent)
    }
}