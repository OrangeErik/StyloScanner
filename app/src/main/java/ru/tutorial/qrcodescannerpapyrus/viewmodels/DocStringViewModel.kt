package ru.tutorial.qrcodescannerpapyrus.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.tutorial.qrcodescannerpapyrus.KtApplication
import ru.tutorial.qrcodescannerpapyrus.data.DocString
import ru.tutorial.qrcodescannerpapyrus.repo.MainRepository

data class DocStringViewData(var docString: DocString, var selected:Boolean)

class DocStringViewModel(parentId:Long): ViewModel() {
	private var repository: MainRepository;
	lateinit var allStrings:LiveData<List<DocString>>;
	var headerId:Long = 0;

	init {
		repository = KtApplication.mainRepository;
		headerId = parentId;
		runBlocking {
			allStrings = repository.docStringRepo.getStrigById(headerId);
		}
	}

	fun insert(docString: DocString) = viewModelScope.launch(Dispatchers.IO) {
		repository.docStringRepo.insert(docString);
	}

	fun updateString(docString: DocString) = viewModelScope.launch(Dispatchers.IO) {
		repository.docStringRepo.update(docString);
	}

	fun deleteByHeaderId(id:Long) = viewModelScope.launch(Dispatchers.IO){
		repository.docStringRepo.deleteByHeaderId(id);
	}

	fun deleteList(idList: List<DocStringViewData>) = viewModelScope.launch(Dispatchers.IO) {
		repository.docStringRepo.deleteList(idList);
	}


	//TODO доделать
	fun takeGoods(goodsCode:String) = viewModelScope.async(Dispatchers.IO) {
		repository.goodsRepo.getGoodsNameByBarcode(goodsCode)
//		GoodsEntity(goodsId = "new", goodsName = "hello"); //@ЗАГЛУШКА
	}
}