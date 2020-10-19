package ru.tutorial.qrcodescannerpapyrus.repo

import android.util.Log
import ru.tutorial.qrcodescannerpapyrus.data.*
import timber.log.Timber
import java.io.InputStream

interface goods {
	suspend fun loadGoods(data: Sequence<String>)
	suspend fun getGoodsNameByBarcode(barcode:String):String
	suspend fun getGoodsIdByBarcode(barcode:String):String
}

class GoodsRepository(private val goodsDao: GoodsDao):goods {
	override suspend fun loadGoods(data: Sequence<String>) {
		goodsDao.deleteAllGoods();
		goodsDao.deleteAllBarcodes()
		val list = data.toList();
		for(i in 1..list.lastIndex) {
			val data_list = list[i].split(";");
			if(data_list.size == 3)
				goodsDao.insertGoods(GoodsEntity(goodsId = data_list[0], goodsName = data_list[1]));
				goodsDao.insertBarcodes(GoodsBarcodeEntity(goodsId = data_list[0], barcode = data_list[2]))
		}
	}

	override suspend fun getGoodsIdByBarcode(barcode:String):String {
		return goodsDao.getGoodsIdByBarcode(barcode);
	}

//	override fun giveGoodsByBarcode(barcode: String): GoodsEntity {
////		return goodsDao.getGoodByCode(barcode)
//		return goodsDao.getGoodsAndBarcodes();
//	}

	override suspend fun getGoodsNameByBarcode(barcode: String):String {
		val goods_id = getGoodsIdByBarcode(barcode);
		return goodsDao.getGoodsNameById(goods_id);
//		return goodsDao.getGoodByCode(barcode)
//		val goods_and_barcodes =  goodsDao.getGoodsAndBarcodes();
//		for (goodsAndBarcode in goods_and_barcodes) {
//			Timber.i(goodsAndBarcode.toString())
//		}
	}
}