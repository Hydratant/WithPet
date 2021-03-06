package com.example.withpet.util

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.withpet.ui.common.WebViewActivity
import java.io.InputStream
import android.view.MotionEvent


object CommonBindingAdapter {


    @JvmStatic
    @BindingAdapter("app:isUnderLine")
    fun textViewUnderLine(view: TextView, isUnderLine: Boolean?) {
        isUnderLine?.let {
            if (it)
                view.paintFlags = view.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
    }


    @JvmStatic
    @BindingAdapter("app:isSelection")
    fun buttonSelection(view: Button, isSelection: Boolean?) {
        isSelection?.let {
            view.isEnabled = it
        }
    }

    @JvmStatic
    @BindingAdapter("app:inputStreamBitmap")
    fun setInputStreamToBitmap(view: ImageView, inputStream: InputStream?) {
        inputStream?.let {
            val imageBitmap = BitmapFactory.decodeStream(it)
            view.setImageBitmap(imageBitmap)
        }
    }

    @JvmStatic
    @BindingAdapter("app:bitmap")
    fun setBitmapImage(view: ImageView, bitmap: Bitmap?) {
        bitmap?.let {
            view.scaleType = ImageView.ScaleType.CENTER_CROP
            view.setImageBitmap(bitmap)
        }
    }

    @JvmStatic
    @BindingAdapter("app:glideImage")
    fun setGlideImage(view: ImageView, downloadUrl: String?) {
        downloadUrl?.let {
            val context = view.context
            Glide.with(context).load(it).into(view)
        }
    }

    @JvmStatic
    @BindingAdapter("app:loadUrl")
    fun loadUrl(view: View, url: String) {
        view.setOnClickListener {
            val context = view.context
            val intent = Intent(context, WebViewActivity::class.java).apply {
                putExtra(WebViewActivity.URL, url)
            }
            context.startActivity(intent)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @JvmStatic
    @BindingAdapter("app:scroll")
    fun scroll(view: TextView, isScroll: Boolean?) {
        isScroll?.let {
            if (it) {
                view.movementMethod = ScrollingMovementMethod()
                view.setOnTouchListener { v, event ->
                    view.parent.requestDisallowInterceptTouchEvent(true)
                    when (event.action and MotionEvent.ACTION_MASK) {
                        MotionEvent.ACTION_UP -> view.parent.requestDisallowInterceptTouchEvent(false)
                    }
                    return@setOnTouchListener false
                }
            }
        }

    }

}