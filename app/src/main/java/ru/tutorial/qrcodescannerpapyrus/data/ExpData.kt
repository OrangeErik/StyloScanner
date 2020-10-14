package ru.tutorial.qrcodescannerpapyrus.data

import ru.tutorial.qrcodescannerpapyrus.util.Constants

data class ExpData(
	var docName:String = "",
	var docDescr:String = "",
	var data: List<Pair<String, Long>> = listOf<Pair<String, Long>>()
) {
	//return CSV string
	fun toCSV(): String {
		var outer:String = "${Constants.str_doc_name};${Constants.str_doc_descr};${Constants.str_code};${Constants.str_goods_count}\n";
		for(i:Int in data.indices) {
			outer += "$docName;${if(docDescr.length > 0) docDescr else " "};${data[i].first};${data[i].second.toString()}\n"
		}
		return outer;
	}

	fun toCSVWithoutHeader():String {
		var outer:String = "";
		for(i:Int in data.indices) {
			outer += "$docName;$docDescr;${data[i].first};${data[i].second.toString()}\n"
		}
		return outer;
	}
}

