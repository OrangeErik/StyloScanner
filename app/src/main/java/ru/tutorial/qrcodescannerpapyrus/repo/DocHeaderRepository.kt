package ru.tutorial.qrcodescannerpapyrus.repo

import androidx.lifecycle.LiveData
import ru.tutorial.qrcodescannerpapyrus.data.DocHeader
import ru.tutorial.qrcodescannerpapyrus.data.DocHeaderDao
import ru.tutorial.qrcodescannerpapyrus.viewmodels.HeaderViewData


class DocHeaderRepository(private val docHeaderDao:DocHeaderDao) {
	val allHeaders: LiveData<List<DocHeader>> = docHeaderDao.getAll();

	suspend fun insert(docHeader:DocHeader) {
		docHeaderDao.insert(docHeader);
	}

	suspend fun update(docHeader: DocHeader) {
		docHeaderDao.Update(docHeader);
	}

	suspend fun deleteByHeaderId(id:Long) {
		docHeaderDao.deleteDoc(id);
	}

	suspend fun deleteList(idList: List<HeaderViewData>) {
		idList.forEach {
			docHeaderDao.deleteDoc(it.docHeader.headerId);
		}
	}

	suspend fun updateStrSizeByHeaderId(docId:Long, newSize:Int) {
		docHeaderDao.updateStrSizeByHeaderId(docId, newSize);
	}
}