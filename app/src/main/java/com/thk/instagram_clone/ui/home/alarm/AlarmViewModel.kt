@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thk.instagram_clone.ui.home.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.snapshots
import com.thk.data.model.AlarmDto
import com.thk.data.repository.MainRepository
import com.thk.data.util.Firebase
import com.thk.data.util.logd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    /**
     * 알람 리스트를 가지는 Flow
     */
    val alarmsFlow = mainRepository.getAlarmList { logd(it ?: "message is null") }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}