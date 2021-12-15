package com.currencywiki.currencyconverter.utils

import android.content.Context
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset

class JSONUtil {
    fun loadJSONFromAssets(context: Context, fileName: String) : String? {
        var json: String? = null
        json = try {
            val inputStream: InputStream = context.assets.open("json/$fileName")
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    fun loadJSONFromURL(context: Context): String? {
        return null
    }
}