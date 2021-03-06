package com.example.withpet.ui.diary

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.withpet.core.BaseViewModel
import com.example.withpet.ui.diary.usecase.DiaryUseCase
import com.example.withpet.ui.pet.usecase.ImageUseCase
import com.example.withpet.util.*
import com.example.withpet.vo.diary.DiaryDTO
import java.io.*
import kotlin.math.log10

class DiaryEditViewModel(private val diaryUseCase: DiaryUseCase,
                         private val imageUseCase: ImageUseCase) : BaseViewModel() {


    val content = ObservableField<String>()         // 내용
    val date = ObservableField<String>()            // 날짜
    val image = ObservableField<Bitmap>()           // 사진

    private val _callCrop = MutableLiveData<Uri>()     // Crop 호출
    val callCrop: LiveData<Uri>
        get() = _callCrop

    private val _callGallery = LiveEvent<Any>()     // Gallery 호출
    val callGallery: LiveData<Any>
        get() = _callGallery

    private val _showCalendar = LiveEvent<Any>()    // Calendar 호출
    val showCalendar: LiveData<Any>
        get() = _showCalendar

    private val _errorMessage = MutableLiveData<String>()   // Error Message
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private val _showProgress = MutableLiveData<Boolean>()   // Error Message
    val showProgress: LiveData<Boolean>
        get() = _showProgress

    private val _insertSuccess = MutableLiveData<DiaryDTO>()   // Gallery 호출
    val insertSuccess: LiveData<DiaryDTO>
        get() = _insertSuccess

    lateinit var petName: String

    var diaryDTO: DiaryDTO? = null


    fun gallery() = _callGallery.call()
    fun calendar() = _showCalendar.call()
    private fun callCrop(imageUri: Uri) = _callCrop.postValue(imageUri)

    fun init(diaryDTO: DiaryDTO) {
        content.set(diaryDTO.content)
        date.set(diaryDTO.date)
        launch {
            imageUseCase.getBitmapFromUrl(diaryDTO.imageUrl)
                    .with()
                    .progress(_showProgress)
                    .subscribe({ image.set(it) }, { _errorMessage.postValue(it.message) })
        }
        this.diaryDTO = diaryDTO
    }


    fun resultCalendar(year: Int, month: Int, dayOfMonth: Int) {
        Log.i("year : $year, month : $month, dayOfMonth:$dayOfMonth")
        val plusMonth = month + 1

        val dayLength = (log10(dayOfMonth.toDouble()) + 1).toInt()
        val monthLength = (log10(plusMonth.toDouble()) + 1).toInt()

        val formatDay = if (dayLength == 1) "0$dayOfMonth" else dayOfMonth.toString()
        val formatMonth = if (monthLength == 1) "0$plusMonth" else plusMonth.toString()

        val selectDate = "$year$formatMonth$formatDay"
        val parseSelectDate = SDF.yyyymmdd.parse(selectDate)

        date.set(SDF.yyyymmdd_2.format(parseSelectDate))
    }

    fun validation() {
        val image = image.get()
        val getContent = content.get()
        val getDate = date.get()

        if (image == null) {
            _errorMessage.postValue("이미지를 넣어주세요.")
        }

        if (getContent.isNullOrEmpty()) {
            _errorMessage.postValue("내용을 입력 해 주세요.")
            return
        }

        if (getDate.isNullOrEmpty()) {
            _errorMessage.postValue("날짜를 입력 해 주세요.")
            return
        }

        imageUpload()
    }

    fun resultGallery(data: Intent) {
        try {
            val imageUri = data.data
            imageUri?.let { callCrop(it) }
        } catch (fe: FileNotFoundException) {
            Log.e("resultGallery ErrorMessage : ${fe.message}")
            fe.printStackTrace()
            _errorMessage.postValue("사진첩 호출 중 오류가 발생하였습니다.\n다시 시도하여 주세요.")
        }
    }

    fun resultCrop(data: Intent) {
        Log.i("resultCrop")
        val imageUri = data.data
        imageUri?.let {
            val imagePath = imageUseCase.getRealPath(it)
            imagePath?.let { path ->
                val stream = FileInputStream(File(path))
                val imageBitmap = BitmapFactory.decodeStream(stream)
                image.set(imageBitmap)
            }
        }
    }


    private fun imageUpload() {
        val email = Auth.getEmail()
        val isEmailNotNull = !email.isNullOrEmpty()
        if (isEmailNotNull) {
            val storagePath = "$email/diary/$petName/${date.get()}_${System.currentTimeMillis()}.jpg"
            val imageBitmap = image.get()
            imageBitmap?.let {
                try {
                    val baos = ByteArrayOutputStream()
                    it.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val byteArray = baos.toByteArray()
                    val stream = ByteArrayInputStream(byteArray)
                    launch {
                        imageUseCase.upload(storagePath, stream)
                                .with()
                                .progress(_showProgress)
                                .subscribe({ downloadUrl -> insert(downloadUrl) },
                                        { exception ->
                                            exception.printStackTrace()
                                            _errorMessage.postValue(exception.message)
                                        })
                    }
                } catch (fe: FileNotFoundException) {
                    fe.printStackTrace()
                    _errorMessage.postValue(fe.message)
                }
            }
        } else {
            _errorMessage.postValue("로그인이 필요합니다.")
        }
    }

    private fun insert(downloadUrl: String) {

        val content = content.get()
        val date = date.get()

        if (content != null && date != null) {
            val diaryDTO = DiaryDTO(content, downloadUrl, date, createDate = this.diaryDTO?.createDate
                    ?: System.currentTimeMillis())
            launch {
                diaryUseCase.edit(diaryDTO, petName)
                        .with()
                        .progress(_showProgress)
                        .subscribe({
                            if (it) _insertSuccess.postValue(diaryDTO)
                            else _insertSuccess.postValue(null)
                        }, {
                            Log.e(it)
                            _errorMessage.postValue(it.message)
                        })
            }
        }
    }
}