package com.akirambow.blescanlibs;

import java.io.FileWriter;
import java.io.BufferedWriter;

import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

public class BleScannedDataWriter {

	private BufferedWriter mWriteBuffer;
	private String mFilePath;
	private String mNote;
	
	private boolean mUseIbeaconData;
	private boolean mUseAroundInfo;
	private int mRecordAroundInfoFlags;
	
	private static final int AINFO_NONE		= BleScanRecordData.AroundInfo.INFO_NONE;
	private static final int AINFO_SCREEN	= BleScanRecordData.AroundInfo.INFO_SCREEN_ON;
	private static final int AINFO_GYRO		= BleScanRecordData.AroundInfo.INFO_GYRO_ATTITUDE;
	private static final int AINFO_GRAVITY	= BleScanRecordData.AroundInfo.INFO_GRAVITY;
	private static final int AINFO_ACCELERO	= BleScanRecordData.AroundInfo.INFO_ACCELERO;
		
	private static final String DEFAULT_FILE_PATH  = Environment.getExternalStorageDirectory().getPath() + "/Download/";
//	private static final String DEFAULT_FILE_PATH  = "/sdcard/PRIVATE/SHARP/";
	private static final String FILE_EXTENSION     = ".log";
	private static final String RECORD_DATA_HEADER_STR = "DeviceName\tDeviceAddr\tRSSI\tTimestamp";
	private static final String RECORD_DATA_HEADER_STR_INCLUDE_IBEACON = "DeviceName\tDeviceAddr\tRSSI\tTimestamp\tUUID\tMajor\tMinor\tC-RSSI";

	private static final String LOG_TAG = "BleBeaconDataRecorder";
	
	public BleScannedDataWriter(String aFilePath )
	{
		initVariablesWithFilePath(aFilePath);
		mUseIbeaconData   = false;
		mUseAroundInfo    = false;
		mRecordAroundInfoFlags = AINFO_NONE;
	}
		
	public BleScannedDataWriter()
	{
		initVariablesWithFilePath(null);
		mUseIbeaconData = false;
		mUseAroundInfo    = false;
		mRecordAroundInfoFlags = AINFO_NONE;
	}
	
	public void setUseIbeaconData(boolean aFlag)
	{
		if( mWriteBuffer==null ) mUseIbeaconData = aFlag;
	}
	
	public void setUseAroundInfo(boolean aFlag)
	{
		if( mWriteBuffer==null ) mUseAroundInfo = aFlag;
	}
	
	public void setUseAroundInfo(boolean aFlag, int aRecordInfoFlags)
	{
		if( mWriteBuffer==null ) 
		{
			mUseAroundInfo = aFlag;
			mRecordAroundInfoFlags = aRecordInfoFlags;			
		}
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
										+ "BLELOG_"
										+ t.toString().substring(0,8) // yyyymmdd
										+ "_"
										+ t.toString().substring(9,15) //hhmmss
										+ FILE_EXTENSION ) );
			String headerString = mUseIbeaconData ? RECORD_DATA_HEADER_STR_INCLUDE_IBEACON : RECORD_DATA_HEADER_STR;
			if( mUseAroundInfo )
			{
				if( (mRecordAroundInfoFlags & AINFO_SCREEN) > 0  )  headerString += "\tScreen";
				if( (mRecordAroundInfoFlags & AINFO_GYRO) > 0    )  headerString += "\tGYRO_Vx\tGYRO_Vy\tGYRO_Vz";
				if( (mRecordAroundInfoFlags & AINFO_GRAVITY) > 0 )  headerString += "\tGRAVITY_Ax\tGRAVITY_Ay\tGRAVTIY_Az";
				if( (mRecordAroundInfoFlags & AINFO_ACCELERO) > 0)  headerString += "\tACCELERO_X\tACCELERO_Y\tACCELERO_Z";
			}
			
			if( mNote != null )
			{
				writeDataStrAndLine(headerString + "\tNote:\t" + mNote);
			}
			else
			{
				writeDataStrAndLine(headerString);				
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
	
	private void writeDataStrAndLine(String aDataStr)
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
	
	public void writeOneRecord(BleScanRecordData aBleData)
	{
		Log.d(LOG_TAG, "writeOneRecord");
		if( aBleData == null ) return;
		
		String outStr = new String("");
		outStr += aBleData.getDeviceName()    + "\t";
		outStr += aBleData.getDeviceAddress() + "\t";
		outStr += aBleData.getRssi() + "\t";
		outStr += aBleData.getTimestamp();
		if( mUseIbeaconData )
		{
			if( aBleData.isAppleBeaconData() )
			{
				AppleBeaconRecordData iBeaconData = (AppleBeaconRecordData)aBleData;
				outStr += "\t";
				outStr += iBeaconData.getUuidHexString() + "\t";
				outStr += iBeaconData.getMajorValue() + "\t";
				outStr += iBeaconData.getMinorValue() + "\t";
				outStr += iBeaconData.getCalibratedRssiValue();
			}
		}
		
		if( mUseAroundInfo )
		{
			BleScanRecordData.AroundInfo info = aBleData.getAroundInfo();
			if( (mRecordAroundInfoFlags & AINFO_SCREEN) > 0  )
			{
				outStr += "\t";
				outStr += (info.getAroundInfoFlag(BleScanRecordData.AroundInfo.INFO_SCREEN_ON) ? "ON" : "OFF");				
			}
			
			// float[] floatData;
			if( (mRecordAroundInfoFlags & AINFO_GYRO) > 0  )
				outStr += createThreeDementionsFloatsString(info.getAroundInfoFloats(AINFO_GYRO));

			if( (mRecordAroundInfoFlags & AINFO_GRAVITY) > 0  )
				outStr += createThreeDementionsFloatsString(info.getAroundInfoFloats(AINFO_GRAVITY));

			if( (mRecordAroundInfoFlags & AINFO_ACCELERO) > 0  )
				outStr += createThreeDementionsFloatsString(info.getAroundInfoFloats(AINFO_ACCELERO));

		}
		writeDataStrAndLine(outStr);
	}
	
	public void setRecordNoteString(String aStr)
	{
		mNote = aStr;
	}
		
	private String createThreeDementionsFloatsString(float[] aFloat)
	{
		String outStr = new String("");
		if( aFloat != null && aFloat.length >= 3)
		{
			outStr += ("\t" + aFloat[0] + "\t" + aFloat[1] + "\t" + aFloat[2]);				
		}
		else
		{
			outStr += "\tInvalid\tInvalid\tInvalid";
		}
		return outStr;
		
	}

}
