package com.akirambow.blebeacondetector;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;

public class AppControlMessageHandler extends Handler {

	private final WeakReference<BleBeaconDetectorMainActivity> mActivityRef;

	public static final int MSG_START_SEQUENCE		= 100;
	public static final int MSG_START_SEQUENCE_DONE	= 101;
	
	public static final int MSG_STOP_SEQUENCE		= 200;
	public static final int MSG_STOP_SEQUENCE_DONE	= 201;

	public static final int MSG_START_BLE_SCAN   	= 1000;
	public static final int MSG_START_BLE_SCAN_DONE	= 1001;
	public static final int MSG_STOP_BLE_SCAN    	= 2000;
	public static final int MSG_STOP_BLE_SCAN_DONE  = 2001;
	public static final int MSG_BLE_DEVICE_FOUND	= 3000;
	
	public static final int MSG_START_BLE_SCAN_SUCCESS = 30000;
	public static final int MSG_START_BLE_SCAN_FAIL    = 30500;

	public static final int MSG_STOP_BLE_SCAN_SUCCESS = 31000;
	
	public static final int _SUCCESS  = 0;
	public static final int _FAILURE  = -1;

	public AppControlMessageHandler(BleBeaconDetectorMainActivity aActivity)
	{
		mActivityRef = new WeakReference<BleBeaconDetectorMainActivity>(aActivity);
	}

	public void handleMessage(Message aMsg)
	{
		BleBeaconDetectorMainActivity activity = mActivityRef.get();
		if( activity == null ) return;
		
		switch(aMsg.what)
		{
			case MSG_START_SEQUENCE:
				activity.startBleScanSequence();
				break;

			case MSG_START_SEQUENCE_DONE:
				activity.processStateStartingToScanning();
				break;

			case MSG_STOP_SEQUENCE:
				activity.stopBleScanSequence();
				break;

			case MSG_STOP_SEQUENCE_DONE:
				activity.processStateStoppingToIdle();
				break;

			case MSG_START_BLE_SCAN:
				activity.startBluetoothLeScan();
				break;

			case MSG_START_BLE_SCAN_DONE:
				activity.setScanStatusTextAfterStart( aMsg.arg1 == _SUCCESS );
				break;

			case MSG_STOP_BLE_SCAN:
				activity.stopBluetoothLeScan();
				break;

			case MSG_STOP_BLE_SCAN_DONE:
				activity.setScanStatusTextAfterStop( aMsg.arg1 == _SUCCESS );
				break;

			case MSG_BLE_DEVICE_FOUND:
				activity.notifyBleScanDataArrived((BleAdvertiseDataStructure)aMsg.obj);
				break;
				
			default:
				
		}
	}
}
