package org.sjhstudio.howstoday.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import org.sjhstudio.howstoday.model.LocBookmark
import org.sjhstudio.howstoday.data.local.dao.LocBookmarkDao

@Database(entities = [LocBookmark::class], version = 1, exportSchema = false)
abstract class LocBookmarkDatabase: RoomDatabase() {

    abstract fun locBookmarkDao(): LocBookmarkDao

}