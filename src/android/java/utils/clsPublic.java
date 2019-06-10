package utils;

public class clsPublic {
	
	/**  
	 * bytes�ַ���ת��ΪByteֵ
	 * @return byte[]  
	 */ 
	public static byte[] HexString2Bytes(String src)    
	{    
	    int m=0,n=0;    
	    int l=src.length()/2;    
	    System.out.println(l);    
	    byte[] ret = new byte[l];    
	    for (int i = 0; i < l; i++)    
	    {    
	        m=i*2+1;    
	        n=m+1;    
	        ret[i] = Byte.decode("0x" + src.substring(i*2, m) + src.substring(m,n));    
	    }    
	    return ret;    
	}    
	
	/**  
	 * bytesת����ʮ�������ַ���
	 * @return String ÿ��Byteֵ֮��ո�ָ�  
	 */    
	public static String Bytes2HexString(byte[] b)    
	{    
	    String stmp="";    
	    StringBuilder sb = new StringBuilder("");    
	    for (int n=0;n<b.length;n++)    
	    {    
	        stmp = Integer.toHexString(b[n] & 0xFF);    
	        sb.append((stmp.length()==1)? "0"+stmp : stmp);    
	        sb.append(" ");    
	    }    
	    return sb.toString().toUpperCase().trim();    
	}    
	
	/**  
	 * String���ַ���ת����unicode��String
	 * @return String ÿ��unicode֮���޷ָ���  
	 * @throws Exception  
	 */    
	public static String strToUnicode(String strText)    
	    throws Exception    
	{    
	    char c;    
	    StringBuilder str = new StringBuilder();    
	    int intAsc;    
	    String strHex;    
	    for (int i = 0; i < strText.length(); i++)    
	    {    
	        c = strText.charAt(i);    
	        intAsc = (int) c;    
	        strHex = Integer.toHexString(intAsc);    
	        if (intAsc > 128)    
	            str.append("\\u" + strHex);    
	        else // ��λ��ǰ�油00    
	            str.append("\\u00" + strHex);    
	    }    
	    return str.toString();    
	}    
	    
	/**  
	 * unicode��Stringת����String���ַ���
	 * @return String ȫ���ַ���  
	 */    
	public static String unicodeToString(String hex)    
	{    
	    int t = hex.length() / 6;    
	    StringBuilder str = new StringBuilder();    
	    for (int i = 0; i < t; i++)    
	    {    
	        String s = hex.substring(i * 6, (i + 1) * 6);    
	        // ��λ��Ҫ����00��ת    
	        String s1 = s.substring(2, 4) + "00";    
	        // ��λֱ��ת    
	        String s2 = s.substring(4);    
	        // ��16���Ƶ�stringתΪint    
	        int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);    
	        // ��intת��Ϊ�ַ�    
	        char[] chars = Character.toChars(n);    
	        str.append(new String(chars));    
	    }    
	    return str.toString();    
	}   
	
	/**   
	 * �ַ���ת����ʮ�������ַ���
	 * @return String ÿ��Byte֮��ո�ָ�����: [61 6C 6B]  
	 */      
	public static String str2HexStr(String str)    
	{      
	  
	    char[] chars = "0123456789ABCDEF".toCharArray();      
	    StringBuilder sb = new StringBuilder("");    
	    byte[] bs = str.getBytes();      
	    int bit;      
	        
	    for (int i = 0; i < bs.length; i++)    
	    {      
	        bit = (bs[i] & 0x0f0) >> 4;      
	        sb.append(chars[bit]);      
	        bit = bs[i] & 0x0f;      
	        sb.append(chars[bit]);    
	        sb.append(' ');    
	    }      
	    return sb.toString().trim();      
	}    
	    
	/**   
	 * ʮ������ת���ַ���
	 * @return String ��Ӧ���ַ���  
	 */      
	public static String hexStr2Str(String hexStr)    
	{      
	    String str = "0123456789ABCDEF";      
	    char[] hexs = hexStr.toCharArray();      
	    byte[] bytes = new byte[hexStr.length() / 2];      
	    int n;      
	  
	    for (int i = 0; i < bytes.length; i++)    
	    {      
	        n = str.indexOf(hexs[2 * i]) * 16;      
	        n += str.indexOf(hexs[2 * i + 1]);      
	        bytes[i] = (byte) (n & 0xff);      
	    }      
	    return new String(bytes);      
	}
	
	/**   
	 * int ת byte
	 * @return byte
	 */  
	public static byte int2OneByte(int num) {  
        return (byte) (num & 0x000000ff);  
    }
	
	public static int oneByte2Int(byte byteNum) {  
		               //���������int  
		    return byteNum > 0 ? byteNum : (128 + (128 + byteNum));  
		}  
	
	public static int toInt(byte b) {
		return (int) b & 0xFF;
	}

	public static byte toByte(int c) {
		return (byte) (c <= 0x7f ? c : ((c % 0x80) - 0x80));
	}



}
