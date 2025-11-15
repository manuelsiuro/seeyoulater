package com.msa.seeyoulater.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.msa.seeyoulater.data.local.dao.LinkDao
import com.msa.seeyoulater.data.local.dao.TagDao
import com.msa.seeyoulater.data.local.dao.CollectionDao
import com.msa.seeyoulater.data.local.entity.Link
import com.msa.seeyoulater.data.local.entity.Tag
import com.msa.seeyoulater.data.local.entity.LinkTag
import com.msa.seeyoulater.data.local.entity.Collection
import com.msa.seeyoulater.data.local.entity.LinkCollection

@Database(
    entities = [
        Link::class,
        Tag::class,
        LinkTag::class,
        Collection::class,
        LinkCollection::class
    ],
    version = 2,
    exportSchema = false
)
abstract class LinkDatabase : RoomDatabase() {

    abstract fun linkDao(): LinkDao
    abstract fun tagDao(): TagDao
    abstract fun collectionDao(): CollectionDao

    companion object {
        @Volatile
        private var INSTANCE: LinkDatabase? = null

        /**
         * Migration from version 1 to version 2
         * Adds tags and collections tables
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create tags table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS tags (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        color TEXT,
                        createdTimestamp INTEGER NOT NULL,
                        usageCount INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // Create link_tags junction table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS link_tags (
                        linkId INTEGER NOT NULL,
                        tagId INTEGER NOT NULL,
                        addedTimestamp INTEGER NOT NULL,
                        PRIMARY KEY(linkId, tagId),
                        FOREIGN KEY(linkId) REFERENCES links(id) ON DELETE CASCADE,
                        FOREIGN KEY(tagId) REFERENCES tags(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                // Create indexes for link_tags
                db.execSQL("CREATE INDEX IF NOT EXISTS index_link_tags_linkId ON link_tags(linkId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_link_tags_tagId ON link_tags(tagId)")

                // Create collections table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS collections (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT,
                        icon TEXT,
                        color TEXT,
                        createdTimestamp INTEGER NOT NULL,
                        lastModifiedTimestamp INTEGER NOT NULL,
                        linkCount INTEGER NOT NULL,
                        sortOrder INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // Create link_collections junction table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS link_collections (
                        linkId INTEGER NOT NULL,
                        collectionId INTEGER NOT NULL,
                        addedTimestamp INTEGER NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        PRIMARY KEY(linkId, collectionId),
                        FOREIGN KEY(linkId) REFERENCES links(id) ON DELETE CASCADE,
                        FOREIGN KEY(collectionId) REFERENCES collections(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                // Create indexes for link_collections
                db.execSQL("CREATE INDEX IF NOT EXISTS index_link_collections_linkId ON link_collections(linkId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_link_collections_collectionId ON link_collections(collectionId)")
            }
        }

        fun getDatabase(context: Context): LinkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LinkDatabase::class.java,
                    "link_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
