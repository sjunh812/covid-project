package org.sjhstudio.howstoday.database

import androidx.lifecycle.LiveData
import androidx.room.*

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