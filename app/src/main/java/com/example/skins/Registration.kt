package com.example.skins

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.util.regex.Pattern

class Registration : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var login: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confPassword: EditText
    private lateinit var signup: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        val db = Firebase.firestore
        auth = Firebase.auth

        login = findViewById(R.id.login)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        confPassword = findViewById(R.id.confpassword)

        // Кнопка регистрации
        signup = findViewById(R.id.signup)

        // Устанавливаем обработчик нажатия
        signup.setOnClickListener {
            registerUser() // Вызов вашей функции при нажатии на кнопку
        }
    }

    private fun registerUser() {
        val inputLogin = login.text.toString().trim()
        val inputEmail = email.text.toString().trim()
        val inputPassword = password.text.toString().trim()
        val inputConfPassword = confPassword.text.toString().trim()

        if (!isValidLogin(inputLogin)) {
            login.error = "Некорректный login"
            Toast.makeText(this, "Недопустимые символы", Toast.LENGTH_SHORT).show()
            return
        }

        //проверка на правильность email
        if (!isValidEmail(inputEmail)) {
            email.error = "Некорректный email"
            Toast.makeText(this, "Email введен неправильно", Toast.LENGTH_SHORT).show()
            return
        }

        //проверка на кол-во символов
        if (inputPassword.length < 6) {
            Toast.makeText(this, "Пароль меньше 6 символов", Toast.LENGTH_SHORT).show()
            return
        }

        //проверка на совпадение паролей
        if (inputPassword != inputConfPassword) {
            confPassword.error = "Пароль не совпадает"
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
            return
        }

        // Регистрация пользователя в Firebase Authentication
        createAccount(inputEmail, inputPassword)

        val intent = Intent(this, Login::class.java)
        startActivity(intent)
    }

    private fun isValidLogin(login: String): Boolean {
        // Регулярное выражение для проверки допустимых символов
        val forbiddenCharactersPattern = "[\\-;:'\",]+"
        val regex = Regex(forbiddenCharactersPattern)
        return !regex.containsMatchIn(login) // Возвращает true, если запрещенных символов нет
    }


    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
    }

    companion object {
        private const val TAG = "EmailPassword"
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        val pattern = Pattern.compile(emailPattern)
        return pattern.matcher(email).matches()
    }

    fun back(v: View){
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
    }
}