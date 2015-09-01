package com.akirambow.blebeacondetector;

public class BleAdvertiseDataStructure {
	private String mDeviceName;
	private String mDeviceAddr;
	private int    mRssi;
	private byte[] mScanRecord;
	private long   mTimeMillis;

	public BleAdvertiseDataStructure(String aDeviceName, String aDeviceAddr, int aRssi, byte[] aRecord)
	{
		mDeviceName = aDeviceName;
		mDeviceAddr = aDeviceAddr;
		mRssi       = aRssi;
		mScanRecord = null;
		if( aRecord != null ) mScanRecord = aRecord.clone();
		mTimeMillis = -1;
	}
	
	public void setScannedTime(long aTime)
	{
		mTimeMillis = aTime;
	}
	
	public long getScannedTime()
	{
		return mTimeMillis;
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
	
	public boolean isDuplicatedData(BleAdvertiseDataStructure aAdvData)
	{
		if( mDeviceAddr ==null || aAdvData == null ) return false;
		return mDeviceAddr.equals(aAdvData.getDeviceAddress());
	}
	
	public boolean compareAddress(String aAddr)
	{
		if( mDeviceAddr == null ) return false;
		return mDeviceAddr.equalsIgnoreCase(aAddr);
	}

}
