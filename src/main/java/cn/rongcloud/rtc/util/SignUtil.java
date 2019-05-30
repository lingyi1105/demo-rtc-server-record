package cn.rongcloud.rtc.util;

import java.security.MessageDigest;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.rongcloud.rtc.config.Config;

public class SignUtil {
	private static Logger logger = LoggerFactory.getLogger(SignUtil.class);
	public static boolean checkSign(HttpServletRequest request){
		
		String nonce = request.getHeader("nonce");
        String timestamp = request.getHeader("timestamp");
        String signature = request.getHeader("signature");
        logger.info("===========nonce:{}, timestamp:{},sign:{}", nonce,timestamp,signature);
        String sign = hexSHA1(Config.instance().getSecret(), nonce, timestamp);

        if(sign.equalsIgnoreCase(signature)){
        	return true;
        }
        
        return false;
	}
	
	public static String hexSHA1(String secret, String nonce, String timestamp) {
		StringBuilder sb = new StringBuilder();
		sb.append(secret).append(nonce).append(timestamp);
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(sb.toString().getBytes("utf-8"));
			byte[] digest = md.digest();
			return byteToHexString(digest);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static String byteToHexString(byte[] bytes) {
		return String.valueOf(Hex.encodeHex(bytes));
	}
}
