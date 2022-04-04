package org.sjhstudio.howstoday.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * AppDatabase
 * DB 인스턴스 초기화
 *
 * interface와 abstract의 차이..?
 * -> abstarct에서는 구현도 가능
 */
@Database(entities = [LocBookmark::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun locBookmarkDao(): LocBookmarkDao

    companion object {
        private var instance: AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context) {
            if(instance != null) {
                // 단 한번의 접근을 위한 lock(다중스레드)
                synchronized(this) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "db"
                    ).build()
                }
            }
        }
    }

}