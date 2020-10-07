package ru.tutorial.qrcodescannerpapyrus.repo

import android.app.Application
import android.content.Context
import ru.tutorial.qrcodescannerpapyrus.KtApplication
import ru.tutorial.qrcodescannerpapyrus.data.DocHeader
import ru.tutorial.qrcodescannerpapyrus.data.DocStringDao
import ru.tutorial.qrcodescannerpapyrus.data.ExpData
import ru.tutorial.qrcodescannerpapyrus.data.GoodsDao
import timber.log.Timber

data class ExportParam(
	var dataFormat: DataFormat,
	var direction: ExpDirection
);

enum class ExpDirection{
	PPY,
	FILE,
	E_MAIL,
}

enum class DataFormat{
	JSON,
	CSV,
	XML;
}

class MainRepository(val context: Context, val docHeaderRepo:DocHeaderRepository, val docStringRepo:DocStringRepository, val goodsRepo:GoodsRepository) {
	fun takeExportData(docExpList:List<DocHeader>, param: ExportParam):String {
		val exp_list:MutableList<ExpData> = mutableListOf<ExpData>();
		for (doc_header in docExpList) {
			val exp_strings_list = docStringRepo.getDataForExportByHeaderId(doc_header.headerId).map { it.docString to it.goodsCount};
			val exp_data = ExpData(
				doc_header.headerName,
				doc_header.headerDescription,
				exp_strings_list
			);
			exp_list.add(exp_data);
		}
		val csv_data:String = expListToCSV(exp_list)
		Timber.i(csv_data);
		return csv_data;
	}

	fun expListToCSV(exportList:List<ExpData>):String {
		var csv_string:String = "";
		for(i:Int in exportList.indices) {
			if(i == 0) {
				csv_string += exportList[i].toCSV();
			}
			else {
				csv_string += exportList[i].toCSVWithoutHeader();
			}
		}
		return csv_string;
	}

	fun goodsCsvToEntity(rawData:String) {

	}
}