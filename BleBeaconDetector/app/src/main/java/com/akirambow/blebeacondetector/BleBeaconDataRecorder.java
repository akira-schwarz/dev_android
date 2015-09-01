package com.akirambow.blebeacondetector;

import java.io.FileWriter;
import java.io.BufferedWriter;

import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

public class BleBeaconDataRecorder {

	private BufferedWriter mWriteBuffer;
	private String mFilePath;
	private String mNote;
	
	private static final String DEFAULT_FILE_PATH  = Environment.getExternalStorageDirectory().getPath() + "/PRIVATE/SHARP/";
//	private static final String DEFAULT_FILE_PATH  = "/sdcard/PRIVATE/SHARP/";
	private static final String FILE_EXTENSION     = ".log";
	private static final String RECORD_DATA_HEADER_STR = "DeviceName\tDeviceAddr\tRSSI\tTimestampS";

	private static final String LOG_TAG = "BleBeaconDataRecorder";
	
	public BleBeaconDataRecorder(String aFilePath )
	{
		initVariablesWithFilePath(aFilePath);
	}
	
	public BleBeaconDataRecorder()
	{
		initVariablesWithFilePath(null);
	}
	
	private void initVariablesWithFilePath(String aFilePath)
	{
		mFilePath = aFilePath;
		mWriteBuffer = null;
		mNote = null;
	}

	public void setRecordFilePath(String aFilePath)
	{
		mFilePath = aFilePath;
	}
	
	public boolean openRecordFile()
	{
		Log.d(LOG_TAG,"openRecordFile");
		Time t = new Time("Asia/Tokyo");
		t.setToNow();
		try
		{
			String filePath = (mFilePath!=null) ? mFilePath  :  DEFAULT_FILE_PATH;
			Log.d(LOG_TAG, "write path:" + filePath);
			Log.d(LOG_TAG, "default path:" + Environment.getExternalStorageDirectory().getPath() + "/PRIVATE/SHARP/");
			mWriteBuffer = new BufferedWriter(
								new FileWriter(
										filePath
										+ t.toString().substring(0,8)
										+ t.toString().substring(9,15)
										+ FILE_EXTENSION ) );
			if( mNote != null )
			{
				writeDataStrAndLine(RECORD_DATA_HEADER_STR + "\tNote:\t" + mNote);
			}
			else
			{
				writeDataStrAndLine(RECORD_DATA_HEADER_STR);				
			}
		}
		catch(Exception e)
		{
			Log.w(LOG_TAG, "Fail to open file for write.:" + e);
			mWriteBuffer  = null;
			return false;
		}		
		return true;
	}
	
	public void closeRecordFile()
	{
		Log.d(LOG_TAG,"closeRecordFile");
		if( mWriteBuffer ==null ) return;
		try
		{
			mWriteBuffer.close();			
		}
		catch(Exception e)
		{
			Log.w(LOG_TAG, "Fail to close file :" + e);
		}
		mWriteBuffer = null;
		return;
	}
	
	public void writeDataStrAndLine(String aDataStr)
	{
		if( mWriteBuffer == null ) return;
		try
		{
			mWriteBuffer.write(aDataStr);
			mWriteBuffer.newLine();
		}
		catch(Exception e)
		{
			Log.w(LOG_TAG, "Fail to write data to file :" + e);			
		}
	}
	
	public boolean isRecordFileOpened()
	{
		return (mWriteBuffer != null );
	}
	
	public void writeOneRecord(BleAdvertiseDataStructure aBleData)
	{
		Log.d(LOG_TAG, "writeOneRecord");
		if( aBleData == null ) return;
		
		String outStr = new String("");
		outStr += aBleData.getDeviceName()    + "\t";
		outStr += aBleData.getDeviceAddress() + "\t";
		outStr += aBleData.getRssi() + "\t";
		outStr += aBleData.getScannedTime();		
		writeDataStrAndLine(outStr);
	}
	
	public void setRecordNoteString(String aStr)
	{
		mNote = aStr;
	}

}
