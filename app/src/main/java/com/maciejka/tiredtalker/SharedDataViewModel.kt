package com.maciejka.tiredtalker

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedDataViewModel : ViewModel() {
    fun setReferenceRecorded(b: Boolean) {
        isReferenceRecorded.value = b

    }

    val wordLimit = MutableLiveData<Int>()
    val isReferenceRecorded = MutableLiveData<Boolean>().apply { value = false }
    val referenceFilePath = MutableLiveData<String>()
}
