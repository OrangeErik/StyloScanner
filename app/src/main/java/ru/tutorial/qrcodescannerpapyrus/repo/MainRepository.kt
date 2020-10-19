package ru.tutorial.qrcodescannerpapyrus.repo

import android.content.Context
import ru.tutorial.qrcodescannerpapyrus.data.DocHeader
import ru.tutorial.qrcodescannerpapyrus.data.ExpData
import ru.tutorial.qrcodescannerpapyrus.data.ExpGoodsObject
import ru.tutorial.qrcodescannerpapyrus.data.GoodsExpData
import timber.log.Timber

data class ExportParam(var dataFormat: DataFormat, var direction: ExpDirection);
enum class ExpDirection{ PPY, FILE,E_MAIL,}
enum class DataFormat{ JSON, CSV, XML; }

class MainRepository(val context: Context, val docHeaderRepo:DocHeaderRepository, val docStringRepo:DocStringRepository, val goodsRepo:GoodsRepository) {
	suspend fun takeExportData(docExpList:List<DocHeader>, param: ExportParam):String {
		val exp_list:MutableList<GoodsExpData> = mutableListOf<GoodsExpData>();
		for (doc_header in docExpList) {
			val exp_strings_list = docStringRepo.getDataForExportByHeaderId(doc_header.headerId).map { ExpGoodsObject(barcode = it.docString, qtty = it.goodsCount)};
			for (expGoodsObject in exp_strings_list) {
				val goodsName:String? = goodsRepo.getGoodsNameByBarcode(expGoodsObject.barcode)
				val goodsId:String? = goodsRepo.getGoodsIdByBarcode(expGoodsObject.barcode)
				if(goodsName!= null)
					expGoodsObject.name = goodsName;
				if(goodsId!= null)
					expGoodsObject.id = goodsId;
			}
			val exp_data = GoodsExpData(doc_header.headerName, doc_header.headerDescription, exp_strings_list);
			exp_list.add(exp_data);
		}


//		val exp_list:MutableList<ExpData> = mutableListOf<ExpData>();
//		for (doc_header in docExpList) {
//			val exp_strings_list = docStringRepo.getDataForExportByHeaderId(doc_header.headerId).map { it.docString to it.goodsCount};
//			val exp_data = ExpData(
//				doc_header.headerName,
//				doc_header.headerDescription,
//				exp_strings_list
//			);
//			exp_list.add(exp_data);
//		}
//		val csv_data:String = expListToCSV(exp_list)

		val csv_data:String = expGoodsListToCSV(exp_list)
		Timber.i(csv_data);
		return csv_data;
	}

	fun expListToCSV(exportList:List<ExpData>):String {
		var csv_string:String = "";
		for(i:Int in exportList.indices) {
			if(i == 0) { csv_string += exportList[i].toCSV(); }
			else { csv_string += exportList[i].toCSVWithoutHeader(); }
		}
		return csv_string;
	}

	fun expGoodsListToCSV(expList:List<GoodsExpData>):String {
		var csv_string:String = "";
		for(i:Int in expList.indices) {
			if(i == 0) { csv_string += expList[i].toCSV(); }
			else { csv_string += expList[i].toCSVWithoutHeader(); }
		}
		return csv_string;
	}

}