package ru.tutorial.qrcodescannerpapyrus.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.util.isNotEmpty
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.dialog_new_doc.view.*
import kotlinx.coroutines.Deferred
import ru.tutorial.qrcodescannerpapyrus.*
import ru.tutorial.qrcodescannerpapyrus.data.DocHeader
import ru.tutorial.qrcodescannerpapyrus.util.Constants
import ru.tutorial.qrcodescannerpapyrus.view.StringListActivity
import ru.tutorial.qrcodescannerpapyrus.viewmodels.DocHeaderViewModel
import ru.tutorial.qrcodescannerpapyrus.viewmodels.HeaderViewData

class DocHeaderListAdapter internal constructor(context: Context, viewModelHandler: DocHeaderViewModel) : RecyclerView.Adapter<DocHeaderListAdapter.DocHeaderViewHolder>() {
	private val inflater: LayoutInflater = LayoutInflater.from(context)
	var docHeadersList = emptyList<HeaderViewData>();
	var docHeaderViewModel = viewModelHandler;
	private var ctx = context;

	//for selected items
	val selectedItems = SparseBooleanArray()
	private var currentSelectedPos: Int = -1

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocHeaderViewHolder {
		val itemView = inflater.inflate(R.layout.item_rv_header, parent, false)
		return DocHeaderViewHolder(itemView)
	}

	override fun onBindViewHolder(holder: DocHeaderViewHolder, position: Int) {
		val current_header_view_data = docHeadersList[position];
		holder.onBind(current_header_view_data);
	}

	override fun getItemCount():Int {
		return docHeadersList.size;
	}

	fun setDocHeaders(docHeaders: List<DocHeader>) {
		this.docHeadersList = docHeaders.map { HeaderViewData(it, false) };
		notifyDataSetChanged();
	}

	//TODO
	fun editHeader(headerViewData:HeaderViewData) {
		val mutable_doc_header = headerViewData.docHeader
		val dialog_view = LayoutInflater.from(ctx).inflate(R.layout.dialog_new_doc, null)
		dialog_view.new_doc_tw.text = "Редактирование документа";
		dialog_view.new_doc_name_et.append(mutable_doc_header.headerName)
		dialog_view.new_doc_descr_et.append(mutable_doc_header.headerDescription)
		val builder = AlertDialog.Builder(ctx).setView(dialog_view)
		val alert_dialog = builder.show();

		dialog_view.ok_button_new_doc.setOnClickListener {
			alert_dialog.dismiss();
			val doc_name = dialog_view.new_doc_name_et.text.toString();
			val doc_descr = dialog_view.new_doc_descr_et.text.toString();
			if(doc_name.isNotEmpty()) {
				if(doc_name != mutable_doc_header.headerName)
					mutable_doc_header.headerName = doc_name;
				if(doc_descr != mutable_doc_header.headerDescription)
					mutable_doc_header.headerDescription = doc_descr;
				docHeaderViewModel.update(mutable_doc_header);
			}
		}
	}
	fun goToStrList(headerViewData:HeaderViewData) {
		val intent = Intent(ctx, StringListActivity::class.java);
		intent.putExtra("PARENT_ID", headerViewData.docHeader.headerId);
		ctx.startActivity(intent);
	}
	//@select
	var onItemClick: ((Int) -> Unit)? = null
	var onItemLongClick: ((Int) -> Unit)? = null

	fun toggleSelection(position: Int) {
		currentSelectedPos = position
		if (selectedItems[position, false]) {
			selectedItems.delete(position)
			docHeadersList[position].selected = false
		} else {
			selectedItems.put(position, true)
			docHeadersList[position].selected = true
		}
		notifyItemChanged(position)
	}

	fun deleteDocs() {
		val selected_list= docHeadersList.filter { it.selected };
		this.docHeaderViewModel.deleteList(selected_list)
		notifyDataSetChanged()
		currentSelectedPos = -1
	}

	fun expDocs():Deferred<String>{
		val selected_list = docHeadersList.filter { it.selected };
		return this.docHeaderViewModel.expDocs(selected_list);
	}

	inner class DocHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val docHeaderNameView: TextView = itemView.findViewById(R.id.doc_header_name_tw);
		val docHeaderDescrView: TextView = itemView.findViewById(R.id.doc_header_descr_tw);
		val docHeaderStrCount:TextView = itemView.findViewById(R.id.doc_header_str_count)
//		val scanStrListButton:ImageButton = itemView.findViewById(R.id.doc_header_add_str_btn);
//		val scanStrListButton:CardView = itemView.findViewById(R.id.doc_header_card_view);
		val editHeaderButton:ImageButton = itemView.findViewById(R.id.doc_header_edit_btn);

		fun onBind(headerViewData:HeaderViewData) {
			docHeaderNameView.text = headerViewData.docHeader.headerName;
			docHeaderDescrView.text = headerViewData.docHeader.headerDescription;
			docHeaderStrCount.text = "${Constants.str_str_count} ${headerViewData.docHeader.strCount}"
//			scanStrListButton.setOnClickListener { goToStrList(headerViewData); }
			editHeaderButton.setOnClickListener { editHeader(headerViewData); }

			//@select{
			itemView.setOnClickListener {
				if (selectedItems.isNotEmpty()) {
					onItemClick?.invoke(position)
				}
				else {
					goToStrList(headerViewData);
				}
			}
			itemView.setOnLongClickListener {
				onItemLongClick?.invoke(position)
				return@setOnLongClickListener true
			}

			if (headerViewData.selected) {
				itemView.background = GradientDrawable().apply {
					shape = GradientDrawable.RECTANGLE
					cornerRadius = 32f
					setColor(Color.rgb(232, 240, 253))
				}
			} else {
				itemView.background = GradientDrawable().apply {
					shape = GradientDrawable.RECTANGLE
					cornerRadius = 32f
					setColor(Color.WHITE)
				}
			}
			// }
		}
	}
}