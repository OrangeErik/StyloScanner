package ru.tutorial.qrcodescannerpapyrus.view
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.*
import kotlinx.android.synthetic.main.custom_camera_scanner.*
import kotlinx.android.synthetic.main.rv_strings.*
import kotlinx.android.synthetic.main.dialog_new_string.view.*
import ru.tutorial.qrcodescannerpapyrus.KtApplication
import ru.tutorial.qrcodescannerpapyrus.R
import ru.tutorial.qrcodescannerpapyrus.adapters.DocStringListAdapter
import ru.tutorial.qrcodescannerpapyrus.data.DocString
import ru.tutorial.qrcodescannerpapyrus.util.Constants
import ru.tutorial.qrcodescannerpapyrus.viewmodels.DocHeaderViewModel
import ru.tutorial.qrcodescannerpapyrus.viewmodels.DocStringViewModel
import ru.tutorial.qrcodescannerpapyrus.viewmodels.DocStringViewModelFactory
import timber.log.Timber
import java.util.*

class StringListActivity: AppCompatActivity() {
	private var parentId:Long = 0;
	lateinit var adapter:DocStringListAdapter
	private var actionMode: ActionMode? = null
	private lateinit var activityViewModel: ViewModel;
	private var captureManager: CaptureManager? = null;
	lateinit var beepManager: BeepManager;
	private var scanContinuousState: Boolean = false;
	private lateinit var scanContinuousBG: Drawable;
	private var lastScan = Date();
	var useCamera:Boolean = false;
	var itemCount:Int = 0;
	lateinit var iMM:InputMethodManager;

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState);

		//init variables
		val inflater = LayoutInflater.from(this@StringListActivity);
		useCamera = KtApplication.settings.getBoolean(Constants.PREF_KEY_USE_CAMERA, false);
		setContentView(R.layout.rv_strings);
		parentId = intent.getLongExtra("PARENT_ID", 0);
		val docStringViewModelFactory = DocStringViewModelFactory(parentId);
		activityViewModel = ViewModelProvider(this, docStringViewModelFactory).get(DocStringViewModel::class.java);
		adapter = DocStringListAdapter(this, activityViewModel as DocStringViewModel);
		iMM = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager;

		//rv_setting
		doc_custom_str_rv.adapter = adapter;
		doc_custom_str_rv.layoutManager = LinearLayoutManager(this);
		val doc_header_vm = ViewModelProvider(this@StringListActivity).get(DocHeaderViewModel::class.java);
		(activityViewModel as DocStringViewModel).allStrings.observe(this@StringListActivity, Observer { str ->
			str.let {
				adapter.setDocStrings(it);
				if(itemCount != adapter.itemCount){
					if(itemCount < adapter.itemCount)
						doc_custom_str_rv.scrollToPosition(adapter.itemCount-1);
					itemCount = adapter.itemCount;
					doc_header_vm.updateStrSizeByHeaderId(parentId, it.size);
					count_tw_custom_str_rv.text = "${Constants.str_str_count} ${it.size}";
				}
			}
		})

		//add scanners to activity
		if(useCamera) {
			initCameraBarcodeScanner(inflater, savedInstanceState);
		}
		addTxtBarcodeScanner(inflater);
		setAdapterTouch(inflater);
}

	override fun onPause() {
		super.onPause();
		captureManager?.onPause();
	}
	override fun onResume() {
		super.onResume();
		captureManager?.onResume();
	}
	override fun onDestroy() {
		super.onDestroy();
		captureManager?.onDestroy();
	}

	private fun initCameraBarcodeScanner(inflater:LayoutInflater, savedInstanceState: Bundle?) {
		captureManager = addCameraBarcodeScanner(inflater)
		captureManager?.initializeFromIntent(intent, savedInstanceState);
		beepManager = BeepManager(this);
		beepManager.isVibrateEnabled = true;
	}
	private fun addTxtBarcodeScanner(inflater:LayoutInflater){
		val txt_scanner = inflater.inflate(R.layout.text_view_scanner, null);
		val editText = txt_scanner.findViewById<EditText>(R.id.txt_result_custom_str_scanner);
		editText.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
			if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
				strInput(inflater, editText.text.toString());
				editText.text.clear()
			}
			false
		});
		fragment_container_custom_rv_str.addView(txt_scanner);
	}

	private fun addCameraBarcodeScanner(inflater:LayoutInflater):CaptureManager{
		val camera_scanner = inflater.inflate(R.layout.custom_camera_scanner, null);
		val barcode_view = camera_scanner.findViewById<DecoratedBarcodeView>(R.id.barcode_view_custom_scan)
		val btn_scan = camera_scanner.findViewById<Button>(R.id.btnScan_custom_scan)

		fun animateBackground(){
			val colorFrom = resources.getColor(R.color.colorAccent);
			val colorTo = resources.getColor(R.color.colorPrimary);
			val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo);
			colorAnimation.duration = 250; // milliseconds
			colorAnimation.addUpdateListener { animator -> btn_scan.setBackgroundColor(
				animator.animatedValue as Int
			); }
			colorAnimation.start();
		}

		scanContinuousBG = btn_scan.background;
		btn_scan.setOnClickListener(View.OnClickListener {
			if (!scanContinuousState) {
				scanContinuousState = !scanContinuousState;
				btn_scan.setBackgroundColor(ContextCompat.getColor(InlineScanActivity@ this, R.color.colorPrimary));
				btn_scan.text = getString(R.string.scanning);
				barcode_view.decodeContinuous(object : BarcodeCallback {
					override fun barcodeResult(result: BarcodeResult?) {
						result?.let {
							val current = Date();
							val diff = current.time - lastScan.time;
							if(diff >= 2000){
								strInput(inflater, it.text)
								lastScan = current;
								if (beepManager.isVibrateEnabled)
									beepManager.playBeepSoundAndVibrate();
								else
									beepManager.playBeepSound();
								animateBackground();
							}
						}
					}
					override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {}
				});
			} else {
				btn_scan.text = getString(R.string.scan);
				scanContinuousState = !scanContinuousState;
				btn_scan.background = scanContinuousBG;
				barcode_view.barcodeView.stopDecoding();
			}
		});

		fragment_container_custom_rv_str.addView(camera_scanner);
		return CaptureManager(this@StringListActivity, barcode_view_custom_scan)
	}

	private fun enableActionMode(position: Int) {
		Timber.i("ENABLE_ACTION_MODE");
		if (actionMode == null)
			actionMode = startSupportActionMode(object : ActionMode.Callback {
				override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
					if (item?.itemId == R.id.action_str_delete) {
						adapter.deleteStrings();
						Timber.i("DATA WAS DELETED");
						Toast.makeText(this@StringListActivity, "Data was deleted", Toast.LENGTH_LONG).show();
						mode?.finish()
						return true
					}
					return false
				}

				override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
					mode?.menuInflater?.inflate(R.menu.rv_strings_menu, menu)
					return true
				}

				override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
					return false
				}

				override fun onDestroyActionMode(mode: ActionMode?) {
					adapter.selectedItems.clear()
					adapter.docStringsList
						.filter { it.selected }
						.forEach { it.selected = false }

					adapter.notifyDataSetChanged()
					actionMode = null
				}
			})

		adapter.toggleSelection(position)
		val size = adapter.selectedItems.size()
		if (size == 0) {
			actionMode?.finish()
		} else {
			actionMode?.title = "$size"
			actionMode?.invalidate()
		}
	}
	private fun strInput(inflater:LayoutInflater, newStr:String) {
		if(parentId.toInt() != 0) {
			var to_open_dialog: Boolean = true
			if (KtApplication.settings.contains(Constants.PREF_KEY_OPEN_NEW_STR_DIALOG)) {
				to_open_dialog = KtApplication.settings.getBoolean(Constants.PREF_KEY_OPEN_NEW_STR_DIALOG, true)
			}

			if (to_open_dialog) {
				newStrInputDialog(inflater, newStr)
			} else {
				val new_string = newStr.trim();
				if (new_string.isNotEmpty()) {
					(activityViewModel as DocStringViewModel).insert(DocString(new_string, parentId, 0, 1));
				}
			}
		}else{
			Toast.makeText(this@StringListActivity, "Parent_id error", Toast.LENGTH_SHORT).show();
		}
	}
	private fun updateStr(inflater:LayoutInflater, docString:DocString) {
		var to_open_dialog = KtApplication.settings.getBoolean(Constants.PREF_KEY_OPEN_NEW_STR_DIALOG, true)
		val updated_string = docString
		val dialog_view = inflater.inflate(R.layout.dialog_new_string, null)
		dialog_view.new_string_goods_count_et.text.append(updated_string.goodsCount.toString());
		dialog_view.new_string_tv.text = updated_string.docString;
		dialog_view.checkbox_output_dialog_new_string.isChecked = to_open_dialog;

		val builder = AlertDialog.Builder(this@StringListActivity).setView(dialog_view)
		val alert_dialog = builder.show();
		dialog_view.ok_button_new_string.setOnClickListener {
			updated_string.goodsCount = dialog_view.new_string_goods_count_et.text.toString().toLong();
			(activityViewModel as DocStringViewModel).updateString(updated_string);
			if(dialog_view.checkbox_output_dialog_new_string.isChecked != to_open_dialog) {
				to_open_dialog = dialog_view.checkbox_output_dialog_new_string.isChecked;
				val editor = KtApplication.settings.edit();
				editor.putBoolean(Constants.PREF_KEY_OPEN_NEW_STR_DIALOG, to_open_dialog);
				editor.apply();
			}
			alert_dialog.dismiss();
		}
	}
	private fun newStrInputDialog(inflater:LayoutInflater, newStr:String) {
		var to_open_dialog: Boolean = KtApplication.settings.getBoolean(Constants.PREF_KEY_OPEN_NEW_STR_DIALOG, true)
		val new_string = newStr.trim();
		val dialog_view = inflater.inflate(R.layout.dialog_new_string, null);
		dialog_view.new_string_goods_count_et.text.append("1");
		dialog_view.new_string_tv.text = new_string;
		dialog_view.checkbox_output_dialog_new_string.isChecked = to_open_dialog;

		val builder = AlertDialog.Builder(this@StringListActivity).setView(dialog_view)
		val alert_dialog = builder.show();

		fun completeScan() {
			alert_dialog.dismiss();
			val count = dialog_view.new_string_goods_count_et.text.toString().toLong();
			if(new_string.isNotEmpty())
				(activityViewModel as DocStringViewModel).insert(DocString(new_string, parentId, 0, count));
			if(dialog_view.checkbox_output_dialog_new_string.isChecked != to_open_dialog) {
				to_open_dialog = dialog_view.checkbox_output_dialog_new_string.isChecked;
				val editor = KtApplication.settings.edit();
				editor.putBoolean(Constants.PREF_KEY_OPEN_NEW_STR_DIALOG, to_open_dialog);
				editor.apply();
			}
		}

		dialog_view.new_string_goods_count_et.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
			if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
				val view:View = alert_dialog.currentFocus!!;
				iMM.hideSoftInputFromWindow(view.windowToken, 0)
				completeScan()
			}
			false
		});

		dialog_view.ok_button_new_string.setOnClickListener {
			completeScan()
		}


	}
	private fun setAdapterTouch(inflater:LayoutInflater) {
		adapter.onItemClick = {
			enableActionMode(it)
		}
		adapter.onItemLongClick = {
			enableActionMode(it)
		}
		adapter.onEditItemClick = {
			updateStr(inflater, it);
		}
	}
}