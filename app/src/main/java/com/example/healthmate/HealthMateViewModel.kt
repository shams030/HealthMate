package com.example.healthmate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HealthMateViewModel(application: Application) :
    AndroidViewModel(application) {

    private val dao = HealthMateDatabaseInstance
        .getDatabase(application)
        .healthRecordDao()

    val records = dao.getAllRecords().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addRecord(record: HealthRecord) {
        viewModelScope.launch {
            dao.insertRecord(record)
        }
    }

    fun updateRecord(record: HealthRecord) {
        viewModelScope.launch {
            dao.updateRecord(record)
        }
    }

    fun deleteRecord(record: HealthRecord) {
        viewModelScope.launch {
            dao.deleteRecord(record)
        }
    }
}