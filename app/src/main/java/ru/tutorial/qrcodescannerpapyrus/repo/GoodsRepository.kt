package ru.tutorial.qrcodescannerpapyrus.repo

import ru.tutorial.qrcodescannerpapyrus.data.*
import timber.log.Timber
import java.io.InputStream

interface goods {
	suspend fun loadGoods(data: Sequence<String>)
	fun giveGoodsByBarcode(barcode:String):GoodsEntity
}

class GoodsRepository(private val goodsDao: GoodsDao):goods {
	override suspend fun loadGoods(data: Sequence<String>) {
		data.forEach {
			val data_list = it.split(";");
			goodsDao.insert(GoodsEntity(data_list[0], data_list[1]));
		}
	}

	override fun giveGoodsByBarcode(barcode: String): GoodsEntity {
		TODO("Not yet implemented")
	}
}