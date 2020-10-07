package ru.tutorial.qrcodescannerpapyrus.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DocStringViewModelFactory(private val parentId:Long):ViewModelProvider.Factory {
	override fun <T : ViewModel?> create(modelClass: Class<T>): T {
		if(modelClass.isAssignableFrom(DocStringViewModel::class.java)) {
			return DocStringViewModel(parentId) as T;
		}
		throw IllegalArgumentException("Unknown ViewModel class");
	}
}