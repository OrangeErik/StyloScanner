package ru.tutorial.qrcodescannerpapyrus.view

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.dialog_new_doc.view.*
import kotlinx.android.synthetic.main.main_activity.*
import kotlinx.android.synthetic.main.scan_setting_dialog.view.*
import kotlinx.coroutines.*
import ru.tutorial.qrcodescannerpapyrus.KtApplication
import ru.tutorial.qrcodescannerpapyrus.R
import ru.tutorial.qrcodescannerpapyrus.adapters.DocHeaderListAdapter
import ru.tutorial.qrcodescannerpapyrus.data.DocHeader
import ru.tutorial.qrcodescannerpapyrus.util.Constants
import ru.tutorial.qrcodescannerpapyrus.util.bindViewModel
import ru.tutorial.qrcodescannerpapyrus.viewmodels.DocHeaderViewModel
import timber.log.Timber
import java.io.File
import java.io.FileWriter

class DocumentsListActivity : AppCompatActivity() {
	companion object {
		private const val TAKE_GOODS_ActivityRequestCode = 2;
		private const val cameraPermissionRequestCode = 3;
		private const val SHARE_CSV_ActivityRequestCode = 4;
	}

	private lateinit var activityViewModel: ViewModel;
	private lateinit var rvAdapter: DocHeaderListAdapter
	private var actionMode: ActionMode? = null
	lateinit var iMM:InputMethodManager;

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		activityViewModel = bindViewModel(this, null, DocHeaderViewModel::class.java)
		rvAdapter = DocHeaderListAdapter(this, activityViewModel as DocHeaderViewModel);
		val inflater:LayoutInflater = LayoutInflater.from(this);
		iMM = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager;
		doc_header_rv.apply {
			adapter = rvAdapter;
			layoutManager = LinearLayoutManager(this@DocumentsListActivity);
			addItemDecoration(DividerItemDecoration(this@DocumentsListActivity, DividerItemDecoration.HORIZONTAL))
		}
		(activityViewModel as DocHeaderViewModel).allHeaders.observe(this, Observer { headers ->
			headers?.let {
				rvAdapter.setDocHeaders(it);
				doc_header_rv.scrollToPosition(rvAdapter.itemCount-1);
			}
		});

		KtApplication.settings.apply {
			if(!contains(Constants.PREF_KEY_USE_CAMERA)) {
				var use_camera = false;
				val welcome_dialog = inflater.inflate(R.layout.scan_setting_dialog, null);
				val builder = AlertDialog.Builder(this@DocumentsListActivity).setView(welcome_dialog);
				welcome_dialog.checkbox_have_scanner.isChecked = use_camera;

				val alert_dlg = builder.show();
				welcome_dialog.ok_button_scan_setting.setOnClickListener {
					alert_dlg.dismiss();
					use_camera = welcome_dialog.checkbox_have_scanner.isChecked;
					val editor = edit();
					if(use_camera) {
						if (ContextCompat.checkSelfPermission(this@DocumentsListActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
							ActivityCompat.requestPermissions(this@DocumentsListActivity, arrayOf(Manifest.permission.CAMERA), cameraPermissionRequestCode)
						}
						editor.putBoolean(Constants.PREF_KEY_USE_CAMERA, true);
					}
					else {
						editor.putBoolean(Constants.PREF_KEY_USE_CAMERA, false);
					}
					editor.apply();

				}
			}
		}

		fab.setOnClickListener { view ->
			val dialog_view = LayoutInflater.from(this).inflate(R.layout.dialog_new_doc, null);
			val builder = AlertDialog.Builder(this).setView(dialog_view);
			val alert_dialog = builder.show();

			fun inputComplete()
			{
				alert_dialog.dismiss();
				val doc_name = dialog_view.new_doc_name_et.text.toString();
				val doc_descr = dialog_view.new_doc_descr_et.text.toString();
				if(doc_name.isNotEmpty()) {
					(activityViewModel as DocHeaderViewModel).insert(DocHeader(doc_name, doc_descr, 0));
					doc_header_rv.scrollToPosition(rvAdapter.itemCount-1);
				}
				else {
					Toast.makeText(applicationContext, R.string.empty_not_saved, Toast.LENGTH_LONG).show();
				}
			}

			dialog_view.new_doc_descr_et.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					val focus_view: View = alert_dialog.currentFocus!!;
					iMM.hideSoftInputFromWindow(view.windowToken, 0)
					inputComplete()
				}
				false
			});

			dialog_view.ok_button_new_doc.setOnClickListener {
				inputComplete();
			}
		}

		//@ERIK UPDATE {
		rvAdapter.onItemClick = {
			enableActionMode(it)
		}
		rvAdapter.onItemLongClick = {
			enableActionMode(it)
		}
		// } @ERIK UPDATE
	}

	override fun onCreateOptionsMenu(menu: Menu?): Boolean {
		menuInflater.inflate(R.menu.menu, menu);
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when(item.itemId) {
			R.id.action_scan_setting -> {
				KtApplication.settings.let {setting ->
					val inflater = LayoutInflater.from(this@DocumentsListActivity);
					var use_camera = setting.getBoolean(Constants.PREF_KEY_USE_CAMERA, false);
					val welcome_dialog = inflater.inflate(R.layout.scan_setting_dialog, null);
					val builder = AlertDialog.Builder(this@DocumentsListActivity)
						.setView(welcome_dialog);
					welcome_dialog.checkbox_have_scanner.isChecked = use_camera;
					val alert_dlg = builder.show();
					welcome_dialog.ok_button_scan_setting.setOnClickListener {
						use_camera = welcome_dialog.checkbox_have_scanner.isChecked;
						val editor = setting.edit();
						editor.putBoolean(Constants.PREF_KEY_USE_CAMERA, use_camera);
						editor.apply();
						alert_dlg.dismiss();
					}
				}
			}
			R.id.load_goods_handbook_menuitem -> {
//				runBlocking {
//					val intent = Intent()
//						.addCategory(Intent.CATEGORY_OPENABLE)
//						.setType("text/csv")
//						.setAction(Intent.ACTION_OPEN_DOCUMENT)
//
//					startActivityForResult(Intent.createChooser(intent, "Open CSV"), TAKE_GOODS);
//				}

				GlobalScope.launch {
					val intent = Intent()
						.addCategory(Intent.CATEGORY_OPENABLE)
						.setType("text/csv")
						.setAction(Intent.ACTION_OPEN_DOCUMENT)
					startActivityForResult(Intent.createChooser(intent, "Open CSV"), TAKE_GOODS_ActivityRequestCode);
				}
			}
		}
		return super.onOptionsItemSelected(item)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if(requestCode == TAKE_GOODS_ActivityRequestCode && resultCode == RESULT_OK) {
			val selectedFilename = data?.data //The uri with the location of the file
			if (selectedFilename != null) {
				val raw_goods_stream = contentResolver.openInputStream(selectedFilename);
				if(raw_goods_stream != null) {
					(activityViewModel as DocHeaderViewModel).loadGoodsFromStream(raw_goods_stream);
				}
			} else {
				Toast.makeText(applicationContext, "Null filename data received!", Toast.LENGTH_LONG)
					.show()
			}
		}
		else if(requestCode == SHARE_CSV_ActivityRequestCode && resultCode == RESULT_OK) {
			val share_dir = File(filesDir, "export")
			if(share_dir.exists()) {
				val share_file = File(share_dir, "Scan_doc_list.csv");
				if(share_file.exists()) {
					share_file.delete();
					Toast.makeText(applicationContext, "Data was exported!", Toast.LENGTH_SHORT)
						.show()
				}
			}
		}
	}

	//@ERIK UPDATE {
	private fun enableActionMode(position: Int) {
		if (actionMode == null)
			actionMode = startSupportActionMode(object : ActionMode.Callback {
				override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
					if (item?.itemId == R.id.action_doc_delete) {
						rvAdapter.deleteDocs();
						Timber.i("DATA WAS DELETED");
						Toast.makeText(this@DocumentsListActivity, "Data was deleted", Toast.LENGTH_LONG)
							.show();
						mode?.finish()
						return true
					} else if (item?.itemId == R.id.action_doc_share) {
						GlobalScope.launch {
							val exp_data = rvAdapter.expDocs().await();
							shareCsvData(exp_data);
						}
					}
					return false
				}

				override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
					mode?.menuInflater?.inflate(R.menu.rv_docs_menu, menu)
					return true
				}

				override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
					return false
				}

				override fun onDestroyActionMode(mode: ActionMode?) {
					rvAdapter.selectedItems.clear()
					rvAdapter.docHeadersList
						.filter { it.selected }
						.forEach { it.selected = false }

					rvAdapter.notifyDataSetChanged()
					actionMode = null
				}
			})
		rvAdapter.toggleSelection(position)
		val size = rvAdapter.selectedItems.size()
		if (size == 0) {
			actionMode?.finish()
		} else {
			actionMode?.title = "$size"
			actionMode?.invalidate()
		}
	}

	// } @ERIK UPDATE
	fun shareCsvData(csvData: String) {
		val share_dir = File(filesDir, "export")
		if(!share_dir.exists()) {
			share_dir.mkdir();
		}
		val share_file = File(share_dir, "Scan_doc_list.csv");
		var file_writer: FileWriter? = null;
		try {
			file_writer = FileWriter(share_file);
			file_writer.write(csvData)
			file_writer.close();
			val uri = FileProvider.getUriForFile(this, "ru.tutorial.qrcodescannerpapyrus.fileprovider", share_file);
//			val send_intent = Intent(Intent.ACTION_SEND);
			Intent(Intent.ACTION_SEND).apply {
				type = "text/csv"
				putExtra(Intent.EXTRA_STREAM, uri)
				startActivityForResult(this, SHARE_CSV_ActivityRequestCode);
			}

		}catch (e: Exception) {
			Timber.e("Writing CSV error!");
			Timber.e(e);
			e.printStackTrace();
			file_writer?.close();
			share_file.delete();

		}
	}
}