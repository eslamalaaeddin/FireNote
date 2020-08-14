package com.example.firebaseapp

class Note(var id:String, var title:String, var note:String, var timestamp: Long) {
    constructor() : this ("","","",0L)
}