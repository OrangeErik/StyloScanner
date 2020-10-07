package ru.tutorial.qrcodescannerpapyrus.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.util.isEmpty
import androidx.core.util.isNotEmpty
import androidx.recyclerview.widget.RecyclerView
import ru.tutorial.qrcodescannerpapyrus.R
import ru.tutorial.qrcodescannerpapyrus.data.DocString
import ru.tutorial.qrcodescannerpapyrus.viewmodels.DocStringViewModel
import ru.tutorial.qrcodescannerpapyrus.viewmodels.DocStringViewData

class DocStringListAdapter internal constructor(context: Context, viewModelHandler: DocStringViewModel) : RecyclerView.Adapter<DocStringListAdapter.DocStringViewHolder>() {
	private val inflater: LayoutInflater = LayoutInflater.from(context)
	var docStringsList = emptyList<DocStringViewData>();
	private var docStringsViewModel = viewModelHandler;
//	private var ctx = context;

	//for selected items
	val selectedItems = SparseBooleanArray()
	private var currentSelectedPos: Int = -1

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocStringViewHolder {
		val itemView = inflater.inflate(R.layout.item_rv_string, parent, false);
		return DocStringViewHolder(itemView);
	}

	override fun onBindViewHolder(holder: DocStringViewHolder, position: Int) {
		val current_doc_string:DocStringViewData = docStringsList[position];
		holder.onBind(current_doc_string, position)
	}

	fun setDocStrings(docStrings: List<DocString>) {
		this.docStringsList = docStrings.map { DocStringViewData(it, false) };
		notifyDataSetChanged();
	}

	override fun getItemCount():Int {
		return docStringsList.size;
	}

	//@select
	var onEditItemClick:((DocString) -> Unit)? = null
	var onItemClick: ((Int) -> Unit)? = null
	var onItemLongClick: ((Int) -> Unit)? = null

	fun toggleSelection(position: Int) {
		currentSelectedPos = position
		if (selectedItems[position, false]) {
			selectedItems.delete(position)
			docStringsList[position].selected = false
		} else {
			selectedItems.put(position, true)
			docStringsList[position].selected = true
		}
		notifyItemChanged(position)
	}

	fun deleteStrings() {
		val selected_list= docStringsList.filter { it.selected };
		docStringsViewModel.deleteList(selected_list)
		notifyDataSetChanged()
		currentSelectedPos = -1
	}

	inner class DocStringViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val docStringDescrView: TextView = itemView.findViewById(R.id.string_item_descr);
		val docStringCountView: TextView = itemView.findViewById(R.id.string_item_count);
		val docStringIndexView: TextView = itemView.findViewById(R.id.string_item_idx);

		fun onBind(stringViewData: DocStringViewData, position: Int) {
			docStringDescrView.text = stringViewData.docString.docString;
			docStringIndexView.text = (position+1).toString();
			docStringCountView.text = stringViewData.docString.goodsCount.toString();

			if (stringViewData.selected) {
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

			//@select{
			itemView.setOnClickListener {
				if (selectedItems.isNotEmpty())
					onItemClick?.invoke(position)
				else {
					onEditItemClick?.invoke(stringViewData.docString)
				}
			}
			itemView.setOnLongClickListener {
				onItemLongClick?.invoke(position)
				return@setOnLongClickListener true
			}
			// }
		}
	}
}