package com.akirambow.blescanlibs;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;
import java.util.ArrayList;


public class BleScanController {

	private BluetoothAdapter mBluetoothAdapter;
	private long mStartTime;

	private volatile boolean mScanRunning;

	private ArrayList<String> mFilteringAddressList;
	private boolean mUseFilter;
	
	private boolean mUseIbeaconFilter;

	private BleScanCallbackListener	mListener;
	private final BluetoothAdapter.LeScanCallback mLeScanCallback = 
			new BluetoothAdapter.LeScanCallback()
			{
				@Override
				public void onLeScan(BluetoothDevice aDevice, int aRssi, byte[] aScanRecord)
				{
					onLeScanLocal(aDevice, aRssi, aScanRecord);
				}		
			};

	private static final long STARTTIME_NONE = -1;
	private static final String DEVICE_NAME_NONE = "NO_NAME";
	private static final String LOG_TAG = "BleScanController";

	public BleScanController(Context aContext)
	{
		if( aContext != null )
		{
			mBluetoothAdapter = ((BluetoothManager)aContext.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();			
		}
		else
		{
			mBluetoothAdapter = null;
			Log.w(LOG_TAG, "Fail to get BluetoothAdapter beacuse of Invalid Context.");
		}
		mScanRunning = false;
		mFilteringAddressList = null;
		mUseFilter = false;
		mUseIbeaconFilter = false;
		mStartTime = STARTTIME_NONE;
	}
	
	public void resetStartTime()
	{
		mStartTime = STARTTIME_NONE;
	}
			
	public void startBleDeviceScan()
	{
		Log.d(LOG_TAG, "startBleDeviceScan");
		if( mBluetoothAdapter == null )
		{
			Log.w(LOG_TAG, "startBleDeviceScan : BluetoothAdapter is not found.");
			return;
		}
		
		if( !mBluetoothAdapter.isEnabled() )
		{
			Log.w(LOG_TAG, "startBleDeviceScan : BluetoothAdapter is not enabled.");
			return;
		}
		
		if( mStartTime < 0 ) mStartTime = System.currentTimeMillis();
		boolean ret = mBluetoothAdapter.startLeScan(mLeScanCallback);
		if( ret )
		{
			mScanRunning = true;
		}
		else
		{
			Log.w(LOG_TAG, "startBleDeviceScan : Fail to do BluetoothAdapter.startLeScan.");
			mScanRunning = false;
		}
	}
			
	public void stopBleDeviceScan()
	{
		Log.d(LOG_TAG, "stopBleDeviceScan");
		if( mBluetoothAdapter == null )
		{
			Log.w(LOG_TAG, "stopBleDeviceScan : Bluetooth Adapter is not found.");
			return;
		}
		
		mScanRunning = false;
		if( !mBluetoothAdapter.isEnabled() )
		{
			Log.w(LOG_TAG, "startBleDeviceScan : BluetoothAdapter is not enabled.");
			return;
		}
		mBluetoothAdapter.stopLeScan(mLeScanCallback);
	}
			
	public boolean isScanRunning()
	{
		return mScanRunning;
	}
	
	public void terminate()
	{
		if( mScanRunning ) stopBleDeviceScan();
		mScanRunning = false;
		mBluetoothAdapter = null;
		mListener = null;
	}
	
	public void setFilteringAddressList(ArrayList<String> aAddressList)
	{
		mFilteringAddressList = aAddressList;
	}
	
	public void setUseFilter(boolean aFlag)
	{
		mUseFilter = aFlag;
	}
	
	public void setUseIbeaconFilter(boolean aFlag)
	{
		mUseIbeaconFilter = aFlag;
	}
	
	public interface BleScanCallbackListener
	{
		public void onBleScan(BleScanRecordData aScanData);
	}
	
	public void setBleScanCallbackListener(BleScanCallbackListener aListener)
	{
		mListener = aListener;
	}
	
	private void onLeScanLocal(BluetoothDevice aDevice, int aRssi, byte[] aScanRecord)
	{
		if( mListener != null )
		{
			String deviceName = (aDevice.getName() != null ) ? aDevice.getName() : DEVICE_NAME_NONE;
			if( deviceName.equals(" ") ) deviceName = DEVICE_NAME_NONE;
			
			long timestamp = System.currentTimeMillis();
			if( mUseFilter && mFilteringAddressList != null)
			{
				int listSize = mFilteringAddressList.size();
				if( listSize > 0 )
				{
					for( int i=0; i<listSize; i++)
					{
						String currentStr = mFilteringAddressList.get(i);
						if( currentStr == null ) continue;
						if( currentStr.equalsIgnoreCase(aDevice.getAddress()) )
						{
							if( mUseIbeaconFilter )
							{
								if( AppleBeaconRecordData.isAppleBeaconRecordData(aScanRecord) )
								{
									AppleBeaconRecordData iBeaconData = new AppleBeaconRecordData(deviceName, aDevice.getAddress(), aRssi, aScanRecord, timestamp);
									mListener.onBleScan(iBeaconData);
								}
							}
							else
							{
								BleScanRecordData scanData = new BleScanRecordData(deviceName, aDevice.getAddress(), aRssi, aScanRecord, timestamp);								
								mListener.onBleScan(scanData);
							}
							break;
						}
					}
				}
			}
			else
			{
				if( mUseIbeaconFilter )
				{
					if( AppleBeaconRecordData.isAppleBeaconRecordData(aScanRecord) )
					{
						AppleBeaconRecordData iBeaconData = new AppleBeaconRecordData(deviceName, aDevice.getAddress(), aRssi, aScanRecord, timestamp);
						mListener.onBleScan(iBeaconData);
					}
				}
				else
				{
					BleScanRecordData scanData = new BleScanRecordData(deviceName, aDevice.getAddress(), aRssi, aScanRecord, timestamp);								
					mListener.onBleScan(scanData);
				}
			}
		}
	}

}
