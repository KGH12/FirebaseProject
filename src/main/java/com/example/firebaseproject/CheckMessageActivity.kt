package com.example.firebaseproject

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CheckMessageActivity : AppCompatActivity() {

    private val db: FirebaseFirestore = Firebase.firestore
    private val messagesCollectionRef = db.collection("messages")

    private var adapter: MessageAdapter? = null
    private val recyclerViewMessages by lazy { findViewById<RecyclerView>(R.id.recyclerViewMessages) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_checkmessage)

        recyclerViewMessages.layoutManager = LinearLayoutManager(this)
        adapter = MessageAdapter(this, emptyList())
        recyclerViewMessages.adapter = adapter

        updateList()

        findViewById<Button>(R.id.buttonCheckCancel).setOnClickListener {
            finish()
        }
    }

    private fun updateList() {
        messagesCollectionRef
            .whereEqualTo("seller", Firebase.auth.currentUser?.email)
            .get()
            .addOnSuccessListener {
                val messages = mutableListOf<Message>()
                for (doc in it) {
                    messages.add(Message(doc))
                }
                adapter?.updateList(messages)
            }
    }
}