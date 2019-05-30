package cn.rongcloud.rtc.model;

import com.alibaba.fastjson.JSONObject;

public class Member {

	private String userId;
    private JSONObject data;
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public JSONObject getData(){
		return data;
	}
	public void setData(JSONObject data){
		this.data = data;
	}
}
