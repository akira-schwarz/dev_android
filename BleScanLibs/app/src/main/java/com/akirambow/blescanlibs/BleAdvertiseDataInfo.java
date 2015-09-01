package com.akirambow.blescanlibs;

public class BleAdvertiseDataInfo {
	
	private static final String[] AD_TYPE_STR =
		{
			/*0x00*/ "Undefined",
			/*0x01*/ "Flags",
			/*0x02*/ "Incomplete list of 16-bit Service Class UUIDs",
			/*0x03*/ "Complete list of 16-bit Service Class UUIDs",
			/*0x04*/ "Incomplete list of 32-bit Service Class UUIDs",
			/*0x05*/ "Complete list of 32-bit Service Class UUIDs",
			/*0x06*/ "Incomplete list of 128-bit Service Class UUIDs",
			/*0x07*/ "Complete list of 128-bit Service Class UUIDs",
			/*0x08*/ "Shortened Local Name",
			/*0x09*/ "Complete Local Name",
			/*0x0A*/ "Manufacture Specific Data",
			/*0x0B*/ "Undefined",
			/*0x0C*/ "Undefined",
			/*0x0D*/ "Class of Device",
			/*0x0E*/ "Simple Pairing Hash C",
			/*0x0F*/ "Simple Pairing Randomizer R",
			/*0x10*/ "Security Manager TK Value",
			/*0x11*/ "Security Manager Out of Band Flags",
			/*0x12*/ "Slave Connection Interval Range",
			/*0x13*/ "Undefined",
			/*0x14*/ "List of 16-bit Service Solicitation UUIDs",
			/*0x15*/ "List of 128-bit Service Solicitation UUIDs",
			/*0x16*/ "Service Data",
			/*0x17*/ "Public Target Address",
			/*0x18*/ "Random Target Address",
			/*0x19*/ "Appearance",
			/*0x1A*/ "Advertising Interval",
			/*0x1B*/ "LE Bluetooth Device Address",
			/*0x1C*/ "LE Role",
			/*0x1D*/ "Simple Pairing Hash C-256",
			/*0x1E*/ "Simple Pairing Randomizer R-256",
			/*0x1F*/ "Undefined",
			/*0x20*/ "List of 32-bit Service Solicitation UUIDs",
			/*0x21*/ "List of 128-bit Service Solicitation UUIDs"
		};
	
	private static final String ADTYPE_3D_STR = "3D Information Data";
	private static final String ADTYPE_FF_STR = "Manufacturer Specific Data";
	private static final String ADTYPE_INVALID = "INVALID ADTYPE INDEX"
	;
	private static final String ADFLAG_LE_LIMITED_DISCOVERY = "LE Limited Discovery mode";
	private static final String ADFLAG_LE_GENERAL_DISCOVERY = "LE General Discovery mode";
	private static final String ADFLAG_BR_EDR_NOT_SUPPORTED = "BR/EDR Not Supported";
	private static final String ADFLAG_SIMULTANEOUS_CTRLER  = "Simultaneous LE and BR/EDR to Same Device Capa-(Controller)";
	private static final String ADFLAG_SIMULTANEOUS_HOST    = "Simultaneous LE and BR/EDR to Same Device Capa-(Host)";
	private static final String ADFLAG_UNKNOWN = "Unknown Flag";
	
	public static String getADTypeString(int aAdvType)
	{
		if( aAdvType >= 0x0 && aAdvType < 0x22 ) return AD_TYPE_STR[aAdvType];
		if( aAdvType == 0x3D) return ADTYPE_3D_STR;
		if( aAdvType == 0xFF) return ADTYPE_FF_STR;
		return ADTYPE_INVALID;
	}
	
	public static String getADTypeFlagString(int aFlagValue)
	{
		String retStr = new String("");
		
		if( (aFlagValue & 0x01) > 0 )  retStr += "- " + ADFLAG_LE_LIMITED_DISCOVERY + "\n";
		if( (aFlagValue & 0x02) > 0 )  retStr += "- " + ADFLAG_LE_GENERAL_DISCOVERY + "\n";
		if( (aFlagValue & 0x04) > 0 )  retStr += "- " + ADFLAG_BR_EDR_NOT_SUPPORTED + "\n";
		if( (aFlagValue & 0x08) > 0 )  retStr += "- " + ADFLAG_SIMULTANEOUS_CTRLER  + "\n";
		if( (aFlagValue & 0x10) > 0 )  retStr += "- " + ADFLAG_SIMULTANEOUS_HOST    + "\n"; 
		if( retStr.equalsIgnoreCase("")) retStr += "- " + ADFLAG_UNKNOWN + "\n";
		
		return retStr;
	}
	
	public static boolean isValidAdvType(int aAdvType)
	{
		if( aAdvType >= 0x0 && aAdvType < 0x22 ) return true;
		if( aAdvType == 0x3D) return true;
		if( aAdvType == 0xFF) return true;		
		return false;
	}

}
