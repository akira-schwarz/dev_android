package com.akirambow.blescanlibs;

public class AppleBeaconRecordData extends BleScanRecordData {

	private String mUuidHexString;
	private byte[] mUuidByteArray;
	private int mMajorInt;
	private int mMinorInt;
	private int mCalibratedRssiInt;
	
	private static final int NO_VALUE = -1;
	private static final int NO_RSSI_VALUE = Integer.MIN_VALUE;
	
	public AppleBeaconRecordData(String aDeviceName, String aDeviceAddr, int aRssi, byte[] aRecord)
	{
		super(aDeviceName, aDeviceAddr, aRssi, aRecord);
		parseRecord(aRecord);
	}
	
	public AppleBeaconRecordData(String aDeviceName, String aDeviceAddr, int aRssi, byte[] aRecord, long aTimestamp)
	{
		super(aDeviceName, aDeviceAddr, aRssi, aRecord, aTimestamp);
		parseRecord(aRecord);
	}

	
	private void parseRecord(byte[] aRecord)
	{
		if( isAppleBeaconRecordData(aRecord) )
		{
			mUuidHexString = new String("");
			mUuidByteArray = new byte[16];
			for( int i=0; i<16; i++)
			{
				mUuidHexString = mUuidHexString + String.format("%02X", (int)(aRecord[i+9] & 0xFF));
				if( i==3 || i==5 || i==7 || i==9 ) mUuidHexString = mUuidHexString + "-";
				mUuidByteArray[i] = aRecord[i+9];
			}			
			mMajorInt =  (aRecord[25] & 0xFF) * 0x100 + (aRecord[26] & 0xFF); 
			mMinorInt =  (aRecord[27] & 0xFF) * 0x100 + (aRecord[28] & 0xFF);
			mCalibratedRssiInt = -1 * (0xFF - (aRecord[29] & 0xFF));

			mIsAppleBeaconData = true;
		}
		else
		{
			mUuidHexString = null;
			mUuidByteArray = null;
			mMajorInt = NO_VALUE;
			mMinorInt = NO_VALUE;
			mCalibratedRssiInt = NO_RSSI_VALUE;
			mIsAppleBeaconData = false;
		}
	}
	
	public String getUuidHexString()
	{
		return mUuidHexString;
	}
	
	public byte[] getUuidHexBytes()
	{
		return mUuidByteArray;
	}
	
	public int getMajorValue()
	{
		return mMajorInt;
	}
	
	public int getMinorValue()
	{
		return mMinorInt;
	}
	
	public int getCalibratedRssiValue()
	{
		return mCalibratedRssiInt;
	}

	public static boolean isAppleBeaconRecordData(byte[] aRecord)
	{
		return isAppleBeaconRecordData(aRecord, false);
	}

	public static boolean isAppleBeaconRecordData(byte[] aRecord, boolean aStrictCheck)
	{
		if( aRecord == null )		return false;
		if( aRecord.length < 30)	return false;
		
		boolean isAppleBeaconData = true;
		if( aStrictCheck )
		{
			isAppleBeaconData &= (aRecord[0] == (byte)0x02); // Number of bytes of first ADV data
			isAppleBeaconData &= (aRecord[1] == (byte)0x01); // ADV type (Flag)
			isAppleBeaconData &= (aRecord[2] == (byte)0x06); // Flag value
			isAppleBeaconData &= (aRecord[3] == (byte)0x1A); // Number of bytes of 2nd ADV data
		}
		isAppleBeaconData &= (aRecord[4] == (byte)0xFF); // ADV type (Manufacture specific data)
		isAppleBeaconData &= (aRecord[5] == (byte)0x4C); // Company code (0x004C)
		isAppleBeaconData &= (aRecord[6] == (byte)0x00); // Company code (0x004C)
		isAppleBeaconData &= (aRecord[7] == (byte)0x02); // indicator of iBeacon
		if( aStrictCheck )
		{
			isAppleBeaconData &= (aRecord[8] == (byte)0x15); // Length of iBeacon's value data			
		}
		return isAppleBeaconData;
	}
}
