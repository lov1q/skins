package com.example.skins

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun onClick(v: View){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
    fun sighUp(v: View){
        val intent = Intent(this, Registration::class.java)
        startActivity(intent)
    }
}