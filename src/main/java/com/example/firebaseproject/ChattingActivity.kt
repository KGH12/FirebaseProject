package com.example.firebaseproject

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChattingActivity : AppCompatActivity() {

    private val db: FirebaseFirestore = Firebase.firestore
    private val itemsCollectionRef = db.collection("items")
    private val messagesCollectionRef = db.collection("messages")

    private val editTextChatMessage by lazy { findViewById<EditText>(R.id.editTextChatMessage) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatting)

        val productId = intent.getStringExtra("productId").toString()

        var product = "No product"
        var seller = "No seller"
        var buyer = Firebase.auth.currentUser?.email ?: "No buyer"

        itemsCollectionRef.document(productId).get().addOnSuccessListener {
            product = it["name"].toString()
            seller = it["sellerEmail"].toString()
        }.addOnFailureListener { }

        // 채팅 보내기 벼튼
        findViewById<Button>(R.id.buttonChatSend)?.setOnClickListener {
            addMessage(product, seller, buyer)
        }

        // 뒤로가기 버튼
        findViewById<Button>(R.id.buttonChatBack)?.setOnClickListener {
            finish()
        }
    }

    private fun addMessage(product: String, seller: String, buyer: String) {

        val message = editTextChatMessage.text.toString()
        if (message.isEmpty()) {
            Snackbar.make(editTextChatMessage, "Input message!", Snackbar.LENGTH_SHORT).show()
            return
        }

        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val time = currentDateTime.format(formatter)

        val messageMap = hashMapOf(
            "product" to product,
            "message" to message,
            "seller" to seller,
            "buyer" to buyer,
            "time" to time,
        )

        messagesCollectionRef.add(messageMap).addOnSuccessListener {
            Toast.makeText(this, "Message successful", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { }
    }
}