package com.example.withpet.ui.walk.usecase.impl

import android.content.Context
import com.example.withpet.R
import com.example.withpet.ui.walk.usecase.WalkUseCase
import com.example.withpet.util.Formatter
import com.example.withpet.util.Log
import com.example.withpet.util.Util
import com.example.withpet.vo.walk.WalkBicycleDTOList
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import io.reactivex.Observable

class WalkUseCaseImpl(var context: Context) : WalkUseCase {
    private val db = FirebaseFirestore.getInstance()

    override fun insertBicycleList(): Observable<Boolean> {
        return Observable.create { emitter ->
            val raw = Util.raw2string(context, R.raw.test_bicycle)
            val temp = Gson().fromJson(raw, WalkBicycleDTOList::class.java).parseData()
            var successCount = 0
            var cancelCount = 0
            var failCount = 0
            var finishCount = 0
            val target = temp.bicycleDTOList.subList(0, 100)
            target.forEach { data ->
                val id = WALK_BICYCLE + "_" + Formatter.getDbIdFormat(data.objectid)
                db.collection(WALK_BICYCLE).document(id).set(data)
                        .addOnSuccessListener { successCount++ }
                        .addOnCanceledListener { cancelCount++ }
                        .addOnFailureListener { failCount++ }
                        .addOnCompleteListener {
                            finishCount++
                            if (finishCount == target.size) {
                                Log.w("Database Insert Finish\n" +
                                        "==================================================================================================\n" +
                                        "successCount = $successCount\n" +
                                        "cancelCount = $cancelCount\n" +
                                        "failCount = $failCount\n" +
                                        "finishCount = $finishCount\n" +
                                        "=================================================================================================="
                                )
                                emitter.onNext(true)
                            }
                        }
            }
        }
    }

    override fun getBicycleList(): Observable<WalkBicycleDTOList> {
        return Observable.create { emitter ->
            val raw = Util.raw2string(context, R.raw.test_bicycle)
            emitter.onNext(Gson().fromJson(raw, WalkBicycleDTOList::class.java).parseData())
        }

    }

    companion object {
        private const val WALK_BICYCLE = "WALK_BICYCLE"
    }

}