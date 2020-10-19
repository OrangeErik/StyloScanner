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


//Main class. В него закинуты все основные внешние зависимости: От базы данных и от основного функционала.
//Испольован паттерн ServiceLocator. Т.е Это одна основная глобальная точнка для вызова всехго функционала из приложения

class KtApplication:Application() {

	//Это аналог Static полей. В Kotlin Static полей нет, но
	// Есть Companion object, внутри которого лежат поля и методы, которые можно вызвать из вне, не
	// создавая объект класса
	companion object {
		var database:AppDatabase? = null;
		lateinit var settings:SharedPreferences;
		lateinit var mainRepository:MainRepository;
	}

	override fun onCreate() {
		super.onCreate();
		Timber.plant(Timber.DebugTree()); // Система логирования. Большенство логов выводится с ее помощью

		database = synchronized(this) {
			val instance:AppDatabase = Room.databaseBuilder(
				this.applicationContext, AppDatabase::class.java, "docs_database")
				.addMigrations(AppDatabase.MIGRATION_1_2).build();
			instance;
		}


		//Основной репозиторий. Из него можно получить достуцп к любому из репозиториев.
		mainRepository = MainRepository(
			this,
			DocHeaderRepository(database!!.docHeaderDao()),
			DocStringRepository(database!!.docStringDao()),
			GoodsRepository(database!!.goodsDao()));

		// Хранилище настроек в виде Key-Value
		settings = getSharedPreferences(Constants.SETTING_FILE_NAME, Context.MODE_PRIVATE);
	}
}