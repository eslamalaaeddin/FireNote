package com.example.firebaseapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.add_note_dialog_layout.view.*
import kotlinx.android.synthetic.main.add_note_dialog_layout.view.note_edit_text
import kotlinx.android.synthetic.main.add_note_dialog_layout.view.title_edit_text
import kotlinx.android.synthetic.main.update_note_dialog_layout.view.*
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "MainActivity"
private const val TITLE = "title"
private const val NOTE = "note"
class MainActivity : AppCompatActivity() {

    private lateinit var database:FirebaseDatabase
    private var reference: DatabaseReference? = null
    private lateinit var fab: FloatingActionButton
    private lateinit var alertBuilder: AlertDialog.Builder
    private lateinit var addNoteAlertDialog: AlertDialog
    private lateinit var view: View
    private lateinit var recyclerView: RecyclerView
    private var adapter = NotesAdapter(emptyList())
    private var notesList = mutableListOf<Note>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //get database object
        database = FirebaseDatabase.getInstance()
        //get a root reference
        reference = database.getReference("Notes")

        fab = findViewById(R.id.fab)

        fab.setOnClickListener {
            showAddNoteDialog()
        }

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        reference?.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: ${error.message}" )
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                notesList.clear()
                for (note in snapshot.children) {
                    val currentNote = note.getValue(Note::class.java)
                    notesList.add(0,currentNote!!)
                }
                Log.i(TAG, "onDataChange: ${notesList.size}")
                adapter = NotesAdapter(notesList)
                recyclerView.adapter = adapter
            }
        })

    }


    private fun showAddNoteDialog() {
        alertBuilder = AlertDialog.Builder(this)
        view = layoutInflater.inflate(R.layout.add_note_dialog_layout,null)
        alertBuilder.setView(view)

        addNoteAlertDialog =  alertBuilder.create()
        addNoteAlertDialog.show()

        view.add_note_button.setOnClickListener {
            addNote()
        }
    }

    private fun showUpdateNoteDialog (note:Note) {
        alertBuilder = AlertDialog.Builder(this)
        view = layoutInflater.inflate(R.layout.update_note_dialog_layout,null)
        alertBuilder.setView(view)

        addNoteAlertDialog =  alertBuilder.create()
        addNoteAlertDialog.show()

        //set the data

        var title = note.title
        var currentNote = note.note
        val id = note.id
        var timeStamp: Long

        view.title_edit_text.setText(title)
        view.note_edit_text.setText(currentNote)

        view.update_note_button.setOnClickListener {
             title = view.title_edit_text.editableText.toString()
             currentNote = view.note_edit_text.editableText.toString()
             timeStamp = System.currentTimeMillis()

            if (title.isNotEmpty() && currentNote.isNotEmpty()) {
                val myNote = Note(id,title,currentNote,timeStamp)
                reference!!.child(id).setValue(myNote)
                addNoteAlertDialog.dismiss()
                Toast.makeText(this, "${note.title} updated.", Toast.LENGTH_LONG).show()
            }

            else{
                Toast.makeText(this, "Empty title or note!", Toast.LENGTH_LONG).show()
            }
        }

        view.delete_note_button.setOnClickListener {
            reference?.child(note.id)?.removeValue()
            addNoteAlertDialog.dismiss()
            Toast.makeText(this, "${note.title} deleted..", Toast.LENGTH_LONG).show()
        }
    }

    private fun addNote () {
        val title = view.title_edit_text.editableText.toString()
        val note = view.note_edit_text.editableText.toString()
        val time = System.currentTimeMillis()
        if (title.isNotEmpty() && note.isNotEmpty()) {
            val id = reference!!.push().key
            val myNote = Note(id!!,title,note,time)
            reference!!.child(id).setValue(myNote)
            addNoteAlertDialog.dismiss()
        }

        else{
            Toast.makeText(this, "Empty title or note!", Toast.LENGTH_LONG).show()
        }

    }


   inner class NotesAdapter (private var list:List<Note>) : RecyclerView.Adapter<NotesAdapter.NotesHolder>() {

        inner class NotesHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

            init {
                itemView.setOnClickListener(this)
                itemView.setOnLongClickListener(this)
            }

            private val titleTextView = itemView.findViewById<TextView>(R.id.title)
            private val dateTextView = itemView.findViewById<TextView>(R.id.date)

            fun bind (note:Note) {
                titleTextView.text = note.title
                dateTextView.text = getDate(note.timestamp)
            }

            override fun onClick(item: View?) {
                val noteItem = list[adapterPosition]
                val currentTitle = noteItem.title
                val currentNoteDetails =  noteItem.note

                val intent = Intent(item?.context , NoteActivity::class.java)
                intent.putExtra(TITLE , currentTitle)
                intent.putExtra(NOTE , currentNoteDetails)

                item?.context?.startActivity(intent)
            }

            override fun onLongClick(item: View?): Boolean {
                showUpdateNoteDialog(list[adapterPosition])
                return true
            }

            private fun getDate (timeStamp:Long) : String {
                val sdf = SimpleDateFormat("MM/dd/yyyy")
                val date = Date(timeStamp)

                return sdf.format(date).toString()
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.note_layout,parent,false )

            return NotesHolder(view)
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: NotesHolder, position: Int) {
            val note = list[holder.adapterPosition]
            holder.bind(note)
        }
    }


}