package com.akirambow.blebeacondetector;

import java.lang.ref.WeakReference;
import android.os.Message;
import android.util.Log;

public class BleScanTicker implements Runnable
{
	private long mScanningIntervalMillis;
	private long mWaitingIntervalMillis;
	
	private int mRunningState;
	
	private static final int  STATE_INITED   = 10000;
	private static final int  STATE_WAITING  = 10100;
	private static final int  STATE_SCANNING = 10200;
	
	private static final long MINIMUM_INTERVAL_MSEC = 50;
	private static final long MAXIMUM_INTERVAL_MSEC = 5000;
	private static final long DEFAULT_INTERVAL_MSEC = 500;
	
	public static final int SCANNING_INTERVAL = 100;
	public static final int WAITING_INTERVAL  = 101;	

	private final WeakReference<BleBeaconDetectorMainActivity> activityRef;

	private static final String LOG_TAG = "ScanningTicker";
	
	public BleScanTicker(BleBeaconDetectorMainActivity aActivity)
	{
		activityRef = new WeakReference<BleBeaconDetectorMainActivity>(aActivity);
		mScanningIntervalMillis = DEFAULT_INTERVAL_MSEC;
		mWaitingIntervalMillis  = DEFAULT_INTERVAL_MSEC;
		mRunningState = STATE_INITED;
	}

	@Override
	public void run() {
		BleBeaconDetectorMainActivity activity;
		AppControlMessageHandler handler;
		Message msg;
		long sleepTime;
		
		activity = activityRef.get();
		if( activity == null )
		{
			Log.d(LOG_TAG, "Fatal Error! : Cannot get the base activity.");
			return;
		}

		sleepTime = mScanningIntervalMillis;
		
		handler = activity.getAppControlMessageHandler();
		msg = Message.obtain(handler);
		msg.what = AppControlMessageHandler.MSG_START_SEQUENCE_DONE;
		msg.arg1 = AppControlMessageHandler._SUCCESS;
		handler.sendMessage(msg);
		
		Log.d(LOG_TAG, "Thread start. scan:" + mScanningIntervalMillis + " ms, wait:" + mWaitingIntervalMillis);
		
		boolean isBreakable = false;
		
//		while( activity.getBleScanLooper() != null )
		while( true )
		{
			handler = activity.getAppControlMessageHandler();
			if( handler == null ) break;
			
			msg = Message.obtain(handler);
			
			switch(mRunningState)
			{
			case STATE_INITED:
			case STATE_WAITING:
				msg.what = AppControlMessageHandler.MSG_START_BLE_SCAN;
				mRunningState = STATE_SCANNING;
				sleepTime = mScanningIntervalMillis;
				isBreakable = false;
				break;
				
			case STATE_SCANNING:
				msg.what = AppControlMessageHandler.MSG_STOP_BLE_SCAN;
				mRunningState = STATE_WAITING;
				sleepTime = mWaitingIntervalMillis;
				isBreakable = true;
				break;
			}
			handler.sendMessage(msg);
			
			try
			{
				Thread.sleep(sleepTime);
			}
			catch(Exception e)
			{
				Log.w(LOG_TAG, "Thread interruption occured.");
			}
			
			if( activity.getBleScanLooper()==null && isBreakable ) break;
		}

		handler = activity.getAppControlMessageHandler();
		msg = Message.obtain(handler);
		msg.what = AppControlMessageHandler.MSG_STOP_SEQUENCE_DONE;
		msg.arg1 = AppControlMessageHandler._SUCCESS;
		handler.sendMessage(msg);

		mRunningState = STATE_INITED;
		Log.d(LOG_TAG, "Thread terminate.");
	}

	public void setIntervalMillis(long aIntervalMillis, int aIntervalType)
	{
		long currentIntervalMillis;
		
		if( aIntervalMillis < MINIMUM_INTERVAL_MSEC )
		{
			currentIntervalMillis  = MINIMUM_INTERVAL_MSEC;
		}
		else if( aIntervalMillis > MAXIMUM_INTERVAL_MSEC )
		{
			currentIntervalMillis  = MAXIMUM_INTERVAL_MSEC;			
		}
		else
		{
			currentIntervalMillis  = aIntervalMillis;
		}
		
		if( aIntervalType == SCANNING_INTERVAL )
		{
			mScanningIntervalMillis = currentIntervalMillis;
		}
		else
		{
			mWaitingIntervalMillis = currentIntervalMillis;
		}
	}
	
	public long getScanningIntervalMillis()
	{
		return mScanningIntervalMillis;
	}
}
