package com.example.skins

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
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
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore


class HomeFragment : Fragment() {

    private lateinit var avatarImageView: ImageView
    private lateinit var emailTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var send: Button
    private lateinit var tradeUrl: EditText


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_home, container, false)
        emailTextView = view.findViewById(R.id.emailTextView)
        tradeUrl = view.findViewById(R.id.trade)
        avatarImageView = view.findViewById(R.id.avatarImageView)
        send = view.findViewById(R.id.seend)
        avatarImageView.setOnClickListener {
            selectImageFromGallery()
        }
        send.setOnClickListener{
            addUrl()
        }
        getUserProfile()
        return view
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

    private fun addUrl(){
        val email = Firebase.auth.currentUser?.email
        val trdUrl = tradeUrl.text.toString().trim()
        if (trdUrl.isEmpty()) {
            tradeUrl.error = "Trade URL не может быть пустым"
            return
        }
        val db = Firebase.firestore
        auth = Firebase.auth
        val user = hashMapOf(
            "email" to email,
            "Trade URL" to trdUrl
        )
        db.collection("Trade URL")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                Toast.makeText(context, "Trade URL добавлен успешно", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                Toast.makeText(context, "Ошибка добавления Trade URL", Toast.LENGTH_SHORT).show()
            }
    }


}