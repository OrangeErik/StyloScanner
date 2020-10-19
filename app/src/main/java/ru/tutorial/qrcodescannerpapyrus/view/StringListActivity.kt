package ru.tutorial.qrcodescannerpapyrus.view
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Size
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.zxing.client.android.BeepManager
import kotlinx.android.synthetic.main.activity_barcode_scanning.*
import kotlinx.android.synthetic.main.dialog_new_string.*
import kotlinx.android.synthetic.main.rv_strings.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.tutorial.qrcodescannerpapyrus.KtApplication
import ru.tutorial.qrcodescannerpapyrus.R
import ru.tutorial.qrcodescannerpapyrus.adapters.DocStringListAdapter
import ru.tutorial.qrcodescannerpapyrus.data.DocString
import ru.tutorial.qrcodescannerpapyrus.data.GoodsEntity
import ru.tutorial.qrcodescannerpapyrus.data.MLKitBarcodeAnalyzer
import ru.tutorial.qrcodescannerpapyrus.data.ScanningResultListener
import ru.tutorial.qrcodescannerpapyrus.util.Constants
import ru.tutorial.qrcodescannerpapyrus.util.bindViewModel
import ru.tutorial.qrcodescannerpapyrus.viewmodels.DocHeaderViewModel
import ru.tutorial.qrcodescannerpapyrus.viewmodels.DocStringViewModel
import ru.tutorial.qrcodescannerpapyrus.viewmodels.DocStringViewModelFactory
import timber.log.Timber
import java.util.*
import java.util.concurrent.Executors

class StringListActivity: AppCompatActivity() {
	private var parentId:Long = 0;
	lateinit var adapter:DocStringListAdapter
	private var actionMode: ActionMode? = null
	private lateinit var activityViewModel: ViewModel;
	lateinit var beepManager: BeepManager;
	lateinit var inflater:LayoutInflater;
	private var scanContinuousState: Boolean = false;
	private var lastScan = Date();
	var useCamera:Boolean = false;
	var itemCount:Int = 0;
	lateinit var iMM:InputMethodManager;

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState);
		//init variables
		setContentView(R.layout.rv_strings);
		inflater = LayoutInflater.from(this@StringListActivity);
		useCamera = KtApplication.settings.getBoolean(Constants.PREF_KEY_USE_CAMERA, false);
		parentId = intent.getLongExtra("PARENT_ID", 0);
		activityViewModel = bindViewModel(this, DocStringViewModelFactory(parentId), DocStringViewModel::class.java);
		adapter = DocStringListAdapter(this, activityViewModel as DocStringViewModel);
		iMM = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager;

		//rv_setting
		doc_custom_str_rv.adapter = adapter;
		doc_custom_str_rv.layoutManager = LinearLayoutManager(this);
		val doc_header_vm = bindViewModel(this@StringListActivity, null, DocHeaderViewModel::class.java);
		(activityViewModel as DocStringViewModel).allStrings.observe(this@StringListActivity, Observer { strings ->
			strings.let {
				adapter.setDocStrings(it);
				if(itemCount != adapter.itemCount){
					if(itemCount < adapter.itemCount)
						doc_custom_str_rv.scrollToPosition(adapter.itemCount-1);
					itemCount = adapter.itemCount;
					(doc_header_vm as DocHeaderViewModel).updateStrSizeByHeaderId(parentId, it.size);
					strcount_tw.text = "${Constants.str_str_count} ${it.size}";
				}
				goodscount_tw.text = "${Constants.str_goods_count} ${it.map{ str -> str.goodsCount}.sum()}"
			}
		})

		//add scanners to activity
		if(useCamera) {
			addCameraBarcodeScanner()
		}
		addTxtBarcodeScanner();
		setAdapterTouch();
	}

	private fun addTxtBarcodeScanner(){
		val txt_scanner = inflater.inflate(R.layout.text_view_scanner, null);
		val editText = txt_scanner.findViewById<EditText>(R.id.txt_result_custom_str_scanner);
		editText.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
			if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
				strInput(editText.text.toString());
				editText.text.clear()
			}
			false
		});
		fragment_container_custom_rv_str.addView(txt_scanner);
	}

	private fun addCameraBarcodeScanner(){
		beepManager = BeepManager(this);
		beepManager.isVibrateEnabled = true;
		val camera_scanner = inflater.inflate(R.layout.activity_barcode_scanning, null);
		val btn_scan = camera_scanner.findViewById<Button>(R.id.btnScan_custom_scan);
		fragment_container_custom_rv_str.addView(camera_scanner);
		val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
		val cameraProvider = cameraProviderFuture.get()

		//___CAMERA_SELECTOR
		val cameraSelector: CameraSelector = CameraSelector.Builder()
			.requireLensFacing(CameraSelector.LENS_FACING_BACK)
			.build()

		//___PREVIEW
		val preview: Preview = Preview.Builder().build()
		preview.setSurfaceProvider(cameraPreview.surfaceProvider)

		//___Analysis
		val imageAnalysis = createAnalysis();

		btn_scan.setOnClickListener {
			if (!scanContinuousState) {
				scanContinuousState = !scanContinuousState;
				cameraProviderFuture.addListener(Runnable {
					//___BIND
					cameraProvider?.bindToLifecycle(this, cameraSelector, imageAnalysis, preview)
				}, ContextCompat.getMainExecutor(this))

			} else {
				cameraProvider?.unbindAll()
				scanContinuousState = !scanContinuousState;
			}
		}
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

	private fun strInput(newStr:String) {
		if(parentId.toInt() != 0) {
			var to_open_dialog: Boolean = true
			if (KtApplication.settings.contains(Constants.PREF_KEY_OPEN_NEW_STR_DIALOG)) {
				to_open_dialog = KtApplication.settings.getBoolean(Constants.PREF_KEY_OPEN_NEW_STR_DIALOG, true)
			}

			if (to_open_dialog) {
				strInputDialog(newStr)
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

	private fun updateStr(docString:DocString) {
		var to_open_dialog = KtApplication.settings.getBoolean(Constants.PREF_KEY_OPEN_NEW_STR_DIALOG, true)
		val updated_string = docString
		val dialog_view = Dialog(this@StringListActivity, R.style.DialogTheme);
		dialog_view.setContentView(R.layout.dialog_new_string);
		dialog_view.new_string_goods_count_et.text.append(updated_string.goodsCount.toString());
		dialog_view.new_string_tv.text = updated_string.docString;
		dialog_view.checkbox_output_dialog_new_string.isChecked = to_open_dialog;
		dialog_view.ok_button_new_string.setOnClickListener {
			updated_string.goodsCount = dialog_view.new_string_goods_count_et.text.toString().toLong();
			(activityViewModel as DocStringViewModel).updateString(updated_string);
			if(dialog_view.checkbox_output_dialog_new_string.isChecked != to_open_dialog) {
				to_open_dialog = dialog_view.checkbox_output_dialog_new_string.isChecked;
				val editor = KtApplication.settings.edit();
				editor.putBoolean(Constants.PREF_KEY_OPEN_NEW_STR_DIALOG, to_open_dialog);
				editor.apply();
			}
			dialog_view.dismiss();
		}
		dialog_view.show();
	}

	private fun strInputDialog(newStr:String)  = runBlocking{
		var to_open_dialog: Boolean = KtApplication.settings.getBoolean(Constants.PREF_KEY_OPEN_NEW_STR_DIALOG, true)
		val new_string = newStr.trim();
		val dialog_view = Dialog(this@StringListActivity, R.style.DialogTheme);
		dialog_view.setContentView(R.layout.dialog_new_string);
		dialog_view.new_string_goods_count_et.text.append("1");
		dialog_view.new_string_tv.text = new_string;
		dialog_view.checkbox_output_dialog_new_string.isChecked = to_open_dialog;

		//__Goods data from DB__
		GlobalScope.launch(Dispatchers.Main) {
			val goods_name: String? = (activityViewModel as DocStringViewModel).takeGoods(newStr).await()
			if(goods_name!= null) {
				if(goods_name.isNotEmpty()) {
					dialog_view.new_string_name_tv.text = goods_name
				}
			}
		}

		fun completeScan() {
			dialog_view.dismiss();
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
				val view:View = dialog_view.currentFocus!!;
				iMM.hideSoftInputFromWindow(view.windowToken, 0)
				completeScan()
			}
			false
		});
		dialog_view.ok_button_new_string.setOnClickListener {
			completeScan()
		}
		dialog_view.show();
	}

	private fun setAdapterTouch() {
		adapter.apply {
			onItemClick = { enableActionMode(it) }
			onItemLongClick = { enableActionMode(it) }
			onEditItemClick = { updateStr(it); }
		}
	}

	fun createAnalysis():ImageAnalysis {
		var cameraExecutor = Executors.newSingleThreadExecutor()
		val imageAnalysis = ImageAnalysis.Builder()
			.setTargetResolution(Size(1280, 720))
			.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
			.build()

		val orientationEventListener = object : OrientationEventListener(this as Context) {
			override fun onOrientationChanged(orientation: Int) {
				// Monitors orientation values to determine the target rotation value
				val rotation: Int = when (orientation) {
					in 45..134 -> Surface.ROTATION_270
					in 135..224 -> Surface.ROTATION_180
					in 225..314 -> Surface.ROTATION_90
					else -> Surface.ROTATION_0
				}
				imageAnalysis.targetRotation = rotation
			}
		}
		orientationEventListener.enable()

		val listener= object: ScanningResultListener {
			override fun onScanned(result: String) {
				result.let {
					val current = Date();
					val diff = current.time - lastScan.time;
					if(diff >= 2000){
						strInput(it)
						lastScan = current;
						if (beepManager.isVibrateEnabled)
							beepManager.playBeepSoundAndVibrate();
						else
							beepManager.playBeepSound();
					}
				}
			}
		}

		var analyzer: ImageAnalysis.Analyzer = MLKitBarcodeAnalyzer(listener);
		imageAnalysis.setAnalyzer(cameraExecutor, analyzer)
		return imageAnalysis;
	}
}