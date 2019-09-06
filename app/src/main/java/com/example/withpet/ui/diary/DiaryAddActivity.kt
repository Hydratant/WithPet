package com.example.withpet.ui.diary

import android.app.Activity
import android.app.DatePickerDialog
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.withpet.R
import com.example.withpet.core.BaseActivity
import com.example.withpet.databinding.ActivityDiaryAddBinding
import com.example.withpet.util.Gallery
import com.example.withpet.util.Log
import com.example.withpet.util.SDF
import com.sang.permission.permission
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileNotFoundException
import java.util.*
import kotlin.math.log10

class DiaryAddActivity : BaseActivity() {

    private val vm: DiaryAddViewModel by viewModel()
    lateinit var bb: ActivityDiaryAddBinding

    private val calendar = Calendar.getInstance()
    private val datePicker: DatePickerDialog by lazy {
        DatePickerDialog(mContext, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            vm.resultCalendar(year, month, dayOfMonth)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).apply {
            datePicker.maxDate = calendar.timeInMillis
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bb = DataBindingUtil.setContentView(mActivity, R.layout.activity_diary_add)
        bb.vm = vm
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        onLoadOnce()
    }

    private fun onLoadOnce() {
        vm.callGallery.observe(mActivity, Observer {
            permission(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                onGranted = { Gallery.callGallery(mActivity, REQ_GALLERY) }
                onDenied = {
                    showDialog(message = "권한동의를 하지 않으면 사용 하실 수 없습니다.", positiveButtonText = "확인")
                }
            }
        })

        vm.showCalendar.observe(mActivity, Observer { if (!datePicker.isShowing) datePicker.show() })
        vm.alertMessage.observe(mActivity, Observer { message -> message?.let { showDialog(message = message, positiveButtonText = "확인") } })
    }

    private fun resultGallery(data: Intent) {
        try {
            val imageUri = data.data
            imageUri?.let {
                val cropIntent = Gallery.getCropIntent(application, imageUri)
                startActivityForResult(cropIntent, REQ_CROP)
            }
        } catch (fe: FileNotFoundException) {
            Log.e("resultGallery ErrorMessage : ${fe.message}")
            fe.printStackTrace()
        }
    }

    private fun resultCrop(data: Intent) {
        Log.i("resultCrop")
        val imageUri = data.data
        imageUri?.let {
            val imageStream = contentResolver.openInputStream(it)
            val imageBitmap = BitmapFactory.decodeStream(imageStream)
            bb.image.setImageBitmap(imageBitmap)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQ_GALLERY -> data?.let { resultGallery(it) }
                REQ_CROP -> data?.let { resultCrop(it) }
            }
        }
    }


    companion object {
        private const val REQ_START = 2000
        private const val REQ_GALLERY = REQ_START
        private const val REQ_CROP = REQ_START + 1
    }
}