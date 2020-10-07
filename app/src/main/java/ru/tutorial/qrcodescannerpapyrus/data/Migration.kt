package ru.tutorial.qrcodescannerpapyrus.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration1To2: Migration(1,2) {
	override fun migrate(database: SupportSQLiteDatabase) {
		database.execSQL("ALTER TABLE doc_strings ADD COLUMN goods_count INTEGER DEFAULT 1 NOT NULL");
	}
}