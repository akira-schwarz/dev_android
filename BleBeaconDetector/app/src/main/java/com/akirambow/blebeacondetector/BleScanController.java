package com.akirambow.blebeacondetector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Message;
import android.util.Log;

public class BleScanController {

	private BluetoothAdapter 			mBluetoothAdapter;
	private AppControlMessageHandler	mHandler;

	private long mStartTime;

	private volatile boolean mScanRunning;

	private final BluetoothAdapter.LeScanCallback mLeScanCallback = 
			new BluetoothAdapter.LeScanCallback()
			{
				@Override
				public void onLeScan(BluetoothDevice aDevice, int aRssi, byte[] aScanRecord)
				{
					long timeStamp = System.currentTimeMillis() - mStartTime;
					
//					String devName = aDevice.getName();
//					if( devName != null )
//						if( devName.equalsIgnoreCase("estimote") )
//							Log.d(LOG_TAG, "onLeScan : t=" + timeStamp + " name=" + devName);
					Log.d(LOG_TAG, "onLeScan : t=" + timeStamp + " Addr=" + aDevice.getAddress() + " RSSI=" + aRssi);

					BleAdvertiseDataStructure bleData = new BleAdvertiseDataStructure(aDevice.getName(), aDevice.getAddress(), aRssi, null);
					bleData.setScannedTime(timeStamp) ;
					Message msg = mHandler.obtainMessage();
					msg.what = AppControlMessageHandler.MSG_BLE_DEVICE_FOUND;
					msg.obj  = bleData;
					mHandler.sendMessage(msg);
				}		
			};

	private static final String LOG_TAG = "BleScanController";

	public BleScanController(Context aContext, AppControlMessageHandler aHandler)
	{
		mHandler = aHandler;
		mBluetoothAdapter = ((BluetoothManager)aContext.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
		mScanRunning = false;
		mStartTime = -1;
	}
	
	public void resetStartTime()
	{
		mStartTime = -1;
	}
			
	public void startBleDeviceScan()
	{
		if( mBluetoothAdapter == null )
		{
			Log.w(LOG_TAG, "startBleDeviceScan : Bluetooth Adapter is not found.");
			return;
		}
		if( !mBluetoothAdapter.isEnabled() ) mBluetoothAdapter.enable();
		if( mStartTime < 0 ) mStartTime = System.currentTimeMillis();
		boolean ret = mBluetoothAdapter.startLeScan(mLeScanCallback);
		Log.d(LOG_TAG, "startLeScan : t=" + (System.currentTimeMillis() - mStartTime) );
		Message msg = mHandler.obtainMessage();
		msg.obj = null;
		msg.what = AppControlMessageHandler.MSG_START_BLE_SCAN_DONE;
		if( ret )
		{
			mScanRunning = true;
			msg.arg1 = AppControlMessageHandler._SUCCESS;
		}
		else
		{
			Log.w(LOG_TAG, "startBleDeviceScan : fail to do startLeScan.");
			mScanRunning = false;
			msg.arg1 = AppControlMessageHandler._FAILURE;
		}
		mHandler.sendMessage(msg);
	}
			
	public void stopBleDeviceScan()
	{
		if( mBluetoothAdapter == null )
		{
			Log.w(LOG_TAG, "stopBleDeviceScan : Bluetooth Adapter is not found.");
			return;
		}
		mBluetoothAdapter.stopLeScan(mLeScanCallback);
		Log.d(LOG_TAG, "stopLeScan  : t=" + (System.currentTimeMillis() - mStartTime) );
		mScanRunning = false;
		
		Message msg = mHandler.obtainMessage();
		msg.what = AppControlMessageHandler.MSG_STOP_BLE_SCAN_SUCCESS;
		msg.obj = null;
		mHandler.sendMessage(msg);
	}
			
	public boolean isScanRunning()
	{
		return mScanRunning;
	}
		
}
