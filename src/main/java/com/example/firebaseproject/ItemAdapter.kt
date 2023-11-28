package com.example.firebaseproject

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.ktx.Firebase
import android.widget.ImageView
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage


data class Item(
    val id: String,
    val sellerEmail: String,
    val name: String,
    val price: Int,
    val inStock: Boolean,
    val desc: String,
    val imageAddress: String
) {
    constructor(doc: QueryDocumentSnapshot) :
            this(
                doc.id, doc["sellerEmail"].toString(), doc["name"].toString(),
                doc["price"].toString().toIntOrNull() ?: 0, doc["inStock"].toString().toBoolean(),
                doc["description"].toString(), doc["imageAddress"].toString()
            )

    constructor(key: String, map: Map<*, *>) :
            this(
                key,
                map["sellerEmail"].toString(),
                map["name"].toString(),
                map["price"].toString().toIntOrNull() ?: 0,
                map["inStock"].toString().toBoolean(),
                map["description"].toString(),
                map["imageAddress"].toString()
            )
}

class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view)

class ItemAdapter(private val context: Context, private var items: List<Item>) :
    RecyclerView.Adapter<ItemViewHolder>() {

    lateinit var storage: FirebaseStorage

    fun interface OnItemClickListener {
        fun onItemClick(productId: String)
    }

    private var itemClickListener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    fun updateList(newList: List<Item>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.view.findViewById<TextView>(R.id.textSellerName).text = item.sellerEmail
        holder.view.findViewById<TextView>(R.id.textProductName).text = item.name
        holder.view.findViewById<TextView>(R.id.textInStock).text = item.inStock.toString()
        if (item.inStock) {
            holder.view.findViewById<TextView>(R.id.textInStock).text = "재고 있음"
        } else {
            holder.view.findViewById<TextView>(R.id.textInStock).text = "판매 완료"
        }

        holder.view.findViewById<TextView>(R.id.textPrice).text = "₩ ${item.price}"
        val imageView = holder.view.findViewById<ImageView>(R.id.imageViewItem)

        storage = Firebase.storage
        val storageRef = storage.reference
        val imageRef1 = storageRef.child(item.imageAddress)
        displayImageRef(imageRef1, imageView)

        holder.view.findViewById<TextView>(R.id.textSellerName).setOnClickListener {
            itemClickListener?.onItemClick(item.id)
        }
        holder.view.findViewById<TextView>(R.id.textProductName).setOnClickListener {
            itemClickListener?.onItemClick(item.id)
        }
        holder.view.findViewById<TextView>(R.id.textInStock).setOnClickListener {
            itemClickListener?.onItemClick(item.id)
        }
        holder.view.findViewById<TextView>(R.id.textPrice).setOnClickListener {
            itemClickListener?.onItemClick(item.id)
        }
        holder.view.findViewById<ImageView>(R.id.imageViewItem).setOnClickListener {
            itemClickListener?.onItemClick(item.id)
        }
    }

    override fun getItemCount() = items.size

    private fun displayImageRef(imageRef: StorageReference?, view: ImageView) {
        imageRef?.getBytes(Long.MAX_VALUE)?.addOnSuccessListener {
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            view.setImageBitmap(bmp)
        }?.addOnFailureListener {
        }
    }
}