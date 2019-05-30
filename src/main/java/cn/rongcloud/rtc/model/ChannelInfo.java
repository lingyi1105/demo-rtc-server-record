package cn.rongcloud.rtc.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ChannelInfo {
	private String appKey;
	private String channelId;
	private String extra;
	private String userId;
	private String token;
	private int event;
	private List<RecordMember> members = new CopyOnWriteArrayList<>();
	private long lastTimestamp;
	
	public ChannelInfo(String appKey, String channelId) {
		lastTimestamp = System.currentTimeMillis();
		this.appKey = appKey;
		this.channelId = channelId;
	}

	public String getAppKey() {
		return appKey;
	} 
	
	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public List<RecordMember> getMembers() {
		return members;
	}

	public void setMembers(List<RecordMember> members) {
		this.members = members;
	}
	
	public int getEvent() {
		return event;
	}

	public void setEvent(int event) {
		this.event = event;
	}

	public long getLastTimestamp() {
		return lastTimestamp;
	}

	public void setLastTimestamp(long lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public RecordMember getMemberByUserId(String userId) {
		for (RecordMember member : this.members) {
			if (userId.equals(member.getUserId())) {
				return member;
			}
		}
		return null;
	}
	
	public synchronized RecordMember removeMemberByUserId(String userId) {
		RecordMember member = getMemberByUserId(userId);
		if (member == null) {
			return null;
		}
		this.members.remove(member);
		return member;
	}
	
	public synchronized void udpateUserResourceURI(ChannelStateNotify notify) {
		RecordMember recordMem = getMemberByUserId(notify.getUserId());
		if (recordMem == null) {
			recordMem = new RecordMember(notify.getUserId());
			this.members.add(recordMem);
		}
		Member member = notify.getMemberByUserId(notify.getUserId());
		if (member == null || member.getData() == null) {
			return;
		}
		String uris = member.getData().getString("uris");
	    if (uris == null || uris.length() == 0) {
	    	recordMem.setUris(null);
	    	return;
	    }
		
	    List<URI> lstURI = getURIsFromJson(uris);
	    recordMem.setUris(lstURI);
	}
	
	private List<URI> getURIsFromJson (String uris) {
		List<URI> lstURI = new ArrayList<>();
		JsonArray array = new JsonArray();
		JsonElement elementValue = new JsonParser().parse(uris);
		if (elementValue.isJsonArray()){
			array = elementValue.getAsJsonArray();
		} else {
			array.add(elementValue);
		}
		
		for (JsonElement element : array) {
			URI item = new Gson().fromJson(element, URI.class);
			lstURI.add(item);
		}
		
		return lstURI;
	}
}
