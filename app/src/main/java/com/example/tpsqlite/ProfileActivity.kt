package com.example.tpsqlite

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnBack: Button
    private lateinit var btnChangePhoto: Button
    private lateinit var btnTakePhoto: Button
    private lateinit var btnChoosePhoto: Button
    private lateinit var ivProfilePhoto: ImageView
    private lateinit var db: DataHelper

    private var currentPhotoUri: String = ""
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_PICK_IMAGE = 2
    private val REQUEST_PERMISSION_READ_STORAGE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvName = findViewById(R.id.tvProfileName)
        tvEmail = findViewById(R.id.tvProfileEmail)
        btnBack = findViewById(R.id.btnBackHome)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto)
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto)

        db = DataHelper(this)

        val user = db.getCurrentUser()
        if (user != null) {
            tvName.text = "Nom : ${user.name}"
            tvEmail.text = "Email : ${user.email}"

            // Charger la photo de profil si elle existe
            if (user.profilePhotoUri.isNotEmpty()) {
                currentPhotoUri = user.profilePhotoUri
                try {
                    ivProfilePhoto.setImageURI(Uri.parse(currentPhotoUri))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Bouton Prendre photo (caméra)
        btnTakePhoto.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                val photoFile = createImageFile()
                val photoURI = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    photoFile
                )
                currentPhotoUri = photoURI.toString()
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } else {
                Toast.makeText(this, "Aucune appli caméra trouvée", Toast.LENGTH_SHORT).show()
            }
        }

        // Bouton Choisir photo (galerie)
        btnChoosePhoto.setOnClickListener {
            if (checkStoragePermission()) {
                openGallery()
            } else {
                requestStoragePermission()
            }
        }

        // Bouton Sauvegarder la photo
        btnChangePhoto.setOnClickListener {
            val user = db.getCurrentUser()
            if (user != null && currentPhotoUri.isNotEmpty()) {
                val success = db.updateUserProfilePhoto(user.email, currentPhotoUri)
                if (success) {
                    Toast.makeText(this, "Photo de profil mise à jour", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Aucune photo sélectionnée", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                REQUEST_PERMISSION_READ_STORAGE
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_READ_STORAGE
            )
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    try {
                        ivProfilePhoto.setImageURI(Uri.parse(currentPhotoUri))
                        Toast.makeText(this, "Photo prise avec succès", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                REQUEST_PICK_IMAGE -> {
                    val selectedImage = data?.data
                    selectedImage?.let { uri ->
                        currentPhotoUri = uri.toString()
                        try {
                            ivProfilePhoto.setImageURI(uri)
                            Toast.makeText(this, "Photo sélectionnée", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION_READ_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                }
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile(
            "PROFILE_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoUri = Uri.fromFile(this).toString()
        }
    }
}