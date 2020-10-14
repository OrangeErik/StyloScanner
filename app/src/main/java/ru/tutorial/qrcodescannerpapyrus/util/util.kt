package ru.tutorial.qrcodescannerpapyrus.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

fun <T:ViewModel>bindViewModel(owner: ViewModelStoreOwner, factory:ViewModelProvider.Factory?, javaClass:Class<T>): ViewModel {
	if(factory != null)
		return ViewModelProvider(owner, factory).get(javaClass);
	else
		return ViewModelProvider(owner).get(javaClass);
}