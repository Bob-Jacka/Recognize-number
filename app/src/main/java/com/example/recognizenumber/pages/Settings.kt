package com.example.recognizenumber.pages

import GlobalSettings
import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.recognizenumber.R
import java.io.File

class Settings : AppCompatActivity() {

    private lateinit var change_model_btn: Button
    private lateinit var go_back_btn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        change_model_btn = findViewById(R.id.ChangeModelBtn)
        go_back_btn = findViewById(R.id.goBackBtn)
        registerForContextMenu(change_model_btn)
        go_back_btn.setOnClickListener { goBack() }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        val dir = File("app/NeuroNet").listFiles()
        menu.setHeaderTitle("Choose neuro model")
        for (file in dir!!) {
            menu.add(file.name)
        }
        super.onCreateContextMenu(menu, v, menuInfo)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        GlobalSettings.current_model = item.title.toString()
        return super.onContextItemSelected(item)
    }

    private fun goBack() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}