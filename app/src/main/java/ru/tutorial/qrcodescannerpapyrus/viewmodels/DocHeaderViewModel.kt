package ru.tutorial.qrcodescannerpapyrus.viewmodels


import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ru.tutorial.qrcodescannerpapyrus.KtApplication
import ru.tutorial.qrcodescannerpapyrus.data.DocHeader
import ru.tutorial.qrcodescannerpapyrus.repo.*
import java.io.InputStream

data class HeaderViewData(var docHeader: DocHeader, var selected:Boolean)


//Посредник межу визуалкой и репозиториями.
class DocHeaderViewModel(var repository: MainRepository = KtApplication.mainRepository): ViewModel() {
	val allHeaders: LiveData<List<DocHeader>>;

	init {
		allHeaders = repository.docHeaderRepo.allHeaders;
	}

	fun insert(docHeader: DocHeader) = viewModelScope.launch(Dispatchers.IO) {
		repository.docHeaderRepo.insert(docHeader);
	}

	fun update(docHeader: DocHeader) = viewModelScope.launch(Dispatchers.IO) {
		repository.docHeaderRepo.update(docHeader);
	}

	fun deleteByHeaderId(id: Long) = viewModelScope.launch(Dispatchers.IO) {
		repository.docHeaderRepo.deleteByHeaderId(id);
	}

	fun deleteList(idList: List<HeaderViewData>) = viewModelScope.launch(Dispatchers.IO) {
		repository.docHeaderRepo.deleteList(idList);
	}

	fun expDocs(selectedList: List<HeaderViewData>) = viewModelScope.async(Dispatchers.IO) {
		repository.takeExportData(
			selectedList.map { it.docHeader },
			ExportParam(DataFormat.CSV, ExpDirection.FILE)
		);
	}

	fun updateStrSizeByHeaderId(docId: Long, newSize: Int) = viewModelScope.launch(Dispatchers.IO) {
		repository.docHeaderRepo.updateStrSizeByHeaderId(docId, newSize);
	}

	fun loadGoodsFromStream(inputStream: InputStream) = viewModelScope.launch(Dispatchers.IO) {
		repository.goodsRepo.loadGoods(inputStream.bufferedReader().lineSequence());
	}
}