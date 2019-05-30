package cn.rongcloud.rtc.model.http;

public enum ResultCode {
	
	OK(10000),
	TOKEN_ERROR(41001),
	JSON_FORMAT_ERROR(41002),
	SDP_FROMAT_ERROR(41003),
	UDP_CHANNEL_ERROR(41004);
	
//	CONNECTION_NOT_EXIT(41005);
	
	private int code;
	
	ResultCode(int code){
		this.code = code;
	}
	
	public int code(){
		return code;
	}
}
