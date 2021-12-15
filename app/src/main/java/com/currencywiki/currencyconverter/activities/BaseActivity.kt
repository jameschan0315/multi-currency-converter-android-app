package com.currencywiki.currencyconverter.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.currencywiki.currencyconverter.R
import com.currencywiki.currencyconverter.common.*

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private fun showActivity(
        activity: Class<*>?,
        data: Bundle?,
        animation: Int,
        finish: Boolean
    ) {
        try {
            val intent = Intent(this, activity)
            if (data != null) {
                intent.putExtras(data)
            }
            startActivity(intent)
            when (animation) {
                ANIMATION_RIGHT_TO_LEFT -> overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                ANIMATION_LEFT_TO_RIGHT -> overridePendingTransition(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                ANIMATION_BOTTOM_TO_UP -> overridePendingTransition(
                    R.anim.push_up_in,
                    R.anim.push_up_out
                )
                ANIMATION_UP_TO_BOTTOM -> overridePendingTransition(
                    R.anim.push_down_in,
                    R.anim.push_down_out
                )
            }
            if (finish) {
                finish()
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    @JvmOverloads
    fun showActivity(t: Class<*>?, data: Bundle? = null) {
        showActivity(t, data, -1, true)
    }

    fun showActivity(t: Class<*>?, requestCode: Int) {
        showActivity(t, null, requestCode)
    }

    fun showActivity(t: Class<*>?, data: Bundle?, requestCode: Int) {
        val intent = Intent(this, t)
        if (data != null) {
            intent.putExtras(data)
        }
        startActivityForResult(intent, requestCode)
    }

    fun showActivity(
        activity: Class<*>?,
        animation: Int,
        finish: Boolean
    ) {
        try {
            val intent = Intent(this, activity)
            startActivity(intent)
            when (animation) {
                ANIMATION_RIGHT_TO_LEFT -> overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                ANIMATION_LEFT_TO_RIGHT -> overridePendingTransition(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                ANIMATION_BOTTOM_TO_UP -> overridePendingTransition(
                    R.anim.push_up_in,
                    R.anim.push_up_out
                )
                ANIMATION_UP_TO_BOTTOM -> overridePendingTransition(
                    R.anim.push_down_in,
                    R.anim.push_down_out
                )
            }
            if (finish) {
                finish()
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    fun showActivityForResult(
        activity: Class<*>?,
        extraDataKey: String?,
        extraData: Int,
        requestCode: Int
    ) {
        val newIntent = Intent(this, activity)
        newIntent.putExtra(extraDataKey, extraData)
        startActivityForResult(newIntent, requestCode)
    }

    fun showActivityAndClearStack(activity: Class<*>?, animation: Int) {
        val newIntent = Intent(this, activity)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        when (animation) {
            ANIMATION_RIGHT_TO_LEFT -> overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
            ANIMATION_LEFT_TO_RIGHT -> overridePendingTransition(
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            ANIMATION_BOTTOM_TO_UP -> overridePendingTransition(
                R.anim.push_up_in,
                R.anim.push_up_out
            )
            ANIMATION_UP_TO_BOTTOM -> overridePendingTransition(
                R.anim.push_down_in,
                R.anim.push_down_out
            )
        }
        startActivity(newIntent)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    fun showError(message: String?) {
        try {
            Toast.makeText(this.applicationContext, message, Toast.LENGTH_SHORT).show()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    fun showError(message: Int) {
        try {
            Toast.makeText(this.applicationContext, message, Toast.LENGTH_SHORT).show()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    fun cleanBackStack() {
        val fragmentManager = supportFragmentManager
        for (i in 0 until fragmentManager.backStackEntryCount) {
            fragmentManager.popBackStack()
        }
    }
}