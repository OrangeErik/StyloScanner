package ru.tutorial.qrcodescannerpapyrus

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import ru.tutorial.qrcodescannerpapyrus.data.AppDatabase
import ru.tutorial.qrcodescannerpapyrus.repo.DocHeaderRepository
import ru.tutorial.qrcodescannerpapyrus.repo.DocStringRepository
import ru.tutorial.qrcodescannerpapyrus.repo.GoodsRepository
import ru.tutorial.qrcodescannerpapyrus.repo.MainRepository
import ru.tutorial.qrcodescannerpapyrus.util.Constants
import timber.log.Timber

class KtApplication:Application() {
	companion object {
		var database:AppDatabase? = null;
		lateinit var settings:SharedPreferences;
		lateinit var mainRepository:MainRepository;
	}

	override fun onCreate() {
		super.onCreate();
		Timber.plant(Timber.DebugTree());
		database = synchronized(this) {
			val instance:AppDatabase = Room.databaseBuilder(
				this.applicationContext, AppDatabase::class.java, "docs_database")
				.addMigrations(AppDatabase.MIGRATION_1_2).build();
			instance;
		}

		mainRepository = MainRepository(
			this,
			DocHeaderRepository(database!!.docHeaderDao()),
			DocStringRepository(database!!.docStringDao()),
			GoodsRepository(database!!.goodsDao()));
		settings = getSharedPreferences(Constants.SETTING_FILE_NAME, Context.MODE_PRIVATE);
	}
}