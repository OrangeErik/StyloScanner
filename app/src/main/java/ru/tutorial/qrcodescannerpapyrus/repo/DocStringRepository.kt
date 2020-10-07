package ru.tutorial.qrcodescannerpapyrus.repo

import androidx.lifecycle.LiveData
import ru.tutorial.qrcodescannerpapyrus.data.DocString
import ru.tutorial.qrcodescannerpapyrus.data.DocStringDao
import ru.tutorial.qrcodescannerpapyrus.viewmodels.DocStringViewData


class DocStringRepository(private val docStringDao:DocStringDao) {
	lateinit var allStrings: LiveData<List<DocString>>;

	suspend fun insert(docString:DocString) {
		docStringDao.insert(docString);
	}

	suspend fun update(docString:DocString) {
		docStringDao.update(docString);
	}

	suspend fun deleteByHeaderId(id:Long) {
		docStringDao.deleteStr(id);
	}

	suspend fun getStrigById(headerId:Long):LiveData<List<DocString>> {
		allStrings = docStringDao.selectAllByHeaderId(headerId);
		return allStrings;
	}

	fun getDataForExportByHeaderId(headerId:Long):List<DocString> {
		return docStringDao.selectDataForExportByHeaderId(headerId);
	}

	suspend fun deleteList(idList: List<DocStringViewData>) {
		idList.forEach {
			docStringDao.deleteStr(it.docString.strId);
		}
	}
}

//class DocStringRepository(private val docStringDao:DocStringDao, headerId:Long) {
//	val allStrings: LiveData<List<DocString>> = docStringDao.selectAllByHeaderId(headerId);
//
//	suspend fun insert(docString:DocString) {
//		docStringDao.insert(docString);
//	}
//
//	suspend fun deleteByHeaderId(id:Long) {
//		docStringDao.deleteDoc(id);
//	}
//}