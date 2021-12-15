package com.currencywiki.currencyconverter.utils

import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.Exception

class DatabaseManager {
    private var mContext: Context
    private var mHelper: DatabaseHelper? = null
    private var mDb: SQLiteDatabase?
    private val databasePath = "data/data/com.currency.android/databases"
    private val databaseAsset = "db"

    constructor(context: Context) {
        this.mContext = context
        this.mDb = null
    }

    fun openDatabase(sDBName: String) : Boolean {
        var bRet = false
        if (!createDatabase(sDBName)) {
            return bRet
        }
        mHelper = DatabaseHelper(mContext, sDBName)
        mDb = mHelper?.writableDatabase
        if (mDb?.isOpen == true) {
            bRet = true
        }
        return bRet
    }

    private fun closeDatabase() {
        mDb?.close()
        mDb = null
        mHelper?.close()
        mHelper = null
    }

    private fun isOpen() : Boolean {
        var bRet = false
        if (mDb != null) {
            if (mDb?.isOpen == true) {
                bRet = true
            }
        }
        return bRet
    }

    fun openCursor(sQuery: String) : Cursor? {
        var c: Cursor? = null
        try {
            val openRet = isOpen()
            if (openRet) {
                c = mDb?.rawQuery(sQuery, null)
            }
        } catch (e: SQLException) {
            c = null
        }
        return c
    }

    private fun createDatabase(sDBName: String) : Boolean {
        var bRet: Boolean
        val sFilePath = "$databasePath/$sDBName"
        var file = File(sFilePath)
        if (file.exists() && file.isFile) {
            bRet = true
            return bRet
        }

        file = File(databasePath)
        if (!file.exists() || !file.isDirectory) {
            file.mkdir()
        }
        var inputStream: InputStream? = null
        var fos: FileOutputStream? = null

        try {
            inputStream = mContext.assets.open("$databaseAsset/$sDBName")
            fos = FileOutputStream("$databasePath/$sDBName")
            var buf = ByteArray(2048)
            var nLen: Int
            do {
                nLen = inputStream.read(buf)
                if (nLen > -1) {
                    fos.write(buf, 0, nLen)
                }
            } while (nLen > -1)
            bRet = true
        } catch (e: Exception) {
            Log.d("AppData", e.message)
            bRet = false
        } finally {
            try {
                inputStream?.close()
                fos?.close()
            } catch (e: Exception) {

            }
        }

        openDatabase(sDBName)
        if (isOpen()) {
            closeDatabase()
        }
        return bRet
    }

    fun updateTableData(sql: String) {
        mDb?.execSQL(sql)
    }
}