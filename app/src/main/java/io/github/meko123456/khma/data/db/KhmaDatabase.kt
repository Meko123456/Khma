package io.github.meko123456.khma.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PodcastEntity::class, EpisodeEntity::class], version = 1, exportSchema = false)
abstract class KhmaDatabase : RoomDatabase() {

    abstract fun podcastDao(): PodcastDao
    abstract fun episodeDao(): EpisodeDao

    companion object {
        @Volatile
        private var instance: KhmaDatabase? = null

        fun get(context: Context): KhmaDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    KhmaDatabase::class.java,
                    "khma.db",
                ).build().also { instance = it }
            }
    }
}
