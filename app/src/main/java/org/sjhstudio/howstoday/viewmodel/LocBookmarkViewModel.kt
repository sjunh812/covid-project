package org.sjhstudio.howstoday.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sjhstudio.howstoday.database.LocBookmark
import org.sjhstudio.howstoday.repository.LocBookmarkRepository

class LocBookmarkViewModel(application: Application): AndroidViewModel(application) {

    private val locBookmarkRepository = LocBookmarkRepository(application)
    private val locBookmarkList = locBookmarkRepository.getAll()

    private var _lbResult = MutableLiveData<String>()
    val lbResult: LiveData<String>
        get() = _lbResult

    init {
        _lbResult.value = ""
    }

    fun getAll(): LiveData<List<LocBookmark>> {
        return locBookmarkList
    }

    fun insert(locBookmark: LocBookmark) {
        viewModelScope.launch {
            withContext(IO) { locBookmarkRepository.insert(locBookmark) }
            _lbResult.value = "즐겨찾기 추가완료"
        }
    }

    fun delete(locBookmark: LocBookmark) {
        viewModelScope.launch {
            withContext(IO) { locBookmarkRepository.delete(locBookmark) }
            _lbResult.value = "즐겨찾기 삭제완료"
        }
    }

}