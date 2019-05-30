package cn.rongcloud.rtc.model.http;

public class ResponseEntity {
	public static final int CODE_OK = 200;
	
	private int code;
	private String msg;
	
	public ResponseEntity(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public int getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}

	public ResponseEntity setCode(int code) {
		this.code = code;
		return this;
	}

	public ResponseEntity setMsg(String msg) {
		this.msg = msg;
		return this;
	}
}
