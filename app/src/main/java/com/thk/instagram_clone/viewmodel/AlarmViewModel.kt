@file:OptIn(ExperimentalCoroutinesApi::class)

package com.thk.instagram_clone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.snapshots
import com.thk.instagram_clone.model.AlarmDto
import com.thk.instagram_clone.util.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

class AlarmViewModel : ViewModel() {

    /**
     * 알람 리스트를 가지는 Flow
     */
    val alarmsFlow = Firebase.firestore
        .collection("alarms")
        .whereEqualTo("destinationUid", Firebase.auth.currentUser?.uid)
        .snapshots()
        .mapLatest { value ->
            value.documents.map {
                it.toObject(AlarmDto::class.java)
                    ?: throw IllegalArgumentException("null returned")
            }
        }.catch {
            it.printStackTrace()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}