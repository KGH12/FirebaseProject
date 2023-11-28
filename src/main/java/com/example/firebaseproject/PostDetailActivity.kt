package com.example.firebaseproject

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class PostDetailActivity : AppCompatActivity() {

    lateinit var storage: FirebaseStorage
    private val db: FirebaseFirestore = Firebase.firestore
    private val itemsCollectionRef = db.collection("items")

    private val imageViewDetail by lazy { findViewById<ImageView>(R.id.imageViewDetail) }
    private val textViewDetailName by lazy { findViewById<TextView>(R.id.textViewDetailName) }
    private val textViewDetailPrice by lazy { findViewById<TextView>(R.id.textViewDetailPrice) }
    private val textViewDetailDesc by lazy { findViewById<TextView>(R.id.textViewDetailDesc) }
    private val textViewDetailInstock by lazy { findViewById<TextView>(R.id.textViewDetailInstock) }
    private val textViewDetailSellerEmail by lazy { findViewById<TextView>(R.id.textViewDetailSellerEmail) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_postdetail)

        Firebase.auth.currentUser ?: finish()

        // 메인액티비티로부터 선택된 상품의 id를 받는다
        val productId = intent.getStringExtra("productId").toString()

        itemsCollectionRef.document(productId).get().addOnSuccessListener {
            textViewDetailName.setText(it["name"].toString())
            textViewDetailPrice.setText(it["price"].toString())
            textViewDetailDesc.setText(it["description"].toString())
            textViewDetailSellerEmail.setText(it["sellerEmail"].toString())
            if (it["inStock"].toString().toBoolean()) textViewDetailInstock.setText("재고 있음")
            else textViewDetailInstock.setText("판매 완료")
            storage = Firebase.storage
            val storageRef = storage.reference
            val imageRef1 = storageRef.child(it["imageAddress"].toString())
            displayImageRef(imageRef1, imageViewDetail)
        }

        // 판매자에게 메시지를 보내러 가는 버튼
        findViewById<Button>(R.id.buttonChat)?.setOnClickListener {
            startActivity(Intent(this, ChattingActivity::class.java).apply {
                putExtra("productId", productId)
            })
        }

        // 뒤로가기 버튼
        findViewById<Button>(R.id.buttonDetailBack)?.setOnClickListener {
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