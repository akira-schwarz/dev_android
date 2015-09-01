package com.akirambow.blebeacondetector;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;

public class BleBeaconDataRecordHandler extends Handler {

	private final WeakReference<BleBeaconDetectorMainActivity> mActivityRef;

	private BleBeaconDataRecorder mRecorder;
		
	public static final int MSG_RECORD_FILE_OPEN  = 100;
	public static final int MSG_RECORD_FILE_CLOSE = 101;
	public static final int MSG_WRITE_ONE_RECORD_AND_LINE = 300;
	public static final int MSG_WRITE_ONE_STRING_AND_LINE = 301;
	public static final int _SUCCESS = 0;
	public static final int _FAILURE = -1;

	public BleBeaconDataRecordHandler(android.os.Looper looper, BleBeaconDetectorMainActivity aActivity)
	{
		super(looper);
		mActivityRef = new WeakReference<BleBeaconDetectorMainActivity>(aActivity);
		mRecorder = null;
	}
	
	public void setRecorder(BleBeaconDataRecorder aRecorder)
	{
		mRecorder = aRecorder;
	}
	
	@Override
	public void handleMessage(Message aMsg)
	{
		if( mRecorder == null ) return;
		
		switch(aMsg.what)
		{
			case MSG_RECORD_FILE_OPEN:
				BleBeaconDetectorMainActivity activity = mActivityRef.get();
				if( activity != null )
				{
					if( mRecorder.isRecordFileOpened() )
					{
						activity.notifyFileOpenResult(_SUCCESS);
					}
					else
					{
						activity.notifyFileOpenResult( mRecorder.openRecordFile() ? _SUCCESS : _FAILURE );						
					}
				}
				break;
				
			case MSG_RECORD_FILE_CLOSE:
				if( mRecorder.isRecordFileOpened() ) mRecorder.closeRecordFile();
				break;
				
			case MSG_WRITE_ONE_RECORD_AND_LINE:
				BleAdvertiseDataStructure data = (BleAdvertiseDataStructure)aMsg.obj;
				mRecorder.writeOneRecord(data);
				break;
				
			case MSG_WRITE_ONE_STRING_AND_LINE:
				String writeStr = (String)aMsg.obj;
				mRecorder.writeDataStrAndLine(writeStr);
				break;
		}
	}
	
	public boolean isActive()
	{
		if( mRecorder == null ) return false;
		return mRecorder.isRecordFileOpened();
	}
}
