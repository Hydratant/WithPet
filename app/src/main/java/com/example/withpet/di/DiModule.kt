package com.example.withpet.di

import com.example.withpet.viewModel.JoinViewModel
import com.example.withpet.viewModel.LoginViewModel
import com.example.withpet.viewModel.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

var repositoryPart = module {
    //single<HelloRepository> { HelloRepositoryImpl() }
}

var viewModelPart = module {
    viewModel {
        MainViewModel()
    }
    viewModel {
        LoginViewModel()
    }
    viewModel {
        JoinViewModel()
    }
}

var recyclerViewAdapterPart = module {

}


var diModule = listOf(
    viewModelPart, recyclerViewAdapterPart
)