package com.akirambow.blebeacondetector;

public class IBeaconDataStructure {

	private int mFlag;
	private String mName;
	private int[] mUuidInt;
	private byte[] mUuidByte;
	private long mMajor;
	private long mMinor;
	private long mIntensity;
	
	IBeaconDataStructure()
	{
		resetData();
	}
	
	private void resetData()
	{
		mFlag      = -1;
		mName      = null;
		mUuidInt   = null;
		mUuidByte  = null;
		mMajor     = -1;
		mMinor     = -1;
		mIntensity =  0;		
	}
	
	public static boolean isIBeaconData(byte[] aData)
	{
		if( aData == null ) return false;
		if( aData.length < 30) return false;
		
		boolean retVal = true;
		
		retVal = retVal && ((aData[4] & 0xFF) == 0xFF); // ADType : Manufacture specific data
		retVal = retVal && ((aData[5] & 0xFF) == 0x4C); // 0x004C means Apple corp.
		retVal = retVal && ((aData[6] & 0xFF) == 0x00);
		retVal = retVal && ((aData[7] & 0xFF) == 0x02); // This byte indicates "iBeacon"
		
		return retVal;
	}
	
	public boolean parseIBeaconData(byte[] aData)
	{
		if( aData == null ) return false;
		if( aData.length < 30) return false;
		
		mFlag = aData[2] & 0xFF;
		mUuidInt  = new int[16];
		mUuidByte = new byte[16];
		for(int i=0; i<16; i++)
		{
			mUuidInt[i]  = aData[9+i] & 0xff;
			mUuidByte[i] = aData[9+i];
		}
		mMajor = (aData[25] & 0xff) * 256 + (aData[26] & 0xff);
		mMinor = (aData[27] & 0xff) * 256 + (aData[28] & 0xff);
		mIntensity = (0xFF - (aData[29] & 0xff)) * (-1);
		return true;
	}
	
	public void setName(String aString)
	{
		mName = aString;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public byte[] getUuidByte()
	{
		if( mUuidByte == null ) return null;
		return mUuidByte.clone();
	}
	
	public boolean compareBeaconDataByUuidByte(byte[] aData)
	{
		if( mUuidByte == null ) return false;
		if( !isIBeaconData(aData) ) return false;
		
		boolean retVal = true;
		for( int i=0; i<16; i++)
		{
			retVal = retVal && (mUuidByte[i] == aData[i]);
			if( !retVal ) break;
		}
		return retVal;
	}

	public void clearData()
	{
		resetData();
	}
	
	public void setUuid(byte[] aData)
	{
		if( aData == null )
		{
			mUuidInt = null;
			mUuidByte = null;
			return;
		}
		
		if( aData.length!=16 ) return;
		
		if( mUuidInt  == null ) mUuidInt  = new int[16];
		if( mUuidByte == null ) mUuidByte = new byte[16];

		for(int i=0; i<16; i++)
		{
			mUuidInt[i]  = aData[i] & 0xFF;
			mUuidByte[i] = aData[i];
		}
		
	}
	
	public long getMejorLong()
	{
		return mMajor;
	}

	public long getMinorLong()
	{
		return mMinor;
	}
	
	public int getFlagInt()
	{
		return mFlag;
	}
	
	public long getIntensityLong()
	{
		return mIntensity;
	}
	
	public String getUuidHexString()
	{
		if( mUuidInt == null ) return null;
		String retStr = new String("");
		for(int i=0; i<16; i++) retStr += String.format("%1$02X", mUuidInt[i]);
		return retStr;
	}
	
	public boolean compareEstimoteUuid()
	{
		if( mUuidInt == null ) return false;
		
		int[] estimoteUuid = {0xB9, 0x40, 0x7F, 0x30, 0xF5, 0xF8, 0x46, 0x6E,
							  0xAF, 0xF9, 0x25, 0x55, 0x6B, 0x57, 0xFE, 0x6D};
		boolean retVal = true;
		for(int i=0; i<16; i++)
		{
			retVal = retVal && (mUuidInt[i] == estimoteUuid[i]);
			if( !retVal ) break;
		}
		return retVal;
	}
}
