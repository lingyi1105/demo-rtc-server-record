package cn.rongcloud.rtc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 通知事件的object。ex：
 {
    "appKey":"XXXXXXX",
    "channelId":"xxxx",
    "event":1,
    "userId":"xxxx",
    "timestamp":2222222,
    "token":"xxxx",
    "extra":"xxxx",
    "members": {
           [
                "userId": "userid1",
                "memberType": 0,
                "data":{
                    “uris”: [{
                     “tag”: “XXXX”,
                     “streamId”: “MediaStream Id“,
                     “type“: 0,
                     “state“: 1,
                     “uri“: “XXXXXX”
                     }{
                     “tag”: “XXXX”,
                     “streamId”: “MediaStream Id“,
                     “type“: 1,
                     “state“: 1,
                     “uri“: “XXXXXX”
                     }]  
                }
                
                                 ]，
           [
                "userId": "userid2",
                "memberType": 1,
                "data":{
                     “uris”: [{
                     “tag”: “XXXX”,
                     “streamId”: “MediaStream Id“,
                     “type“: 0,
                     “state“: 1,
                     “uri“: “XXXXXX”
                     }]
                }
               
             ]
    }
}
 */
public class ChannelStateNotify {

	private String appKey;
	private String channelId;
	private int event;
	private String userId;
	private long timestamp;
	private String token;
    private String extra;
    private List<Member> members = new ArrayList<Member>();
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
	public int getEvent() {
		return event;
	}
	public void setEvent(int event) {
		this.event = event;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getExtra() {
		return extra;
	}
	public void setExtra(String extra) {
		this.extra = extra;
	}
	public List<Member> getMembers() {
		return members;
	}
	public void setMembers(List<Member> members) {
		this.members = members;
	}
	
	public Member getMemberByUserId (String userId) {
		for (Member member : this.members) {
			if (userId.equals(member.getUserId())) {
				return member;
			}
		}
		return null;
	}
	
}
