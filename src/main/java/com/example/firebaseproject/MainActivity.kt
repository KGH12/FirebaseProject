package com.example.firebaseproject


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {
    private var adapter: ItemAdapter? = null
    private val db: FirebaseFirestore = Firebase.firestore
    private val itemsCollectionRef = db.collection("items")
    private val usersCollectionRef = db.collection("users")
    private var currentUser = ""

    private val recyclerViewItems by lazy { findViewById<RecyclerView>(R.id.recyclerViewItems) }
    private val editMinPrice by lazy { findViewById<EditText>(R.id.editMinPrice) }
    private val editMaxPrice by lazy { findViewById<EditText>(R.id.editMaxPrice) }
    private val checkBoxInStock by lazy { findViewById<CheckBox>(R.id.checkBoxInStock) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 로그인 상태 확인
        currentUser = Firebase.auth.currentUser?.uid ?: "No User"
        if (Firebase.auth.currentUser?.uid == null) {
            startActivity(
                Intent(this, LoginActivity::class.java)
            )
            finish()
        }


        // 환영 메시지 표시
        usersCollectionRef
            .whereEqualTo("uid", currentUser)
            .get()
            .addOnSuccessListener {
                for (doc in it) {
                    findViewById<TextView>(R.id.textUsername).setText("${doc["userName"].toString()} 님 환영합니다.")
                }
            }


        // 로그아웃 버튼
        findViewById<Button>(R.id.button_signout)?.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(
                Intent(this, LoginActivity::class.java)
            )
            finish()
        }


        // 메시지 확인 액티비티로 이동 버튼
        findViewById<Button>(R.id.buttonCheckMessages)?.setOnClickListener {
            startActivity(Intent(this, CheckMessageActivity::class.java))
        }


        // 글 작성 버튼
        findViewById<Button>(R.id.buttonCreatePost).setOnClickListener {
            startActivity(Intent(this, CreatePostActivity::class.java))
        }


        // 필터 버튼
        findViewById<Button>(R.id.buttonApply).setOnClickListener {
            productsFilter()
        }


        // 리사이클러뷰 세팅
        // 클릭한 상품의 판매자 이메일과, 현재 접속중인 유저의 이메일이 같으면 -> EditPostActivity로 이동
        // 다르면 -> PostDeatilActivity로 이동
        recyclerViewItems.layoutManager = LinearLayoutManager(this)
        adapter = ItemAdapter(this, emptyList())
        adapter?.setOnItemClickListener {
            itemsCollectionRef.document(it).get().addOnSuccessListener {
                if (it["sellerEmail"] == Firebase.auth.currentUser?.email) {
                    startActivity(Intent(this, EditPostActivity::class.java).apply {
                        putExtra("productId", it.id)
                    })
                } else {
                    startActivity(Intent(this, PostDetailActivity::class.java).apply {
                        putExtra("productId", it.id)
                    })
                }
            }.addOnFailureListener { }
        }
        recyclerViewItems.adapter = adapter

        // 리사이클러뷰 갱신
        updateList()
    }

    // 리사이클러뷰 데이터베이스 전부 뜨도록 갱신
    private fun updateList() {
        itemsCollectionRef.get().addOnSuccessListener {
            val items = mutableListOf<Item>()
            for (doc in it) {
                items.add(Item(doc))
            }
            adapter?.updateList(items)
        }
    }

    // 리사이클러뷰 데이터베이스 중 일부 Item만 뜨도록 갱신
    private fun updateFilteredList(items: MutableList<Item>) {
        itemsCollectionRef.get().addOnSuccessListener {
            adapter?.updateList(items)
        }
    }

    // 필터 기능을 이용하여 리사이클러뷰 갱신
    // updateFilteredList() 이용
    private fun productsFilter() {
        val isInStock = checkBoxInStock.isChecked
        val minPrice = editMinPrice.text.toString().toIntOrNull() ?: 0
        val maxPrice = editMaxPrice.text.toString().toIntOrNull() ?: 2147483647 // 최대 21억

        if (isInStock == false) { // false -> 재고 있는 상품, 재고 없는 상품 모두 표시
            itemsCollectionRef
                .whereLessThanOrEqualTo("price", maxPrice)
                .whereGreaterThanOrEqualTo("price", minPrice)
                .get()
                .addOnSuccessListener {
                    val items1: MutableList<Item> = mutableListOf()
                    for (doc in it) {
                        items1.add(
                            Item(
                                doc.id,
                                doc["sellerEmail"].toString(),
                                doc["name"].toString(),
                                doc["price"].toString().toInt(),
                                doc["inStock"].toString().toBoolean(),
                                doc["description"].toString(),
                                doc["imageAddress"].toString()
                            )
                        )
                    }
                    updateFilteredList(items1)
                }
                .addOnFailureListener { }
        } else {     // true -> 재고 있는 상품만 표시
            itemsCollectionRef
                .whereLessThanOrEqualTo("price", maxPrice)
                .whereGreaterThanOrEqualTo("price", minPrice)
                .get()
                .addOnSuccessListener {
                    val items2: MutableList<Item> = mutableListOf()
                    for (doc in it) {
                        if (doc["inStock"] == true) {
                            items2.add(
                                Item(
                                    doc.id,
                                    doc["sellerEmail"].toString(),
                                    doc["name"].toString(),
                                    doc["price"].toString().toInt(),
                                    doc["inStock"].toString().toBoolean(),
                                    doc["description"].toString(),
                                    doc["imageAddress"].toString()
                                )
                            )
                        }
                    }
                    updateFilteredList(items2)
                }
                .addOnFailureListener { }
        }
    }

}