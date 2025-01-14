package com.example.skins

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Rect
import android.media.RouteListingPreference
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL


class HomeFragment : Fragment() {

    private lateinit var avatarImageView: ImageView
    private lateinit var emailTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var send: Button
    private lateinit var loading: Button
    private lateinit var perID: EditText
    private lateinit var skinsAdapter: AdapterInventory
    private val datasett = arrayListOf<YourinventClass>()
    private var permanentId: String? = null
    private var isLoading = false


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_home, container, false)

        val db = Firebase.firestore
        auth = FirebaseAuth.getInstance()


        val recyclerView: RecyclerView = view.findViewById(R.id.recycle)
        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.layoutManager = gridLayoutManager
        skinsAdapter = AdapterInventory(datasett)
        recyclerView.adapter = skinsAdapter

        val spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing)
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                val column = position % 2
                outRect.left = spacing - column * spacing / 2
                outRect.right = (column + 1) * spacing / 2
                if (position < 2) outRect.top = spacing
                outRect.bottom = spacing
            }
        })
        emailTextView = view.findViewById(R.id.emailTextView)
        loading = view.findViewById(R.id.loading)
        perID = view.findViewById(R.id.trade)
        avatarImageView = view.findViewById(R.id.avatarImageView)
        send = view.findViewById(R.id.seend)
        avatarImageView.setOnClickListener {
            selectImageFromGallery()
        }
        send.setOnClickListener{
            addUrl()
        }
        loading.setOnClickListener{
            loadSkins()
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && lastVisibleItemPosition >= totalItemCount - 1) {
                    loadSkins()
                }
            }
        })
        getUserProfile()
        return view
    }

    // Функция для получения Permanent ID из Firebase
    private suspend fun getPermanentIdFromFirebase(): String? {
        val userId = auth.currentUser?.uid ?: return null
        val db = Firebase.firestore

        return try {
            val querySnapshot = db.collection("Permanent ID")
                .whereEqualTo("User", userId)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                querySnapshot.documents[0].getString("Permanent ID")
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка получения Permanent ID: $e")
            null
        }
    }

    private fun loadSkins() {
        isLoading = true
        lifecycleScope.launch {
            try {
                val permanentId = getPermanentIdFromFirebase()

                if (permanentId != null) {
                    val url = "https://steamcommunity.com/inventory/$permanentId/730/2"
                    val response = withContext(Dispatchers.IO) {
                        fetchSkinsData(url)
                    }

                    if (response != null) {
                        val skins = parseSkinsFromResponse(response)
                        withContext(Dispatchers.Main) {
                            datasett.addAll(skins)
                            skinsAdapter.notifyDataSetChanged()
                            isLoading = false
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
                            isLoading = false
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Не удалось получить Permanent ID", Toast.LENGTH_SHORT).show()
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Ошибка: $e")
                    Toast.makeText(requireContext(), "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
            }
        }
    }

    private suspend fun fetchSkinsData(url: String): String? {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка подключения: $e")
            null
        }
    }


    private fun parseSkinsFromResponse(response: String): List<YourinventClass> {
        val skins = arrayListOf<YourinventClass>()
        val jsonObject = JSONObject(response)
        val results = jsonObject.getJSONArray("results")
        for (i in 0 until results.length()) {
            val item = results.getJSONObject(i)
            val assetDescription = item.optJSONObject("asset_description") ?: continue

            val title = item.optString("name", "Unknown Item")
            val exterior = extractType(assetDescription)
            val cost = item.optString("sale_price_text", "N/A")
            skins.add(YourinventClass(R.drawable.cssss, title, exterior, cost)) // Убедитесь, что добавляете объекты Skins
        }
        return skins
    }

    private fun extractType(assetDescription: JSONObject): String {
        return assetDescription.optString("type", "Unknown")
    }




    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            avatarImageView.setImageURI(selectedImageUri)
        }
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
    }

    private fun getUserProfile() {
        // Получаем текущего пользователя
        val user = Firebase.auth.currentUser
        user?.let {
            // Получаем данные пользователя
            val email = it.email
            val photoUrl = it.photoUrl

            // Обновляем UI
            emailTextView.text = email // Устанавливаем email в TextView

            // Если у пользователя есть аватар, загружаем его в ImageView
            if (photoUrl != null) {
                Glide.with(this)
                    .load(photoUrl) // Загружаем изображение по URL
                    .into(avatarImageView) // Устанавливаем в ImageView
            } else {
                // Если аватар отсутствует, можно установить изображение по умолчанию
                avatarImageView.setImageResource(R.drawable.satoru)
            }
        }
    }

    private fun addUrl() {
        val userId = auth.currentUser?.uid ?: return
        val email = Firebase.auth.currentUser?.email
        val permID = perID.text.toString().trim()

        if (permID.isEmpty()) {
            perID.error = "Permanent ID не может быть пустым"
            return
        }

        val db = Firebase.firestore
        auth = Firebase.auth

        val user = hashMapOf(
            "Email" to email,
            "Permanent ID" to permID,
            "User" to userId
        )

        // Проверка на существующий Permanent ID
        db.collection("Permanent ID")
            .whereEqualTo("User", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Обновляем существующий документ
                    val documentId = querySnapshot.documents[0].id
                    db.collection("Permanent ID").document(documentId)
                        .set(user)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Permanent ID обновлен", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Ошибка обновления: $e")
                        }
                } else {
                    // Добавляем новый документ
                    db.collection("Permanent ID")
                        .add(user)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Permanent ID добавлен", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Ошибка добавления: $e")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Ошибка проверки существующего ID: $e")
            }
    }



}