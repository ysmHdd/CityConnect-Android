package com.example.tpsqlite

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
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

class AddReportActivity : AppCompatActivity() {

    private lateinit var db: DataHelper
    private var isEditMode = false
    private var originalTitre = ""
    private var currentPhotoUri: String = ""

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_PICK_IMAGE = 2
    private val REQUEST_PERMISSION_READ_STORAGE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_report)

        val etTitre = findViewById<EditText>(R.id.etTitre)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val spinnerCategorie = findViewById<Spinner>(R.id.spinnerCategorie)
        val etPriorite = findViewById<EditText>(R.id.etPriorite)
        val btnSave = findViewById<Button>(R.id.btnSaveReport)
        val btnCancel = findViewById<Button>(R.id.btnCancel)
        val btnTakePhoto = findViewById<Button>(R.id.btnTakePhoto)
        val btnChoosePhoto = findViewById<Button>(R.id.btnChoosePhoto)
        val ivReportPhoto = findViewById<ImageView>(R.id.ivReportPhoto)

        db = DataHelper(this)

        // Configurer les catégories
        val categories = arrayOf(
            "infrastructure",
            "environnement",
            "securite",
            "proprete",
            "urbanisme",
            "eau",
            "transport",
            "autre"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategorie.adapter = adapter

        // Vérifier si on est en mode édition
        val titre = intent.getStringExtra("titre")
        val description = intent.getStringExtra("description")
        val categorie = intent.getStringExtra("categorie")
        val priorite = intent.getIntExtra("priorite", 3)
        val photoUri = intent.getStringExtra("photoUri")

        if (titre != null && titre.isNotEmpty()) {
            // MODE ÉDITION
            isEditMode = true
            originalTitre = titre

            // Pré-remplir les champs avec les données existantes
            etTitre.setText(titre)
            etDescription.setText(description ?: "")
            etPriorite.setText(priorite.toString())

            // Désactiver le champ titre (c'est la clé primaire)
            etTitre.isEnabled = false
            etTitre.setBackgroundColor(Color.parseColor("#333333"))

            // Charger la photo existante si elle existe
            if (!photoUri.isNullOrEmpty()) {
                currentPhotoUri = photoUri
                try {
                    ivReportPhoto.setImageURI(Uri.parse(photoUri))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Sélectionner la bonne catégorie dans le spinner
            val position = categories.indexOf(categorie)
            if (position >= 0) {
                spinnerCategorie.setSelection(position)
            }

            // Changer le texte du bouton
            btnSave.text = "Mettre à jour"
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
            // Vérifier et demander la permission si nécessaire
            if (checkStoragePermission()) {
                openGallery()
            } else {
                requestStoragePermission()
            }
        }

        btnSave.setOnClickListener {
            val newTitre = etTitre.text.toString().trim()
            val newDescription = etDescription.text.toString().trim()
            val newCategorie = spinnerCategorie.selectedItem.toString()
            val newPriorite = etPriorite.text.toString().toIntOrNull() ?: 3

            // Validation
            if (newTitre.isEmpty() || newDescription.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPriorite < 1 || newPriorite > 5) {
                Toast.makeText(this, "Priorité doit être entre 1 et 5", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val report = Report(
                titre = newTitre,
                description = newDescription,
                categorie = newCategorie,
                priorite = newPriorite,
                photoUri = currentPhotoUri
            )

            if (isEditMode) {
                // MODE ÉDITION : Mettre à jour le report existant
                val success = db.updateReport(report)

                if (success) {
                    Toast.makeText(this, "Report mis à jour avec succès", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show()
                }
            } else {
                // MODE AJOUT : Créer un nouveau report
                val success = db.addReport(report)

                if (!success) {
                    Toast.makeText(this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Retourner à l'écran précédent
            finish()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ utilise READ_MEDIA_IMAGES
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android < 13 utilise READ_EXTERNAL_STORAGE
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
        val ivReportPhoto = findViewById<ImageView>(R.id.ivReportPhoto)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    // Photo prise avec la caméra
                    try {
                        ivReportPhoto.setImageURI(Uri.parse(currentPhotoUri))
                        Toast.makeText(this, "Photo prise avec succès", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this, "Erreur de chargement de la photo", Toast.LENGTH_SHORT).show()
                    }
                }
                REQUEST_PICK_IMAGE -> {
                    // Photo choisie depuis la galerie
                    val selectedImage = data?.data
                    selectedImage?.let { uri ->
                        currentPhotoUri = uri.toString()
                        try {
                            ivReportPhoto.setImageURI(uri)
                            Toast.makeText(this, "Photo sélectionnée", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show()
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
                } else {
                    Toast.makeText(
                        this,
                        "Permission refusée. Vous ne pourrez pas choisir de photos.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoUri = Uri.fromFile(this).toString()
        }
    }
}