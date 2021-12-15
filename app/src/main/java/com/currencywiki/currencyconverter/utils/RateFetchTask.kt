package com.currencywiki.currencyconverter.utils

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.currencywiki.currencyconverter.CurrencyConverterApp
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import javax.net.ssl.HttpsURLConnection

open class RateFetchTask(private val mContext: Context) :
    AsyncTask<Void?, Void?, JSONObject?>() {
    var rateFetchTaskInterface: RateFetchTaskInterface? = null

    override fun doInBackground(vararg params: Void?): JSONObject? {
       // val str = "https://www.currency.wiki/api/currency/quotes/784565d2-9c14-4b25-8235-06f6c5029b15"
        val str = "https://www.currency.wiki/api/currency/quotes/784565d2-9c14-4b25-8235-06f6c5029b15"
    //    val str = "http://currencywiki.net/api/currency/quotes/784565d2-9c14-4b25-8235-06f6c5029b15"
//        val str = "http://192.168.132.2/wikijson.php"
        var urlConn: URLConnection? = null
        var bufferedReader: BufferedReader? = null
        return try {
            val url = URL(str)
            urlConn = url.openConnection() as HttpsURLConnection
         //   urlConn = url.openConnection() as HttpURLConnection
            bufferedReader = BufferedReader(InputStreamReader(urlConn.getInputStream()))
            val stringBuffer = StringBuffer()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuffer.append(line)
            }
            JSONObject(stringBuffer.toString())
        } catch (ex: Exception) {
            Log.e("App", "RateFetchTask", ex)
            //Toaster.get().showToast(mContext, "JsonErr>> ${ex.toString()}", Toast.LENGTH_LONG)
            val rateFetchTask = RateFetchTask(mContext)
            rateFetchTask.execute()
            rateFetchTask.rateFetchTaskInterface = rateFetchTaskInterface
           // rateFetchTaskInterface?.rateError("connectionErr>> ${ex.toString()}")
            null
        } catch (ex1: ConnectException) {
            Log.e("App", "RateFetchTask", ex1)
           // Toaster.get().showToast(mContext, "connectionErr>> ${ex1.toString()}", Toast.LENGTH_LONG)
           // rateFetchTaskInterface?.rateError("connectionErr>> ${ex1.toString()}")
            null
        }
        finally {
            if (bufferedReader != null) {
                try {

                   // rateFetchTaskInterface?.rateError("bufferedReader>>")
                    bufferedReader.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onPostExecute(response: JSONObject?) {
        if (response != null) {
            try {
                Log.e("App", "Success: $response")
                val appData = CurrencyConverterApp.instance?.appData!!
                appData.reentrantLock.lock()
                appData.currencyRateJson = response
                appData.reentrantLock.unlock()
                rateFetchTaskInterface?.rateUpdated(true)
              //  Toaster.get().showToast(mContext, "succuss>> ${response.toString()}", Toast.LENGTH_SHORT)
                //rateFetchTaskInterface?.rateError("succuss>> ${response.toString()}")
            } catch (ex: JSONException) {
                Log.e("App", "Failure", ex)
                rateFetchTaskInterface?.rateUpdated(false)
               // Toaster.get().showToast(mContext, "Failure>> ${ex.toString()}", Toast.LENGTH_LONG)
               // rateFetchTaskInterface?.rateError("Failure>> ${ex.toString()}")
            }
        }else{
            rateFetchTaskInterface?.rateUpdated(false)
           // Toaster.get().showToast(mContext, "ResponseErr", Toast.LENGTH_LONG)
        }
    }
}