package com.example.firebaseproject

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage


class CreatePostActivity : AppCompatActivity() {
    lateinit var storage: FirebaseStorage
    private val db: FirebaseFirestore = Firebase.firestore
    private val itemsCollectionRef = db.collection("items")
    private var uploadFileName: String = NOIMAGE_ADDRESS

    private val imageView2 by lazy { findViewById<ImageView>(R.id.imageViewCheck) }
    private val editProductName by lazy { findViewById<EditText>(R.id.editProductName) }
    private val editDesc by lazy { findViewById<EditText>(R.id.editDesc) }
    private val editPrice by lazy { findViewById<EditText>(R.id.editPrice) }


    companion object {
        const val REQUEST_CODE = 1
        const val UPLOAD_FOLDER = "upload_images/"
        const val NOIMAGE_ADDRESS = "noImage.jpg"
    }

    private val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_createpost)

        Firebase.auth.currentUser ?: finish()

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
            requestSinglePermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        else
            requestSinglePermission(Manifest.permission.READ_MEDIA_IMAGES)

        storage = Firebase.storage
        val imageRef2 =
            storage.getReferenceFromUrl("gs://fir-project-8df4e.appspot.com/upload_images/noImage.jpg")
        displayImageRef(imageRef2, imageView2)


        findViewById<Button>(R.id.buttonUploadImage)?.setOnClickListener {
            uploadDialog()
        }

        findViewById<Button>(R.id.buttonPost)?.setOnClickListener {
            addItem()
        }

        findViewById<Button>(R.id.buttonCreateCancel)?.setOnClickListener {
            finish()
        }
    }

    private fun requestSinglePermission(permission: String) {
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
            return

        val requestPermLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (it == false) { // permission is not granted!
                    AlertDialog.Builder(this).apply {
                        setTitle("Warning")
                        setMessage("permission required!")
                    }.show()
                }
            }

        if (shouldShowRequestPermissionRationale(permission)) {
            // you should explain the reason why this app needs the permission.
            AlertDialog.Builder(this).apply {
                setTitle("Reason")
                setMessage("permission required!")
                setPositiveButton("Allow") { _, _ -> requestPermLauncher.launch(permission) }
                setNegativeButton("Deny") { _, _ -> }
            }.show()
        } else {
            requestPermLauncher.launch(permission)
        }
    }

    private fun hasPermission(permission: String) =
        checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    private fun uploadDialog() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            if (!hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) return
        } else {
            if (!hasPermission(Manifest.permission.READ_MEDIA_IMAGES)) return
        }

        val cursor = contentResolver.query(
            collection,
            null, null, null, null
        )

        AlertDialog.Builder(this)
            .setTitle("Choose Photo")
            .setCursor(cursor, { _, i ->
                cursor?.run {
                    moveToPosition(i)
                    val idIdx = getColumnIndex(MediaStore.Images.ImageColumns._ID)
                    val nameIdx = getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)
                    uploadFile(getLong(idIdx), getString(nameIdx))
                    uploadFileName = getString(nameIdx)
                }
            }, MediaStore.Images.ImageColumns.DISPLAY_NAME).create().show()
    }

    private fun uploadFile(file_id: Long?, fileName: String?) {
        file_id ?: return
        val imageRef = storage.reference.child("${UPLOAD_FOLDER}${fileName}")
        val contentUri = ContentUris.withAppendedId(collection, file_id)
        imageRef.putFile(contentUri).addOnCompleteListener {
            if (it.isSuccessful) {
                Snackbar.make(imageView2, "Upload completed.", Snackbar.LENGTH_SHORT).show()
                displayImageRef(imageRef, imageView2)
            }
        }
    }

    private fun displayImageRef(imageRef: StorageReference?, view: ImageView) {
        imageRef?.getBytes(Long.MAX_VALUE)?.addOnSuccessListener {
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            view.setImageBitmap(bmp)
        }?.addOnFailureListener { }
    }

    private fun addItem() {
        val name = editProductName.text.toString()
        if (name.isEmpty()) {
            Snackbar.make(editProductName, "Input name!", Snackbar.LENGTH_SHORT).show()
            return
        }
        val price = editPrice.text.toString()
        if (price.isEmpty()) {
            Snackbar.make(editPrice, "Input price!", Snackbar.LENGTH_SHORT).show()
            return
        }
        val desc = editDesc.text.toString()
        if (desc.isEmpty()) {
            Snackbar.make(editDesc, "Input description!", Snackbar.LENGTH_SHORT).show()
            return
        }
        val sellerName = Firebase.auth.currentUser?.email ?: "No User"
        val imageAddress = "${UPLOAD_FOLDER}${uploadFileName}"
        val itemMap = hashMapOf(
            "sellerEmail" to sellerName,
            "name" to name,
            "description" to desc,
            "price" to price.toInt(),
            "inStock" to true,
            "imageAddress" to imageAddress
        )

        itemsCollectionRef.add(itemMap).addOnSuccessListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }.addOnFailureListener { }
    }
}