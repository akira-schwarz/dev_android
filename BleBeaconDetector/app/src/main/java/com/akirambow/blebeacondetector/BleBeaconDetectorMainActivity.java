package com.akirambow.blebeacondetector;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

//import com.akirambow.blebeacondetector.R;

//import android.view.Menu;
//import android.view.MenuItem;

public class BleBeaconDetectorMainActivity extends Activity
{
	private Button   mStartStopButton;
	private TextView mStatusTextView;
	private TextView mResultTextView;
	private TextView mScanCountTextView;
	private CheckBox mRecordTargetCheckBox;
	private CheckBox mRecordAllCheckBox;
	private Spinner  mTargetDeviceSpinner;
	private EditText mScanIntervalEditText;
	private EditText mWaitIntervalEditText;
	private EditText mRecordNoteEditText;
	
	private String  mDeviceSpinnerSelectedAddr;
	
	private volatile AppControlMessageHandler mHandler;
	private volatile Thread mBleScanLooper;
	private int mAppState;
	
	private BleScanTicker 		mBleScanTicker;
	private BleScanController	mBleScanController;
	private long 				mScanCount;
	
	private HandlerThread				mRecordThread;
	private BleBeaconDataRecorder		mRecorder;
	private BleBeaconDataRecordHandler	mRecordHandler;
	private boolean 					mRecordAll;
	private volatile boolean			mRecordEnabled;

	private static final int STATE_IDLE		= 100;
	private static final int STATE_STARTING	= 200;
	private static final int STATE_SCANNING = 300;
	private static final int STATE_STOPPING	= 400;
	
	
	private static final String LOG_TAG = "BleBeaconDetector";
			

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ibeacon_detector_main);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Log.d(LOG_TAG, "onCreate");

		getUiParts();
		initParams();
		initController();
		registOnClickListenerToButton();
		registOnSelectedListenerToSpinner();
		registOnClickListenerToCheckBox();
		
		initRecordController();
		
		Log.d(LOG_TAG, "storage path root : " + Environment.getExternalStorageDirectory().getPath());
	}
	
	@Override
	protected void onResume()
	{
		Log.d(LOG_TAG, "onResume");
		super.onResume();
		startRecordThread();
	}
	
	@Override
	protected void onPause()
	{
		Log.d(LOG_TAG, "onPause");
		if( mAppState == STATE_SCANNING )
		{
			Message msg = Message.obtain(mHandler);
			msg.what = AppControlMessageHandler.MSG_STOP_SEQUENCE;
			mHandler.sendMessage(msg);
			mAppState = STATE_STOPPING;
		}
		quitRecordThread();
		super.onPause();
	}
	
	private void getUiParts()
	{
		mStatusTextView			= (TextView)findViewById(R.id.StatusTextView);
		mResultTextView			= (TextView)findViewById(R.id.ResultTextView);
		mScanCountTextView		= (TextView)findViewById(R.id.ScanCountTextView);
		mStartStopButton 		= (Button)findViewById(R.id.bt_startstop);
		mRecordAllCheckBox 		= (CheckBox)findViewById(R.id.cb_record_all);
		mRecordTargetCheckBox 	= (CheckBox)findViewById(R.id.cb_record_target_only);
		mTargetDeviceSpinner 	= (Spinner)findViewById(R.id.TargetDeviceSpinner);
		mScanIntervalEditText	= (EditText)findViewById(R.id.ScanIntervalEditText);
		mWaitIntervalEditText	= (EditText)findViewById(R.id.WaitIntervalEditText);
		mRecordNoteEditText     = (EditText)findViewById(R.id.RecordNoteEditText);
	}
	
	private void initParams()
	{
		mDeviceSpinnerSelectedAddr = null;
		mScanCount = 0;
		mAppState = STATE_IDLE;
	}
	
	private void initController()
	{
		mHandler = new AppControlMessageHandler(this);
		mBleScanTicker = new BleScanTicker(this);
		mBleScanController = new BleScanController(getApplicationContext(), mHandler);
	}
	
	private void initRecordController()
	{
		mRecorder = new BleBeaconDataRecorder();
	}
	
	private void startRecordThread()
	{
		mRecordThread  = new HandlerThread("BackgroundThread", android.os.Process.THREAD_PRIORITY_DEFAULT);
		mRecordThread.start();		
		mRecordHandler = new BleBeaconDataRecordHandler(mRecordThread.getLooper(), this);
	}
	
	private void quitRecordThread()
	{		
		if( mRecordHandler.isActive() )
		{
			Message msg = Message.obtain(mRecordHandler);
			msg.what = BleBeaconDataRecordHandler.MSG_RECORD_FILE_CLOSE;
			mRecordHandler.sendMessage(msg);
		}
		mRecordThread.quitSafely();
		mRecordHandler = null;
		mRecordThread  = null;
	}
	
	private void registOnClickListenerToButton()
	{
		mStartStopButton.setOnClickListener(
				new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						
						if( v==mStartStopButton )
						{
							if( mAppState == STATE_IDLE )
							{
								sendMessageToAppControlHandler(AppControlMessageHandler.MSG_START_SEQUENCE, null, 0);
								if( mRecordTargetCheckBox.isChecked() || mRecordAllCheckBox.isChecked())
								{
									Editable recordNote = mRecordNoteEditText.getText();
									String recordNoteStr = "\tScan duration:\t" + mScanIntervalEditText.getText().toString();
									recordNoteStr += "\tScan Interval:\t" + mWaitIntervalEditText.getText().toString();
									if( recordNote != null )
									{
										recordNoteStr += "\tMemo:\t" + recordNote.toString();
									}
									mRecorder.setRecordNoteString(recordNoteStr);
									mRecordHandler.setRecorder(mRecorder);
									mRecordAll = mRecordAllCheckBox.isChecked();
									mRecordEnabled = false;
									sendMessageToRecorderHandler(BleBeaconDataRecordHandler.MSG_RECORD_FILE_OPEN, null, 0);									
								}
								mAppState = STATE_STARTING;
							}
							else if( mAppState == STATE_SCANNING )
							{
								sendMessageToAppControlHandler(AppControlMessageHandler.MSG_STOP_SEQUENCE, null, 0);
								if( mRecordEnabled )
									sendMessageToRecorderHandler(BleBeaconDataRecordHandler.MSG_RECORD_FILE_CLOSE, null, 0);	
								mAppState = STATE_STOPPING;
							}
						}
					}
				}
				);
	}
		
	private void sendMessageToAppControlHandler(int aWhat, Object aObj, int aArg)
	{
		Message msg = Message.obtain(mHandler);
		msg.what = aWhat;
		msg.obj  = aObj;
		msg.arg1 = aArg;
		mHandler.sendMessage(msg);
	}
	
	private void sendMessageToRecorderHandler(int aWhat, Object aObj, int aArg)
	{
		Message msg = Message.obtain(mRecordHandler);
		msg.what = aWhat;
		msg.obj  = aObj;
		msg.arg1 = aArg;
		mRecordHandler.sendMessage(msg);
	}
	
	private void registOnSelectedListenerToSpinner()
	{
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
				(
					getApplicationContext(), 
					R.layout.spinner_item_style,
					getResources().getStringArray(R.array.strarray_target_device_selection)
				);
		arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item_style);
		
		mTargetDeviceSpinner.setAdapter(arrayAdapter);
		mTargetDeviceSpinner.setOnItemSelectedListener(
				new AdapterView.OnItemSelectedListener()
				{
					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
					{
						Spinner spinner     = (Spinner)parent;
						String  selectedStr = (String)spinner.getSelectedItem();						
						mDeviceSpinnerSelectedAddr = selectedStr.substring(selectedStr.indexOf('(')+1, selectedStr.indexOf(')'));
					}
			
					@Override
					public void onNothingSelected(AdapterView<?> arg0)
					{
				
					}
				});
	}
	
	private void registOnClickListenerToCheckBox()
	{
		mRecordTargetCheckBox.setOnClickListener(mCheckBoxOnClickListener);
		mRecordAllCheckBox.setOnClickListener(mCheckBoxOnClickListener);
	}
	
	
	private void setEnabledToAllInputWidgets(boolean aFlag)
	{
		mTargetDeviceSpinner.setEnabled(aFlag);
		mScanIntervalEditText.setEnabled(aFlag);
		mWaitIntervalEditText.setEnabled(aFlag);
		mRecordTargetCheckBox.setEnabled(aFlag);
		mRecordAllCheckBox.setEnabled(aFlag);
		mRecordNoteEditText.setEnabled(aFlag);
	}

	public Thread getBleScanLooper()
	{
		return mBleScanLooper;
	}
	
	public AppControlMessageHandler getAppControlMessageHandler()
	{
		return mHandler;
	}
	
	public void startBleScanSequence()
	{
		Log.d(LOG_TAG, "startBleScanSequence : target=" + mDeviceSpinnerSelectedAddr);
		long intervalTime = Long.valueOf(mScanIntervalEditText.getText().toString());
		mBleScanController.resetStartTime();
		mBleScanTicker.setIntervalMillis(intervalTime, BleScanTicker.SCANNING_INTERVAL);
		intervalTime = Long.valueOf(mWaitIntervalEditText.getText().toString());
		mBleScanTicker.setIntervalMillis(intervalTime, BleScanTicker.WAITING_INTERVAL);
		mBleScanLooper = new Thread(mBleScanTicker);
		mBleScanLooper.start();
		mScanCount = 0;
		mAppState = STATE_STARTING;
	}
	
	public void processStateStartingToScanning()
	{
		Log.d(LOG_TAG, "processStateStartingToScanning");
		if( mAppState == STATE_STARTING )
		{
			mAppState = STATE_SCANNING;
			mStartStopButton.setText(getResources().getText(R.string.str_bt_stop));
			setEnabledToAllInputWidgets(false);
		}
	}

	public void stopBleScanSequence()
	{
		Log.d(LOG_TAG, "stopBleScanSequence");
		mBleScanLooper = null;
		mAppState = STATE_STOPPING;
	}

	public void processStateStoppingToIdle()
	{
		Log.d(LOG_TAG, "processStateStoppingToIdle");
		if( mAppState == STATE_STOPPING )
		{
			mStartStopButton.setText(getResources().getText(R.string.str_bt_start));
			setEnabledToAllInputWidgets(true);
			mAppState = STATE_IDLE;
		}
	}

	public void startBluetoothLeScan()
	{
//		Log.d(LOG_TAG, "startBluetoothLeScan");
		mBleScanController.startBleDeviceScan();
	}
	
	public void stopBluetoothLeScan()
	{
//		Log.d(LOG_TAG, "stopBluetoothLeScan");
		mBleScanController.stopBleDeviceScan();
	}
	
	public void setScanStatusTextAfterStart(boolean aSuccess)
	{
		mStatusTextView.setText(aSuccess ? 
									getResources().getText(R.string.str_text_scanning)
									: getResources().getText(R.string.str_text_scan_error));
		if( aSuccess )
		{
			mScanCount++;
			mScanCountTextView.setText(getResources().getText(R.string.str_text_scan_count).toString() + mScanCount);
		}
	}

	public void setScanStatusTextAfterStop(boolean aSuccess)
	{
		mStatusTextView.setText(aSuccess ? 
									getResources().getText(R.string.str_text_waiting)
									: getResources().getText(R.string.str_text_scan_error));		
	}
	
	private void setResultTextWithDeviceData(BleAdvertiseDataStructure aData)
	{
		if( aData.compareAddress(mDeviceSpinnerSelectedAddr) )
		{
			String resultStr = "Name:\t" + aData.getDeviceName() + "\n";
			resultStr += "Addr:\t" + aData.getDeviceAddress() + "\n";
			resultStr += "RSSI:\t" + aData.getRssi() + "\n";
			resultStr += "Time:\t" + aData.getScannedTime();
			mResultTextView.setText(resultStr);				
		}
	}
	
	public void notifyBleScanDataArrived(BleAdvertiseDataStructure aData)
	{
		if ( aData != null )
		{
			setResultTextWithDeviceData(aData);
			if( mRecordEnabled )
			{
				if( mRecordAll || aData.compareAddress(mDeviceSpinnerSelectedAddr))
				{
					Log.d(LOG_TAG, "notifyBleScanDataArrived: send Message");
					sendMessageToRecorderHandler(BleBeaconDataRecordHandler.MSG_WRITE_ONE_RECORD_AND_LINE, aData, 0);
				}
			}
		}
		else
		{
			mResultTextView.setText(getResources().getText(R.string.str_text_no_result));
		}

	}
	
	public void notifyFileOpenResult(int aFlag)
	{
		if (aFlag == BleBeaconDataRecordHandler._SUCCESS)
		{
			mRecordEnabled = true;
		}
		else
		{
			mRecordEnabled = false;
		}
	}

	
	private final View.OnClickListener mCheckBoxOnClickListener =
			new View.OnClickListener()
			{
				@Override
				public void onClick(View v) {
					
					if( v==mRecordTargetCheckBox )
					{
						if( mRecordAllCheckBox.isChecked() )
						{
							mRecordAllCheckBox.setChecked(false);
						}
					}
					
					if( v==mRecordAllCheckBox )
					{
						if( mRecordTargetCheckBox.isChecked() )
						{
							mRecordTargetCheckBox.setChecked(false);
						}
					}
				}
			};
		
}
