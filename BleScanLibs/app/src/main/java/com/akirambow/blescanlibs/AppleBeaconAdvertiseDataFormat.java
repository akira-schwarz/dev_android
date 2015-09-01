package com.akirambow.blescanlibs;
import java.util.ArrayList;


public class AppleBeaconAdvertiseDataFormat  extends BleAdvertiseDataFormat {
	
	private String mUuidString;
	private long mMajorLong;
	private long mMinorLong;
	private long mIntensity;
	
	public AppleBeaconAdvertiseDataFormat(int aAdvType, byte[] aAdvRawData, String aUuid, long aMajor, long aMinor, long aIntensity)
	{
		super(aAdvType, aAdvRawData);
		mUuidString = aUuid;
		mMajorLong  = aMajor;
		mMinorLong  = aMinor;
		mIntensity  = aIntensity;
	}
	
	public String getUuidString()
	{
		return mUuidString;
	}
	
	public long getMajor()
	{
		return mMajorLong;
	}
	
	public long getMinor()
	{
		return mMinorLong;
	}
	
	public long mIntensity()
	{
		return mIntensity;
	}
	
	public static AppleBeaconAdvertiseDataFormat parseAdvertiseDataToAppleBeaconData(BleAdvertiseDataFormat aData)
	{
		if( aData == null) return null;
		if( aData.getAdvType() != 0xFF ) return null;
		
		byte[] payload = aData.getAdvPayloadData();

		// Check whether the data is based on iBeacon format or not
		if( payload == null ) return null;
		if( payload.length != 25) return null;
		if( (payload[0] & 0xFF) != 0x4C || (payload[1] & 0xFF) != 0x00 || (payload[2] & 0xFF) != 0x02 ) return null; 
		
		// get UUID value
		String uuidStr = new String("");
		for( int i=0; i<16; i++)
		{
			uuidStr = uuidStr + String.format("%02X", (int)(payload[i+4] & 0xFF));
			if( i==3 || i==5 || i==7 || i==9 ) uuidStr = uuidStr + "-";
		}
		
		// get Major value
		long majorValue = (long)( (payload[21] & 0xFF) * 0x100 + (payload[20] & 0xFF) ); 
		long minorValue = (long)( (payload[23] & 0xFF) * 0x100 + (payload[22] & 0xFF) );
		long intensity  = -1 * (0xFF - (payload[24] & 0xFF));

		return new AppleBeaconAdvertiseDataFormat(0xFF, payload, uuidStr, majorValue, minorValue, intensity);		
	}
	
	public static int findAppleBeaconData(ArrayList<BleAdvertiseDataFormat> aDataList)
	{
		if( aDataList == null ) return -1;
		if( aDataList.size() < 2 ) return -1;
		
		int retVal = -1;
		BleAdvertiseDataFormat currentData = null;
		
		for( int i=0; i<aDataList.size(); i++)
		{
			currentData = aDataList.get(i);
			if( currentData == null ) continue;
			if( currentData.getAdvType() != 0xFF || currentData.getAdvUnitLength() != 26 ) continue;
			
			byte[] payload = currentData.getAdvPayloadData();
			if( (payload[1] & 0xFF) == 0x4C && (payload[2] & 0xFF) == 0x00 && (payload[3] & 0xFF) == 0x02 )
			{
				retVal = i;
				break;
			}
		}
		return retVal;
	}
}
