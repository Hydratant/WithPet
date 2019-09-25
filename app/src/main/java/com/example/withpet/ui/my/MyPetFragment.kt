package com.example.withpet.ui.my

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.example.withpet.R
import com.example.withpet.core.BaseFragment
import com.example.withpet.databinding.MyPetFragmentBinding
import com.example.withpet.ui.pet.PetEditActivity
import com.example.withpet.util.Log
import com.example.withpet.vo.pet.PetDTO
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MyPetFragment : BaseFragment() {

    private val viewModelOwner: MyFragment by lazy { parentFragment as MyFragment }

    lateinit var bb: MyPetFragmentBinding
    private val vm by sharedViewModel<MyViewModel>(from = { viewModelOwner })
    private val myPetVm by viewModel<MyPetViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bb = DataBindingUtil.inflate(inflater, R.layout.my_pet_fragment, container, false)
        return bb.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bb.myPetVm = myPetVm
        onParseExtra()
        onLoadOnce()
    }

    private fun onParseExtra() {
        arguments?.let {
            val index = it.getInt(EXTRA.INDEX, -1)
            if (index < 0) {
                showDialog(message = "잘못된 접근입니다.", positiveButtonText = "확인")
                return@let
            }
            myPetVm.initData(vm.petList[index])
        }
    }

    private fun onLoadOnce() {
        myPetVm.goHospital.observe(this, Observer {
            // TODO : 병원 어떻게..?
        })

        myPetVm.goPetNumInfo.observe(this, Observer {
            it?.let { petNum ->
                val petNumInfoUrl = "http://www.animal.go.kr/mobile2/html/03_inquiry_result.jsp?search_dog_reg_no=$petNum"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(petNumInfoUrl)))
            }
        })

        myPetVm.goPetNumUpdate.observe(this, Observer {
            val petAddIntent = Intent(mActivity, PetEditActivity::class.java).apply {
                putExtra(PetEditActivity.EXTRA.PET_DTO, myPetVm.petDTO)
            }
            startActivityForResult(petAddIntent, REQ_UPDATE)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQ_UPDATE -> {
                    data?.let { myPetVm.petEdit(it) }
                }
            }
        }
    }


    interface EXTRA {
        companion object {
            const val INDEX = "INDEX"
        }
    }

    companion object {
        fun newInstance(index: Int): MyPetFragment = MyPetFragment().apply {
            this.arguments = Bundle().apply { putInt(EXTRA.INDEX, index) }
        }

        private const val REQ_START = 1800
        const val REQ_UPDATE = REQ_START
    }
}