package com.thk.data.di

import com.thk.data.repository.LoginRepository
import com.thk.data.repository.LoginRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object LoginModule {
    @Provides
    fun provideLoginRepository(): LoginRepository = LoginRepositoryImpl()
}