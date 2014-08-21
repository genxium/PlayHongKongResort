package utilities;

import java.security.MessageDigest;

public class Converter{
	public static String md5(String input){
		try{
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(input.getBytes());
			byte[] digest = md.digest();
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < digest.length; ++i) {
				buffer.append(Integer.toHexString((digest[i] & 0xFF) | 0x100).substring(1,3));
			}
			return buffer.toString();
		} catch(Exception e){
			e.printStackTrace();
			return null;
		}

	}

	public static String generateToken(String username, String password){
		java.util.Date date= new java.util.Date();
		String dateStr=date.toString();
		String base=username+dateStr+password;
		return md5(base);
	}

    public static Integer toInteger(Object obj) {
        if(obj instanceof Integer) return (Integer)obj;
        else if(obj instanceof String) return Integer.valueOf((String)obj);
        else return null;
    }
}