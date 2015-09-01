package com.akirambow.blescanlibs;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.util.ArrayList;



public class BleScanSequencer {
	
	private BleScanController mController;
	private final BleScanController.BleScanCallbackListener mScanListener = new BleScanController.BleScanCallbackListener()
		{
			@Override
			public void onBleScan(BleScanRecordData aScanData)
			{
				onBleScanCalled(aScanData);
			}
		};
	
	private BleScanTicker	  mTicker;	
	private final BleScanTicker.OnTicksExpiredListener mTickListener = new BleScanTicker.OnTicksExpiredListener()
		{
			@Override
			public void onExpired(int aExpiredType)
			{
				onTickerExpired(aExpiredType);			
			}
			
			@Override
			public void onTickerStarted(long aDuration, long aInterval)
			{
				onTickerStartedLocal(aDuration, aInterval);
			}
			
			@Override
			public void onTickerTerminated()
			{
				onTickerTerminatedLocal();
			}
		};
	
	private HandlerThread mHandlerThread;
	private BleScanSequenceHandler mHandler;
	
	private BleScanDataArrivalListener mDataArrivalListener;

	private int  mState;
	private long mBleScanCount;
	
	private static final int STATE_INITED = 5;
	private static final int STATE_IDLE   = 10;
	private static final int STATE_STARTING = 20;
	private static final int STATE_STARTED  = 30;
	private static final int STATE_STOPPING = 40;
	
	private static final int MSG_START_SEQUENCE  = 100;
	private static final int MSG_STOP_SEQUENCE   = 101;
	private static final int MSG_START_TICKER	 = 150;
	private static final int MSG_STOP_TICKER	 = 151;
	private static final int MSG_TICKER_EXPIRED  = 200;
	private static final int MSG_BLEDATA_ARRIVED = 300;

	private static final int ARG_NONE = -1;
	private static final String LOG_TAG = "BleScanSequencer";
	private static final String THREAD_NAME = "BleScanSequencer";

	public interface BleScanDataArrivalListener
	{
		public void onBleScanDataArrived(BleScanRecordData aData, long aCount);
	};
	
	public BleScanSequencer(Context aContext)
	{
		mController = new BleScanController(aContext);
		mTicker		= new BleScanTicker();
		mHandlerThread = null;
		mHandler = null;
		mDataArrivalListener = null;
		mState = STATE_INITED;
	}
	
	// Method for set up
	public void initHandler()
	{
		Log.d(LOG_TAG, "initHandler");
		if( mState == STATE_INITED )
		{
			mHandlerThread = new HandlerThread(THREAD_NAME);
			mHandlerThread.start();		
			mHandler = new BleScanSequenceHandler(mHandlerThread.getLooper());
			mState = STATE_IDLE;			
		}
		else
		{
			Log.w(LOG_TAG, "initHandler : called in invalid state.");
			return;			
		}
	}
	
	public void deinitHandler()
	{
		Log.d(LOG_TAG, "deinitHandler");
		if( mState != STATE_INITED)
		{
			mHandlerThread.quitSafely();
			mHandler = null;
			mHandlerThread = null;
			mState = STATE_INITED;			
		}
	}

	public void setBleScanDataArrivalListener(BleScanDataArrivalListener aListener)
	{
		mDataArrivalListener = aListener;
	}
	
	public void setScanDuration(long aTimeMillis)
	{
		mTicker.setScanDuration(aTimeMillis);
	}
	
	public void setScanInterval(long aTimeMillis)
	{
		mTicker.setScanInterval(aTimeMillis);
	}
	
	public void terminate()
	{
		mTicker.setOnTicksExpiredListener(null);
		mController.terminate();
		mTicker     = null;
		mController = null;
	}
	
	public void setFilteringAddress(ArrayList<String> aAddressList)
	{
		mController.setFilteringAddressList(aAddressList);
		mController.setUseFilter(true);
	}
	
	public void clearFilteringAddress()
	{
		mController.setFilteringAddressList(null);
		mController.setUseFilter(false);
	}
	
	public void setIbeconDataFiltering(boolean aFlag)
	{
		mController.setUseIbeaconFilter(aFlag);
	}

	// Method for Messaging ------------------------------------------------------------------
	public void startSequence()
	{
		Log.d(LOG_TAG, "startSequence");
		if( mState != STATE_IDLE )
		{
			Log.w(LOG_TAG, "startSequence : already started.");
			return;
		}
		getAndSendMessage(MSG_START_SEQUENCE, null);
		mState = STATE_STARTING;
	}
	
	public void stopSequence()
	{
		Log.d(LOG_TAG, "stopSequence");
		if( mState != STATE_STARTED )
		{
			Log.w(LOG_TAG, "stopSequence : not started yet.");
			return;
		}
		getAndSendMessage(MSG_STOP_SEQUENCE, null);
		mState = STATE_STOPPING;
	}
	
	
	private void onTickerExpired(int aExpiredType)
	{
		if( mState != STATE_STARTED )
		{
			Log.w(LOG_TAG, "onTickerExpired : called in invalid state");
			return;
		}
		getAndSendMessage(MSG_TICKER_EXPIRED, aExpiredType);
	}
	
	private void onTickerStartedLocal(long aDuration, long aInterval)
	{
		if( mState != STATE_STARTING )
		{
			Log.w(LOG_TAG, "onTickerStartedLocal : called in invalid state");
			return;
		}
		getAndSendMessage(MSG_START_TICKER, null, (int)aDuration, (int)aInterval);		
	}

	private void onTickerTerminatedLocal()
	{
		if( mState != STATE_STOPPING )
		{
			Log.w(LOG_TAG, "onTickerTerminatedLocal : called in invalid state");
			return;
		}
		getAndSendMessage(MSG_STOP_TICKER, null);
	}

	private void onBleScanCalled(BleScanRecordData aScanData)
	{
		if( mState != STATE_STARTED )
		{
			Log.w(LOG_TAG, "onBleScanCalled : called in invalid state");
			return;
		}
		getAndSendMessage(MSG_BLEDATA_ARRIVED, aScanData);
		
	}
		
	private class BleScanSequenceHandler extends Handler
	{
		public BleScanSequenceHandler(Looper aLooper)
		{
			super(aLooper);
		}
		
		@Override
		public void handleMessage(Message aMsg)
		{
			switch(aMsg.what)
			{
				case MSG_START_SEQUENCE:
					startSequenceDone();
					break;
					
				case MSG_STOP_SEQUENCE:
					stopSequenceLocalDone();
					break;
				
				case MSG_START_TICKER:
					onTickerStartedLocalDone((long)(aMsg.arg1), (long)(aMsg.arg2));
					break;
					
				case MSG_STOP_TICKER:
					onTickerTerminatedLocalDone();
					break;
					
				case MSG_TICKER_EXPIRED:
					if( aMsg.arg1 == BleScanTicker.SCAN_INTERVAL_EXPIRED )
					{
						startBleScan();
					}
					if( aMsg.arg1 == BleScanTicker.SCAN_DURATION_EXPIRED )
					{
						stopBleScan();
					}
					break;
					
				case MSG_BLEDATA_ARRIVED:
					onBleScanCalledDone( (BleScanRecordData)aMsg.obj );
					break;
					
				default:
					Log.w(LOG_TAG, "Unknown message.");
					break;
			}
		}
	}
	
	private void startSequenceDone()
	{
		mBleScanCount = 0;
		mController.resetStartTime();
		mController.setBleScanCallbackListener(mScanListener);
		mTicker.setOnTicksExpiredListener(mTickListener);
		mTicker.resetTicker();
		(new Thread(mTicker)).start();			
	}

	private void stopSequenceLocalDone()
	{
		mController.setBleScanCallbackListener(null);
		mTicker.stopTicker();			
	}
	
	private void onTickerStartedLocalDone(long aDuration, long aInterval)
	{
		Log.d(LOG_TAG, "Scan sequence is started successfully.");
		Log.d(LOG_TAG, "[Duration: " + aDuration + " ms / Interval: " + aInterval + " ms]");		
		mState = STATE_STARTED;
	}
	
	private void onTickerTerminatedLocalDone()
	{
		Log.d(LOG_TAG, "Scan sequence is terminated successfully.");
		mTicker.setOnTicksExpiredListener(null);				
		mState = STATE_IDLE;
	}
	
	private void startBleScan()
	{
		mController.startBleDeviceScan();
		mBleScanCount++;
	}
	
	private void stopBleScan()
	{
		mController.stopBleDeviceScan();
	}
	
	private void onBleScanCalledDone(BleScanRecordData aScanData)
	{
		if( mDataArrivalListener != null )
		{
			if( mState == STATE_STARTED ) mDataArrivalListener.onBleScanDataArrived(aScanData, mBleScanCount);
		}
	}
	
	private void getAndSendMessage(int aWhat, Object aObj, int aArg1, int aArg2)
	{
		Message msg = Message.obtain(mHandler);
		msg.what = aWhat;
		msg.obj  = aObj;
		msg.arg1 = aArg1;
		msg.arg2 = aArg2;
		mHandler.sendMessage(msg);						
	}
	
	private void getAndSendMessage(int aWhat, Object aObj)
	{
		getAndSendMessage(aWhat, aObj, ARG_NONE, ARG_NONE);
	}
	
	private void getAndSendMessage(int aWhat, int aArg)
	{
		getAndSendMessage(aWhat, null, aArg, ARG_NONE);
	}

}
