package com.example.dewapermana_smkn8jember

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SqlLite(context: Context) : SQLiteOpenHelper(context, "UserDB", null, 2) {
	override fun onCreate(db: SQLiteDatabase?) {
		db?.execSQL("""
            CREATE TABLE users(
                id INTEGER PRIMARY KEY AUTOINCREMENT, 
                username TEXT,
				email TEXT,
                log_fingerprint TEXT
            )
        """)
	}

	override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
		db?.execSQL("DROP TABLE IF EXISTS users")
		onCreate(db)
	}

	fun insertUser(username : String, email : String, logFinger : String){
		val db = this.writableDatabase
		val values = ContentValues()
		values.put("username", username)
		values.put("email", email)
		values.put("log_fingerprint", logFinger)
		db.insert("users", null, values)
		db.close()
	}

	fun verifyUser(email: String, logFinger: String): Boolean {
		val db = this.readableDatabase
		val cursor = db.rawQuery(
			"SELECT * FROM users WHERE email = ? AND log_fingerprint = ?",
			arrayOf(email, logFinger)
		)
		val exists = cursor.count > 0
		cursor.close()
		db.close()
		return exists
	}
}