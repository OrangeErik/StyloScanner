package ru.tutorial.qrcodescannerpapyrus.data


import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
	entities = [
		(DocHeader::class),
		(GoodsEntity::class),
		(DocString::class),
		(GoodsBarcodeEntity::class)
	],
	version = 2,
	exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
	abstract fun docHeaderDao():DocHeaderDao
	abstract fun goodsDao(): GoodsDao
	abstract fun docStringDao(): DocStringDao

	companion object {
		@JvmField
		val MIGRATION_1_2 = Migration1To2();
	}
}


