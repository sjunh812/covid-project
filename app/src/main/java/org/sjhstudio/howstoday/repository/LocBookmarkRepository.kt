package org.sjhstudio.howstoday.repository

import android.app.Application
import androidx.lifecycle.LiveData
import org.sjhstudio.howstoday.database.AppDatabase
import org.sjhstudio.howstoday.database.LocBookmark
import org.sjhstudio.howstoday.database.LocBookmarkDao

class LocBookmarkRepository(application: Application) {

    private val locBookmarkDao: LocBookmarkDao
    private val locBookmarkList: LiveData<List<LocBookmark>>

    init {
        val db = AppDatabase.getInstance(application)
        locBookmarkDao = db!!.locBookmarkDao()
        locBookmarkList = db.locBookmarkDao().getAll()
    }

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