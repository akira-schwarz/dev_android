package com.akirambow.blescanlibs;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class BleScannedDataWriteController {

	protected BleScannedDataWriter 	mWriter;
	private HandlerThread			mHandlerThread;
	private DataWriterHandler		mWriterHandler;
	
	private volatile int mState;
	
	private static final int STATE_INITED = 5;
	private static final int STATE_IDLE   = 10;
	private static final int STATE_STARTING = 20;
	private static final int STATE_STARTED  = 30;
	private static final int STATE_STOPPING = 40;

	private static final int MSG_START_WRITING		= 100;
	private static final int MSG_STOP_WRITING		= 101;
	private static final int MSG_WRITE_ONE_RECORD	= 150;
	
	private static final String LOG_TAG = "BleScannedDataWriteController";
	private static final String THREAD_NAME = "BleScannedDataWriter";

	public BleScannedDataWriteController()
	{
		mWriter = new BleScannedDataWriter();
		mHandlerThread = null;
		mWriterHandler = null;
		mState = STATE_INITED;
	}
	
	public void initHandler()
	{
		Log.d(LOG_TAG, "initHandler");
		if( mState == STATE_INITED )
		{
			mHandlerThread = new HandlerThread(THREAD_NAME);
			mHandlerThread.start();		
			mWriterHandler = new DataWriterHandler(mHandlerThread.getLooper());
			mState = STATE_IDLE;						
		}
		else
		{
			Log.w(LOG_TAG, "initHandler: called in invalid state.");
		}
	}
	
	public void deinitHandler()
	{
		Log.d(LOG_TAG, "deinitHandler E");
		if( mState != STATE_INITED )
		{
			mHandlerThread.quitSafely();
			mWriterHandler = null;
			mHandlerThread = null;
			mState = STATE_INITED;			
		}
		Log.d(LOG_TAG, "deinitHandler X");
	}
	
	public void setFilePath(String aFilePath)
	{
		if( mState == STATE_IDLE ) mWriter.setRecordFilePath(aFilePath);
	}
		
	public void setUseIbeaconData(boolean aFlag)
	{
		mWriter.setUseIbeaconData(aFlag);
	}
	
	public void setUseAroundInfo(boolean aFlag)
	{
		mWriter.setUseAroundInfo(aFlag);
	}
	
	public void setUseAroundInfo(boolean aFlag, int aDetails )
	{
		mWriter.setUseAroundInfo(aFlag, aDetails);
	}
	
	public void startWriting()
	{
		if( mState == STATE_IDLE )
		{
			obtainAndSendMessage(MSG_START_WRITING, null);
			mState = STATE_STARTING;
		}
		else
		{
			Log.w(LOG_TAG, "startWriting: called in invalid state.");			
		}
	}
	
	private void startWritingDone()
	{
		boolean success = mWriter.openRecordFile();
		if( success )
		{
			Log.d(LOG_TAG, "startWriting : successfully done.");
			mState = STATE_STARTED;
		}
		else
		{
			Log.e(LOG_TAG, "startWriting : fail to open file");
			mState = STATE_IDLE;
		}
	}
	
	public boolean isWriting()
	{
		return (mState == STATE_STARTED) ? true : false;
	}

	public void stopWriting()
	{
		if( mState == STATE_STARTED )
		{
			obtainAndSendMessage(MSG_STOP_WRITING, null);
			mState = STATE_STOPPING;
		}
		else
		{
			Log.w(LOG_TAG, "stopWriting: called in invalid state.");			
		}		
	}
	
	private void stopWritingDone()
	{
		mWriter.closeRecordFile();
		mState = STATE_IDLE;
		Log.d(LOG_TAG, "stopWriting : successfully done.");
	}
	
	
	public void writeOneRecord(BleScanRecordData aData)
	{
		if( mState == STATE_STARTED )
		{
			obtainAndSendMessage(MSG_WRITE_ONE_RECORD, aData);			
		}
		else
		{
			Log.w(LOG_TAG, "writeOneRecord: called in invalid state.");			
		}		
	}
	
	private void writeOneRecordDone(BleScanRecordData aData)
	{
		mWriter.writeOneRecord(aData);
	}
	
	private void obtainAndSendMessage(int aWhat, Object aObj)
	{
		if( mWriterHandler != null)
		{
			Message msg = Message.obtain(mWriterHandler);
			msg.what = aWhat;
			msg.obj  = aObj;
			mWriterHandler.sendMessage(msg);									
		}
	}

	
	private class DataWriterHandler extends Handler
	{
		public DataWriterHandler(Looper aLooper)
		{
			super(aLooper);
		}
		
		@Override
		public void handleMessage(Message aMsg)
		{
			switch(aMsg.what)
			{
				case MSG_START_WRITING:
					startWritingDone();
					break;
					
				case MSG_STOP_WRITING:
					stopWritingDone();
					break;
					
				case MSG_WRITE_ONE_RECORD:
					writeOneRecordDone((BleScanRecordData)aMsg.obj);
					break;
					
				default:
					Log.w(LOG_TAG, "handleMessage : unknown message");
					break;
			}
		}
	}		
}
