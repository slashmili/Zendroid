package com.github.slashmili.Zendroid.Storage


import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import android.util.Log

class UserData (context: Context) {
    val DATABASE_NAME = "com.github.slashmili.zondroid.db";
    val DATABASE_VERSION = 1
    val TABLE_NAME  = "user"
    
    val INSERT = "insert into %s (url, username, password, check_every) values( ?, ?, ?, ?)".format(TABLE_NAME)

    val openHelper = new OpenHelper(this.context)
    val db: SQLiteDatabase = openHelper.getWritableDatabase
    val insertStmt : SQLiteStatement = db.compileStatement(INSERT)


    
    def save(url: String, username: String, password: String, checkEvery: String): Long = {
        insertStmt.bindString(1, url)
        insertStmt.bindString(2, username)
        insertStmt.bindString(3, password)
        insertStmt.bindString(4, checkEvery)
        return this.insertStmt.executeInsert
    }

    def deleteAll = {
        db.delete(TABLE_NAME, null,null)
    }

    def selectAll  = {
        var ret_val: List[Map[String, String]] = List()
        val cursor = db.query(TABLE_NAME, Array ( "url", "username","password","check_every" ),null, null, null, null, "url desc");
        if (cursor.moveToFirst()) {
          do {
            val m = Map("url"->cursor.getString(0),"username"->cursor.getString(1),
                        "password"->cursor.getString(2), "check_every"-> cursor.getString(3))

                ret_val = ret_val ::: List(m)
          }while(cursor.moveToNext())
        }
        cursor.close()
        ret_val
    }

    class OpenHelper(context:Context) extends 
    SQLiteOpenHelper(context, DATABASE_NAME,
                null, DATABASE_VERSION)
    {
        override def onCreate(db: SQLiteDatabase) = {
            db.execSQL("CREATE TABLE %s (id INTEGER PRIMARY KEY, url TEXT, username TEXT, password TEXT, check_every TEXT)".format(TABLE_NAME))
        }
        override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int)= {
            Log.w("Example", "Upgrading database, this will drop tables and recreate.")
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
