package com.akirambow.blescanlibs;
import java.util.ArrayList;

public class BleAdvertiseDataFormat {
	
	private int  mAdvTypeIndex;
	private long mAdvUnitLength;
	private byte[] mPayloadDataHex;
	
//	private int mAdvFlagIndex;
//	private String mStringData;
	
	public static int AS_UNSIGNED_INT_8  = 100;
	public static int AS_UNSIGNED_INT_16 = 200;
	public static int AS_UNSIGNED_INT_32 = 400;
	public static int AS_UNSIGNED_INT_64 = 800;
	public static int AS_BIG_ENDIAN = 1600;
	public static int AS_LITTLE_ENDIAN = 3200;
	
	private static int RETVAL_INVALID_INT = -1;

	public BleAdvertiseDataFormat(int aAdvType, byte[] aAdvRawData)
	{
		mAdvTypeIndex = aAdvType;
		mPayloadDataHex = aAdvRawData;
		mAdvUnitLength  = ( mPayloadDataHex != null ) ? mPayloadDataHex.length + 1 :  1; 
	}
			
	public int getAdvType()
	{
		return mAdvTypeIndex;
	}
	
	public long getAdvUnitLength()
	{
		return mAdvUnitLength;
	}
	
	public byte[] getAdvPayloadData()
	{
		return mPayloadDataHex;
	}
	
	public byte getAdvPayloadDataHead()
	{
		if( mPayloadDataHex == null ) return 0x0;
		return mPayloadDataHex[0];
	}
	
	public String getAdvPayloadDataAsByteString()
	{
		return getAdvPayloadDataAsByteString(AS_BIG_ENDIAN);
	}

	public String getAdvPayloadDataAsByteString(int aEndian)
	{
		if( mPayloadDataHex == null) return null;
		String retVal = new String("");
		for( int i=0; i<mPayloadDataHex.length; i++)
		{
			if( aEndian == AS_BIG_ENDIAN )
			{
				retVal = retVal + String.format("%02X", (int)(mPayloadDataHex[i] & 0xFF) );				
			}
			else
			{
				retVal = retVal + String.format("%02X", (int)(mPayloadDataHex[mPayloadDataHex.length-1-i] & 0xFF) );	
			}
		}
		return retVal;
	}

	public long getAdvPayloadDataAsUnsignedInt(int aDigit, int aEndian)
	{
		if( mPayloadDataHex == null) return  RETVAL_INVALID_INT;
		
		long retVal = RETVAL_INVALID_INT;
		int roopMax = 0;
		if( aDigit == AS_UNSIGNED_INT_8 ) roopMax = 1;
		if( aDigit == AS_UNSIGNED_INT_16 && mPayloadDataHex.length >= 2 ) roopMax = 2;
		if( aDigit == AS_UNSIGNED_INT_32 && mPayloadDataHex.length >= 4 ) roopMax = 4;
		if( aDigit == AS_UNSIGNED_INT_64 && mPayloadDataHex.length >= 8 ) roopMax = 8;

		if( roopMax > 0 )
		{
			retVal = 0;
			for( int i=0; i<roopMax; i++)
			{
				if( aEndian == AS_BIG_ENDIAN)
				{
					retVal = retVal * 0x100 + (long)(mPayloadDataHex[i] & 0xFF);						
				}
				else
				{
					retVal = retVal * 0x100 + (long)(mPayloadDataHex[roopMax-i-1] & 0xFF);	
				}				
			}
		}
		return retVal;
	}

	public long getAdvPayloadDataAsUnsignedInt(int aDigit)
	{
		return getAdvPayloadDataAsUnsignedInt(aDigit, AS_LITTLE_ENDIAN);
	}


	public static ArrayList<BleAdvertiseDataFormat> parseScanRecordToAdvDatas(byte[] aAdvRecord)
	{
		if( aAdvRecord == null) 	return null;
		if( aAdvRecord.length < 2 ) return null; // At least 2 bytes (Length and AdvType field should be included.)
		
		int index = 0;
		int  currentAdvTypeInd = 0;
		long currentLength = 0;
		byte[] currentData = null;
		long restLength = aAdvRecord.length;
		ArrayList<BleAdvertiseDataFormat> dataList = null;
		
		while( index < aAdvRecord.length )
		{
			currentLength		= (long)(aAdvRecord[index] & 0xFF); // Length
			if( currentLength < 1 || currentLength > restLength - 1 ) break;
			
			index++;  restLength--;
			
			currentAdvTypeInd	= aAdvRecord[index] & 0xFF; // ADV Type
			index++;  restLength--;
			
			if( currentLength > 1 )
			{
				currentData = new byte[(int)currentLength-1]; // Decrease 1 byte for ADV TYPE 
				for( int i=0; i<currentData.length; i++) currentData[i] = aAdvRecord[index + i];				
			}
			else
			{
				currentData = null;
			}
			
			if( dataList == null ) dataList = new ArrayList<BleAdvertiseDataFormat>();
			dataList.add(new BleAdvertiseDataFormat(currentAdvTypeInd, currentData));

			index = index + currentData.length;
			restLength = restLength - currentData.length;
		}
		return dataList;
	}
	
}
