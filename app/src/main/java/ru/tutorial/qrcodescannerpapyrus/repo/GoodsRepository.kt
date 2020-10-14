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
		goodsDao.deleteAll();
		val list = data.toList();
		for(i in 1..list.lastIndex) {
			val data_list = list[i].split(";");
			if(data_list.size == 2)
				goodsDao.insert(GoodsEntity(data_list[0], data_list[1]));
		}
	}

	override fun giveGoodsByBarcode(barcode: String): GoodsEntity {
		return goodsDao.getGoodByCode(barcode)
	}
}