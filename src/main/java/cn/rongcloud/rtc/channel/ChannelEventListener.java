package cn.rongcloud.rtc.channel;

import cn.rongcloud.rtc.model.ChannelStateNotify;

public interface ChannelEventListener {

	void onChannelSync(ChannelStateNotify notify);
	
	void onChannelCreated(ChannelStateNotify notify);

	void onChannelDestroyed(ChannelStateNotify notify);

	void onMemberJoined(ChannelStateNotify notify);

	void onMemberLeft(ChannelStateNotify notify);
	
	void onMemberKicked(ChannelStateNotify notify);
	
	void onMemberChangeType(ChannelStateNotify notify);

	void onPublishResource(ChannelStateNotify notify);

	void onUnpublishResource(ChannelStateNotify notify);

	void onResourceStateChanged(ChannelStateNotify notify);
}
