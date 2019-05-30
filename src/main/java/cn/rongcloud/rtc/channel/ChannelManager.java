package cn.rongcloud.rtc.channel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.rongcloud.rtc.model.ChannelStateNotify;
import cn.rongcloud.rtc.model.EventType;

public class ChannelManager {

	private static Logger logger = LoggerFactory.getLogger(ChannelManager.class);

	private static ChannelManager instance = new ChannelManager();

	private List<ChannelEventListener> listeners = new ArrayList<>();

	private ChannelManager() {
	}

	public static ChannelManager instance() {
		return instance;
	}

	public void onChannelNotify(ChannelStateNotify notify) throws IOException {

		for (ChannelEventListener listener : listeners) {
			switch (notify.getEvent()) {
			case EventType.CHANNEL_SYNC:
				listener.onChannelSync(notify);
				break;
			case EventType.CHANNEL_CREATED:
				listener.onChannelCreated(notify);
				break;
			case EventType.CHANNEL_DESTROYED:
				listener.onChannelDestroyed(notify);
				break;
			case EventType.MEMBER_JOINED:
				listener.onMemberJoined(notify);
				break;
			case EventType.MEMBER_LEFT:
				listener.onMemberLeft(notify);
				break;
			case EventType.MEMBER_KICKED:
				listener.onMemberKicked(notify);
				break;
			case EventType.MEMBER_TYPE_UPDATED:
				listener.onMemberChangeType(notify);
				break;
			case EventType.PUBLISH_RESOURCE:
				listener.onPublishResource(notify);
				break;
			case EventType.UNPUBLISH_RESOURCE:
				listener.onUnpublishResource(notify);
				break;
			case EventType.RESOURCE_STATE_CHANGED:
				listener.onResourceStateChanged(notify);
				break;
			default:
				logger.error("unknow event type {}", notify.getEvent());
				break;
			}
		}
	}
	public void addChannelEventListener(ChannelEventListener listener) {
		listeners.add(listener);
	}

	public void removeChannelEventListener(ChannelEventListener listener) {
		listeners.remove(listener);
	}



//	private static String getResult(CloseableHttpResponse response) throws Exception {
//		String line = null;
//		StringBuilder sb = new StringBuilder();
//		HttpEntity entity = response.getEntity();
//		InputStream content = entity.getContent();
//		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(content, "utf-8"));
//		while ((line = bufferedReader.readLine()) != null)
//			sb.append(line);
//
//		return sb.toString();
//	}

//	public Collection<Channel> getChannelList() {
//		Collection<Channel> values = channelMap.values();
//		return values;
//	}
//
//	public String getChannelInfo() {
//		Collection<Channel> channelList = getChannelList();
//		String s = gson.toJson(channelList);
//		return s;
//	}
//	
//	public Channel getChannel(String cid) {
//		return channelMap.get(cid);
//	}
}
