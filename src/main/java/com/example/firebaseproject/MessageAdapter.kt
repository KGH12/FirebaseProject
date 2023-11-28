package com.example.firebaseproject

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.QueryDocumentSnapshot


data class Message(
    val product: String,
    val messaage: String,
    val seller: String,
    val buyer: String,
    val time: String
) {
    constructor(doc: QueryDocumentSnapshot) :
            this(
                doc["product"].toString(), doc["message"].toString(), doc["seller"].toString(),
                doc["buyer"].toString(), doc["time"].toString()
            )

    constructor(key: String, map: Map<*, *>) :
            this(
                map["product"].toString(), map["message"].toString(), map["seller"].toString(),
                map["buyer"].toString(), map["time"].toString()
            )
}

class MessageViewHolder(val view: View) : RecyclerView.ViewHolder(view)

class MessageAdapter(private val context: Context, private var messages: List<Message>) :
    RecyclerView.Adapter<MessageViewHolder>() {

    fun updateList(newList: List<Message>) {
        messages = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.view.findViewById<TextView>(R.id.textViewMessageBuyer).text = message.buyer
        holder.view.findViewById<TextView>(R.id.textViewMessageTime).text = message.time
        holder.view.findViewById<TextView>(R.id.textViewMessageProduct).text = message.product
        holder.view.findViewById<TextView>(R.id.textViewMessageMessage).text = message.messaage
    }

    override fun getItemCount() = messages.size
}