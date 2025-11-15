package com.msa.seeyoulater.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.msa.seeyoulater.data.local.dao.LinkDao
import com.msa.seeyoulater.data.local.entity.Link

@Database(entities = [Link::class], version = 1, exportSchema = false)
abstract class LinkDatabase : RoomDatabase() {

    abstract fun linkDao(): LinkDao

    companion object {
        @Volatile
        private var INSTANCE: LinkDatabase? = null

        fun getDatabase(context: Context): LinkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LinkDatabase::class.java,
                    "link_database"
                )
                // Wipes and rebuilds instead of migrating if no Migration object.
                // Migration is not part of this basic implementation.
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
