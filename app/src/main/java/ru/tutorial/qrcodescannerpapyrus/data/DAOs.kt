package ru.tutorial.qrcodescannerpapyrus.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DocHeaderDao {
	@Transaction
	@Query("SELECT * FROM doc_headers")
	fun getAll():LiveData<List<DocHeader>>

	@Transaction
	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insert(vararg docHeader:DocHeader)

	@Transaction
	@Update
	fun Update(vararg docHeader: DocHeader);

	@Transaction
	@Query("DELETE FROM doc_headers")
	fun deleteAll();

	@Transaction
	@Query("DELETE FROM doc_headers WHERE header_id = :docId")
	fun deleteDoc(docId:Long);

	@Transaction
	@Query("SELECT * FROM doc_headers WHERE header_id = :docId")
	fun selectById(docId:Long):LiveData<List<DocHeader>>;

	@Transaction
	@Query("UPDATE doc_headers SET str_count = :newSize WHERE header_id = :docId")
	fun updateStrSizeByHeaderId(docId:Long, newSize:Int);
}

@Dao
interface DocStringDao {
	@Transaction
	@Query("SELECT * FROM doc_strings")
	fun getAll():LiveData<List<DocString>>

	@Transaction
	@Insert
	suspend fun insert(vararg docString:DocString)

	@Transaction
	@Update
	suspend fun update(vararg docString:DocString)

	@Transaction
	@Query("DELETE FROM doc_strings WHERE str_id = :strId")
	fun deleteStr(strId:Long);

	@Transaction
	@Query("SELECT * FROM doc_strings WHERE doc_header_id = :docHeaderId")
	fun selectAllByHeaderId(docHeaderId:Long):LiveData<List<DocString>>;

	@Transaction
	@Query("SELECT * FROM doc_strings WHERE doc_header_id = :docHeaderId")
	fun selectDataForExportByHeaderId(docHeaderId:Long):List<DocString>;
}

@Dao
interface GoodsDao {
	@Transaction
	@Insert
	suspend fun insertGoods(vararg goodsEntity:GoodsEntity)

	@Transaction
	@Insert
	suspend fun insertBarcodes(vararg goodsBarcodeEntity: GoodsBarcodeEntity)

	@Transaction
	@Query("SELECT * FROM goods")
	suspend fun getGoodsAndBarcodes():List<GoodsWithBarcodes>

	@Transaction
	@Query("SELECT goods_id FROM goods_barcodes WHERE barcode= :code")
	suspend fun getGoodsIdByBarcode(code:String):String

	@Transaction
	@Query("SELECT goods_name FROM goods WHERE goods_id = :id")
	suspend fun getGoodsNameById(id:String):String

	@Transaction
	@Query("DELETE FROM goods")
	suspend fun deleteAllGoods();

	@Transaction
	@Query("DELETE FROM goods_barcodes")
	suspend fun deleteAllBarcodes();
}