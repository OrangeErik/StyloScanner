package ru.tutorial.qrcodescannerpapyrus.data

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import java.util.stream.Collectors

@Entity(tableName = "doc_headers")
data class DocHeader(
	@ColumnInfo(name = "header_name") var headerName:String,
	@ColumnInfo(name = "header_description") var headerDescription: String,
	@ColumnInfo(name = "str_count") var strCount:Int,
	@ColumnInfo(name = "header_id") @PrimaryKey(autoGenerate = true) var headerId:Long = 0
)

@Entity(tableName = "doc_strings",
	foreignKeys = [
		ForeignKey(
			entity = DocHeader::class,
			parentColumns = ["header_id"],
			childColumns = ["doc_header_id"],
			onDelete = CASCADE)])
data class DocString(
	@ColumnInfo(name = "doc_string") var docString: String,
	@ColumnInfo(name = "doc_header_id") var docHeaderId: Long,
	@ColumnInfo(name = "str_id") @PrimaryKey(autoGenerate = true) var strId: Long = 0,
	@ColumnInfo(name = "goods_count") var goodsCount:Long = 1
);

@Entity(tableName = "goods")
data class GoodsEntity(
	@ColumnInfo(name = "goods_name") var goodsName:String,
	@ColumnInfo(name = "barcode") var barcode: String,
	@ColumnInfo(name = "goods_id") @PrimaryKey(autoGenerate = true) var headerId:Long = 0
//	@ColumnInfo(name = "goods_count") var strCount:Int,
);

@Entity(tableName = "goods_barcodes",
	foreignKeys = [
		ForeignKey(
			entity = GoodsEntity::class,
			parentColumns = ["goods_id"],
			childColumns = ["goods_id"],
			onDelete = CASCADE)])
data class GoodsBarcodeEntity(
	@ColumnInfo(name = "id") @PrimaryKey(autoGenerate = true) var id:Long = 0,
	@ColumnInfo(name = "goods_id") var goodsId:Long = 0,
	@ColumnInfo(name = "barcode") var barcode: String
);