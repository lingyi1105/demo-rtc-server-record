package cn.rongcloud.rtc.model;

import java.util.ArrayList;
import java.util.List;

public class RecordMember {
	private String userId;
	private List<URI> uris = new ArrayList<>();
	public RecordMember(String userId) {
		this.userId = userId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public List<URI> getUris() {
		return uris;
	}
	public void setUris(List<URI> uris) {
		this.uris = uris;
	}
}
