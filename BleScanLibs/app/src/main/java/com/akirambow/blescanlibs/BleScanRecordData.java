package com.akirambow.blescanlibs;

public class BleScanRecordData {

	private String	mDeviceName;
	private String	mDeviceAddr;
	private int		mRssi;
	private byte[]	mScanRecord;
	private long	mTimestamp;
	protected boolean mIsAppleBeaconData;

	private AroundInfo mAroundInfo;
	
	private static final int RSSI_NONE      = Integer.MIN_VALUE;
	private static final int TIMESTAMP_NONE = -1;
	private static final String DEVICE_NAME_NONE = "NO_NAME";
	
	public BleScanRecordData(String aDeviceName, String aDeviceAddr, int aRssi, byte[] aRecord)
	{
		setEssentialInitValues(aDeviceName, aDeviceAddr, aRssi, aRecord);
		mTimestamp	= TIMESTAMP_NONE;
	}
	
	public BleScanRecordData(String aDeviceName, String aDeviceAddr, int aRssi, byte[] aRecord, long aTimestamp)
	{
		setEssentialInitValues(aDeviceName, aDeviceAddr, aRssi, aRecord);
		mTimestamp	= aTimestamp;		
	}
	
	private void setEssentialInitValues(String aDeviceName, String aDeviceAddr, int aRssi, byte[] aRecord)
	{
		mDeviceName = (aDeviceName != null) ? aDeviceName : DEVICE_NAME_NONE;
		mDeviceAddr = aDeviceAddr;
		mRssi       = aRssi;
		mScanRecord = aRecord;
		mIsAppleBeaconData = false;
		mAroundInfo = new AroundInfo();
	}
	
	public AroundInfo getAroundInfo()
	{
		return mAroundInfo;
	}
		
	public String getDeviceName()
	{
		return mDeviceName;
	}
	
	public String getDeviceAddress()
	{
		return mDeviceAddr;
	}
	
	public int getRssi()
	{
		return mRssi;
	}
	
	public byte[] getScanRecord()
	{
		return mScanRecord;
	}
	
	public long getTimestamp()
	{
		return mTimestamp;
	}
	
	public void updateRssiWithTimestamp(int aRssi, long aTimestamp)
	{
		mRssi = aRssi;
		mTimestamp = aTimestamp;
	}
	
	public void resetData()
	{
		mDeviceName = null;
		mDeviceAddr = null;
		mScanRecord = null;
		mRssi = RSSI_NONE;
		mTimestamp = TIMESTAMP_NONE;
	}
	
	public boolean isDuplicated(BleScanRecordData aAdvData)
	{
		if( mDeviceAddr ==null || aAdvData == null ) return false;
		return mDeviceAddr.equals(aAdvData.getDeviceAddress());
	}
	
	public boolean isAppleBeaconData()
	{
		return mIsAppleBeaconData;
	}
	
	public class AroundInfo
	{
		private boolean mIsScreenOn;
		private float[] mGyroAttitude;
		private float[] mGravity;
		private float[] mAccelero;
		
		public static final int INFO_NONE 			= 0x0;
		public static final int INFO_SCREEN_ON     	= 0x10;
		public static final int INFO_GYRO_ATTITUDE 	= 0x20;
		public static final int INFO_GRAVITY	   	= 0x40;
		public static final int INFO_ACCELERO		= 0x80;
				
		public AroundInfo()
		{
			mIsScreenOn   = false;
			mGyroAttitude = null;
			mGravity = null;
			mAccelero = null;
		}
		
		public void setAroundInfoFlag(int aType, boolean aFlag)
		{
			if( aType == INFO_SCREEN_ON )  mIsScreenOn = aFlag;
		}
		
		public boolean getAroundInfoFlag(int aType)
		{
			if( aType== INFO_SCREEN_ON ) return mIsScreenOn;
			return false;
		}
		
		public void setAroundInfoFloats(int aType, float[] aFloatDatas)
		{
			if( aType == INFO_GYRO_ATTITUDE) 	mGyroAttitude = aFloatDatas;
			if( aType == INFO_GRAVITY ) 		mGravity = aFloatDatas;
			if( aType == INFO_ACCELERO)			mAccelero = aFloatDatas;
		}
		
		public float[] getAroundInfoFloats(int aType)
		{
			if( aType == INFO_GYRO_ATTITUDE ) return mGyroAttitude;
			if( aType == INFO_GRAVITY ) return  mGravity;
			if( aType == INFO_ACCELERO ) return mAccelero;
			return null;
		}
				
	}
}
