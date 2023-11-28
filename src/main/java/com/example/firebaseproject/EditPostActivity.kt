package com.example.firebaseproject

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage


class EditPostActivity : AppCompatActivity() {

    lateinit var storage: FirebaseStorage
    private val db: FirebaseFirestore = Firebase.firestore
    private val itemsCollectionRef = db.collection("items")

    private val imageViewProduct by lazy { findViewById<ImageView>(R.id.imageViewEditPost) }
    private val textViewProductName by lazy { findViewById<TextView>(R.id.textViewProductName) }
    private val editPostPrice by lazy { findViewById<EditText>(R.id.editPostPrice) }
    private val checkBoxEditPostInStock by lazy { findViewById<CheckBox>(R.id.checkBoxEditPostInStock) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editpost)

        Firebase.auth.currentUser ?: finish()

        val productId = intent.getStringExtra("productId").toString()

        itemsCollectionRef.document(productId).get().addOnSuccessListener {
            textViewProductName.setText(it["name"].toString())
            editPostPrice.setText(it["price"].toString())
            checkBoxEditPostInStock.isChecked = it["inStock"].toString().toBoolean()
            storage = Firebase.storage
            val storageRef = storage.reference
            val imageRef = storageRef.child(it["imageAddress"].toString())
            displayImageRef(imageRef, imageViewProduct)
        }

        // 업데이트 확인 버튼
        findViewById<Button>(R.id.buttonUpdatePost)?.setOnClickListener {
            updateItem(productId)
        }

        // 뒤로가기 버튼
        findViewById<Button>(R.id.buttonCancelPost)?.setOnClickListener {
            finish()
        }
    }

    // db에 아이템 정보 업데이트
    private fun updateItem(productId: String) {
        val price = editPostPrice.text.toString()
        val inStock = checkBoxEditPostInStock.isChecked

        if (price.isEmpty()) {
            Snackbar.make(editPostPrice, "Input price!", Snackbar.LENGTH_SHORT).show()
            return
        }

        // price, instock 둘 중 하나만 업데이트되는 일을 방지하기 위해 Transaction으로 처리
        db.runTransaction {
            itemsCollectionRef.document(productId).update("price", price.toInt())
                .addOnSuccessListener { }
            itemsCollectionRef.document(productId).update("inStock", inStock)
                .addOnSuccessListener { }
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun displayImageRef(imageRef: StorageReference?, view: ImageView) {
        imageRef?.getBytes(Long.MAX_VALUE)?.addOnSuccessListener {
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            view.setImageBitmap(bmp)
        }?.addOnFailureListener {
        }
    }
}