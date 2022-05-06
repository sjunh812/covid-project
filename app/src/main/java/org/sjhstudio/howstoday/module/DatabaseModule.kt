package org.sjhstudio.howstoday.module

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.sjhstudio.howstoday.data.local.LocBookmarkDatabase
import org.sjhstudio.howstoday.util.Constants.LOCBOOKMARK_DATABASE
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun getLocationBookmarkDatabase(
        @ApplicationContext context: Context
    ): LocBookmarkDatabase {
        synchronized(this) {
            return Room.databaseBuilder(
                context,
                LocBookmarkDatabase::class.java,
                LOCBOOKMARK_DATABASE
            ).build()
        }
    }

}