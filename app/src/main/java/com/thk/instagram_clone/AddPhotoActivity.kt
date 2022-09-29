package com.thk.instagram_clone

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage
import com.thk.instagram_clone.databinding.ActivityAddPhotoBinding
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPhotoBinding

    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }
    private var photoUri: Uri? = null

    private val albumLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 사진을 선택했을 때
            photoUri = result.data?.data
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

    /**
     * Firebase에 사진 업로드
     */
    private fun uploadContent() = photoUri?.also {
        val fileName = createFileName()
        val storageRef = storage.reference.child("images").child(fileName)

        storageRef.putFile(it).addOnSuccessListener {
            Toast.makeText(this, getString(R.string.upload_success), Toast.LENGTH_SHORT).show()
        }
    } ?: Toast.makeText(this, getString(R.string.no_selected_image_path), Toast.LENGTH_SHORT).show()

    /**
     * 업로드 할 사진 파일의 이름 생성 
     */
    private fun createFileName() = run {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        "Image_${timestamp}_.png"
    }
}