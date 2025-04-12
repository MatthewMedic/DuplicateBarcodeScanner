package com.darksphere.duplicatescanner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darksphere.duplicatescanner.data.Barcode
import com.darksphere.duplicatescanner.data.BarcodeList
import com.darksphere.duplicatescanner.data.BarcodeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ScanResult {
    data class Success(val barcode: String) : ScanResult()
    data class Duplicate(val barcode: String) : ScanResult()
    data class Error(val message: String) : ScanResult()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: BarcodeRepository
) : ViewModel() {

    private val _scanResult = MutableStateFlow<ScanResult?>(null)
    val scanResult: StateFlow<ScanResult?> = _scanResult.asStateFlow()

    private val _barcodeLists = MutableStateFlow<List<BarcodeList>>(emptyList())
    val barcodeLists: StateFlow<List<BarcodeList>> = _barcodeLists.asStateFlow()

    private val _barcodes = MutableStateFlow<List<Barcode>>(emptyList())
    val barcodes: StateFlow<List<Barcode>> = _barcodes.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllLists().collect { lists ->
                _barcodeLists.value = lists
            }
        }
    }

    fun loadBarcodesForList(listId: Long) {
        viewModelScope.launch {
            repository.getBarcodesForList(listId).collect { barcodes ->
                _barcodes.value = barcodes
            }
        }
    }

    fun createList(name: String) {
        viewModelScope.launch {
            repository.createList(name)
        }
    }

    fun deleteList(listId: Long) {
        viewModelScope.launch {
            repository.deleteList(listId)
        }
    }

    fun onBarcodeDetected(barcode: String) {
        viewModelScope.launch {
            // Since we don't have a direct way to check for duplicates across all lists,
            // we'll just proceed with Success for now
            _scanResult.value = ScanResult.Success(barcode)
        }
    }

    fun processBarcode(barcode: String, listId: Long?) {
        viewModelScope.launch {
            if (listId != null) {
                val result = repository.addBarcode(listId, barcode)
                result.fold(
                    onSuccess = { _scanResult.value = ScanResult.Success(barcode) },
                    onFailure = { _scanResult.value = ScanResult.Duplicate(barcode) }
                )
            }
        }
    }

    fun clearScanResult() {
        _scanResult.value = null
    }
} 