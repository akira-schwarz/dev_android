package com.akirambow.blescanlibs;

import android.util.Log;

public class BleScanTicker implements Runnable {

	private long mScanDurationMillis;
	private long mScanIntervalMillis;
	
	private int mRunningState;
	
	private volatile OnTicksExpiredListener mListener;
	private volatile boolean mKeepRunning;
	
	private static final int  STATE_INITED   = 10000;
	private static final int  STATE_WAITING  = 10100;
	private static final int  STATE_SCANNING = 10200;
	
	private static final long VALID_TIME_MSEC_MIN	= 50;
	private static final long VALID_TIME_MSEC_MAX	= 1000 * 60 * 60 * 24; // 24 H
	private static final long DEFAULT_DURATION_MSEC = 1000;
	private static final long DEFAULT_INTERVAL_MSEC = 4000;
	
	public static final int SCAN_DURATION_EXPIRED = 100;
	public static final int SCAN_INTERVAL_EXPIRED = 101;	

	private static final String LOG_TAG = "BleScanTicker";
	
	public BleScanTicker()
	{
		mScanDurationMillis = DEFAULT_DURATION_MSEC;
		mScanIntervalMillis = DEFAULT_INTERVAL_MSEC;
		mRunningState = STATE_INITED;
		mKeepRunning = true;
	}

	@Override
	public void run() {
		long sleepTime;
		
		Log.d(LOG_TAG, "Ticker thread started.");
		sleepTime = mScanDurationMillis;
				
		if( mListener != null ) mListener.onTickerStarted(mScanDurationMillis, mScanIntervalMillis);
				
		while( mListener != null )
		{
			switch(mRunningState)
			{
			case STATE_INITED:
			case STATE_WAITING:
				mRunningState = STATE_SCANNING;
				sleepTime = mScanDurationMillis;
				if( mListener != null ) mListener.onExpired(SCAN_INTERVAL_EXPIRED);
				break;
				
			case STATE_SCANNING:
				mRunningState = STATE_WAITING;
				sleepTime = mScanIntervalMillis;
				if( mListener != null ) mListener.onExpired(SCAN_DURATION_EXPIRED);
				break;
			}
			
			try
			{
				Thread.sleep(sleepTime);
			}
			catch(Exception e)
			{
				Log.w(LOG_TAG, "Thread interruption occured.");
			}
			
			if( !mKeepRunning)
			{
				if( mRunningState == STATE_SCANNING )
				{
					if( mListener != null ) mListener.onExpired(SCAN_DURATION_EXPIRED);
				}
				break;
			}
		}

		mRunningState = STATE_INITED;
		if( mListener != null ) mListener.onTickerTerminated();
		Log.d(LOG_TAG, "Ticker thread terminated.");
	}

	public void stopTicker()
	{
		mKeepRunning = false;
	}
	
	public void resetTicker()
	{
		mKeepRunning = true;
	}
	
	public void setScanDuration(long aDurationMillis)
	{
		if( mRunningState != STATE_INITED ) return;
		if( aDurationMillis < VALID_TIME_MSEC_MIN )
		{
			mScanDurationMillis = VALID_TIME_MSEC_MIN;
		}
		else if ( aDurationMillis > VALID_TIME_MSEC_MAX )
		{
			mScanDurationMillis = VALID_TIME_MSEC_MAX;			
		}
		else
		{
			mScanDurationMillis = aDurationMillis;
		}
	}
	
	public void setScanInterval(long aIntervalMillis)
	{
		if( mRunningState != STATE_INITED ) return;
		if( aIntervalMillis < VALID_TIME_MSEC_MIN )
		{
			mScanIntervalMillis = VALID_TIME_MSEC_MIN;
		}
		else if ( aIntervalMillis > VALID_TIME_MSEC_MAX )
		{
			mScanIntervalMillis = VALID_TIME_MSEC_MAX;			
		}
		else
		{
			mScanIntervalMillis = aIntervalMillis;
		}		
	}
	
	
	public long getScanDurationMillis()
	{
		return mScanDurationMillis;
	}
	
	public long getScanIntervalMillis()
	{
		return mScanIntervalMillis;
	}
	
	public interface OnTicksExpiredListener
	{
		public void onExpired(int aExpiredType);
		public void onTickerStarted(long aDuration, long aInterval);
		public void onTickerTerminated();
	}
	
	public void setOnTicksExpiredListener(OnTicksExpiredListener aListener)
	{
		mListener = aListener;
	}

}
