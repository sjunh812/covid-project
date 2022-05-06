package org.sjhstudio.howstoday.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import org.sjhstudio.howstoday.model.LocBookmark

@Dao
interface LocBookmarkDao {

    @Query("SELECT * FROM LocBookmark")
    fun getAll(): LiveData<List<LocBookmark>>

    @Update
    fun update(locBookmark: LocBookmark)

    @Insert
    fun insert(locBookmark: LocBookmark)

    @Delete
    fun delete(locBookmark: LocBookmark)

}