package org.sjhstudio.howstoday.repository

import androidx.lifecycle.LiveData
import org.sjhstudio.howstoday.data.local.LocBookmarkDatabase
import org.sjhstudio.howstoday.model.LocBookmark
import org.sjhstudio.howstoday.data.local.dao.LocBookmarkDao
import javax.inject.Inject

class LocBookmarkRepository @Inject constructor(
    locBookmarkDatabase: LocBookmarkDatabase
) {

    private val locBookmarkDao: LocBookmarkDao = locBookmarkDatabase.locBookmarkDao()

    fun getAll(): LiveData<List<LocBookmark>> {
        return locBookmarkDao.getAll()
    }

    fun update(locBookmark: LocBookmark) {
        locBookmarkDao.update(locBookmark)
    }

    fun insert(locBookmark: LocBookmark) {
        locBookmarkDao.insert(locBookmark)
    }

    fun delete(locBookmark: LocBookmark) {
        locBookmarkDao.delete(locBookmark)
    }

}