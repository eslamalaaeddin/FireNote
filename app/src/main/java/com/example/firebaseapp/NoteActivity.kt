package com.example.firebaseapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.note_layout.*

private const val TITLE = "title"
private const val NOTE = "note"

class NoteActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var noteTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        titleTextView = findViewById(R.id.title_text_view)
        noteTextView =  findViewById(R.id.note_text_view)

        val intent = intent
        val title = intent.getStringExtra(TITLE)
        val note = intent.getStringExtra(NOTE)

        titleTextView.text = title
        noteTextView.text = note
    }
}