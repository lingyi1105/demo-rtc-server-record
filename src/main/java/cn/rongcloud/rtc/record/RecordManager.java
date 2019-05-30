package cn.rongcloud.rtc.record;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import cn.rongcloud.rtc.channel.ChannelEventListener;
import cn.rongcloud.rtc.config.Config;
import cn.rongcloud.rtc.model.ChannelInfo;
import cn.rongcloud.rtc.model.ChannelStateNotify;
import cn.rongcloud.rtc.model.EventType;
import cn.rongcloud.rtc.model.RecordMember;
import cn.rongcloud.rtc.util.HttpClientUtil;

public class RecordManager implements ChannelEventListener {
	private static Logger logger = LoggerFactory.getLogger(RecordManager.class);
	private static RecordManager instance = new RecordManager();
	private static Gson gson = new Gson();
	private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

	private Map<String, ChannelInfo> channelInfoMap = new ConcurrentHashMap<>();

	private List<String> customRecordChannel = new CopyOnWriteArrayList<>();

	private Map<String, String> userChannelIdMap = new ConcurrentHashMap<>();

	private RecordManager() {
	}

	public static RecordManager instance() {
		return instance;
	}

	@Override
	public void onChannelSync(ChannelStateNotify notify) {
		ChannelInfo info = getChannelInfoFromMap(notify.getAppKey(), notify.getChannelId());
		info.setLastTimestamp(System.currentTimeMillis());
	}

	@Override
	public void onChannelCreated(ChannelStateNotify notify) {
		channelInfoMap.computeIfAbsent(notify.getChannelId(),
				k -> new ChannelInfo(notify.getAppKey(), notify.getChannelId()));
	}

	@Override
	public void onChannelDestroyed(ChannelStateNotify notify) {
		boolean needNotifyToRecord = true;
		ChannelInfo info = channelInfoMap.remove(notify.getChannelId());
		if (info == null) {
			return;
		}

		if (Config.instance().bIsCustomRecord()) {
			removeUserByChannelDestroy(info);
			if (customRecordChannel.contains(notify.getChannelId())) {
				customRecordChannel.remove(notify.getChannelId());
			} else {
				needNotifyToRecord = false;
			}
		}

		if (needNotifyToRecord) {
			HttpClientUtil.sendNotifyToRecordNode(gson.toJson(notify));
		}
	}

	@Override
	public void onMemberJoined(ChannelStateNotify notify) {
		if (Config.instance().bIsCustomRecord()) {
			userChannelIdMap.computeIfAbsent(notify.getUserId(), k -> notify.getChannelId());
		}
		ChannelInfo info = getChannelInfoFromMap(notify.getAppKey(), notify.getChannelId());
		info.setLastTimestamp(System.currentTimeMillis());
	}

	@Override
	public void onMemberLeft(ChannelStateNotify notify) {
		boolean needNotifyToRecord = true;
		ChannelInfo info = getChannelInfoFromMap(notify.getAppKey(), notify.getChannelId());
		RecordMember member = info.removeMemberByUserId(notify.getUserId());
		if (member == null || member.getUris().isEmpty()) {
			needNotifyToRecord = false;
		}
		// 自定义开始结束录像特殊处理
		if (Config.instance().bIsCustomRecord()) {
			if (!customRecordChannel.contains(notify.getChannelId())) {
				needNotifyToRecord = false;
			}
			if (userChannelIdMap.containsKey(notify.getUserId())) {
				userChannelIdMap.remove(notify.getUserId());
			}
		}

		if (needNotifyToRecord) {
			ChannelInfo notifyInfo = createChannelInfoForRecord(notify, info);
			HttpClientUtil.sendNotifyToRecordNode(gson.toJson(notifyInfo));
		}

		info.setLastTimestamp(System.currentTimeMillis());
	}

	@Override
	public void onMemberKicked(ChannelStateNotify notify) {
	}

	@Override
	public void onMemberChangeType(ChannelStateNotify notify) {
	}

	@Override
	public void onPublishResource(ChannelStateNotify notify) {
		ChannelInfo info = getChannelInfoFromMap(notify.getAppKey(), notify.getChannelId());
		info.udpateUserResourceURI(notify);
		boolean needNotifyToRecord = true;
		if (Config.instance().bIsCustomRecord() && !customRecordChannel.contains(notify.getChannelId()) ) {
			needNotifyToRecord = false;
		}
		if (needNotifyToRecord) {
			ChannelInfo notifyInfo = createChannelInfoForRecord(notify, info);
			HttpClientUtil.sendNotifyToRecordNode(gson.toJson(notifyInfo));
		}
		info.setLastTimestamp(System.currentTimeMillis());
	}

	@Override
	public void onUnpublishResource(ChannelStateNotify notify) {
		ChannelInfo info = getChannelInfoFromMap(notify.getAppKey(), notify.getChannelId());
		info.udpateUserResourceURI(notify);
		boolean needNotifyToRecord = true;
		if (Config.instance().bIsCustomRecord() && !customRecordChannel.contains(notify.getChannelId()) ) {
			needNotifyToRecord = false;
		}

		if (needNotifyToRecord) {
			ChannelInfo notifyInfo = createChannelInfoForRecord(notify, info);
			HttpClientUtil.sendNotifyToRecordNode(gson.toJson(notifyInfo));
		}
		info.setLastTimestamp(System.currentTimeMillis());
	}

	@Override
	public void onResourceStateChanged(ChannelStateNotify notify) {
	}

	private ChannelInfo getChannelInfoFromMap(String appKey, String channelId) {
		return channelInfoMap.computeIfAbsent(channelId, k -> new ChannelInfo(appKey, channelId));
	}

	private ChannelInfo createChannelInfoForRecord(ChannelStateNotify notify, ChannelInfo info) {
		ChannelInfo notifyInfo = new ChannelInfo(info.getAppKey(), info.getChannelId());
		notifyInfo.setEvent(notify.getEvent());
		notifyInfo.setToken(notify.getToken());
		notifyInfo.setUserId(notify.getUserId());
		if (!info.getMembers().isEmpty()) {
			notifyInfo.setMembers(info.getMembers());
		}
		return notifyInfo;
	}

	// 两分钟检测一次channel是否过期。如果过期，需要从map中移除channel，并通知record
	public void startCheckChannelExpired() {
		executorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				checkChannelExpired();
			}
		}, Config.instance().getCheckExpiredTimeSec(), Config.instance().getCheckExpiredTimeSec(), TimeUnit.SECONDS);
	}

	public void checkChannelExpired() {
		long nowTime = System.currentTimeMillis();
		for (ChannelInfo info : channelInfoMap.values()) {
			if ((nowTime - info.getLastTimestamp()) > Config.instance().getChannelExpiredTimeSec() * 1000) {
				boolean needNotifyToRecord = true;
				channelInfoMap.remove(info.getChannelId());
				
				if (Config.instance().bIsCustomRecord()) {
					removeUserByChannelDestroy(info);
					if (customRecordChannel.contains(info.getChannelId())) {
						customRecordChannel.remove(info.getChannelId());
					} else {
						needNotifyToRecord = false;
					}
				}

				if (needNotifyToRecord) {
					ChannelInfo notifyInfo = new ChannelInfo(info.getAppKey(), info.getChannelId());
					notifyInfo.setEvent(EventType.CHANNEL_DESTROYED);
					notifyInfo.setToken(info.getToken());
					HttpClientUtil.sendNotifyToRecordNode(gson.toJson(notifyInfo));
				}
			}
		}
	}
	
	private void removeUserByChannelDestroy(ChannelInfo info) {
		if (info.getMembers().isEmpty()) {
			return;
		}
		for (RecordMember member : info.getMembers()) {
			if (userChannelIdMap.containsKey(member.getUserId())) {
				userChannelIdMap.remove(member.getUserId());
			}
		}
	}

	public ChannelInfo getChannelInfoByUidOrCid(String uid, String cid) {
		if (channelInfoMap.containsKey(cid)) {
			return channelInfoMap.get(cid);
		}
		if (userChannelIdMap.containsKey(uid)) {
			String channelId = userChannelIdMap.get(uid);
			if (channelInfoMap.containsKey(channelId)) {
				return channelInfoMap.get(channelId);
			}
		}
		
		return null;
	}

	public boolean startRecord(String userId, ChannelInfo info, String fileName) {
		try {
			if (customRecordChannel.contains(info.getChannelId())) {
				return true;
			}
			ChannelInfo notifyInfo = new ChannelInfo(info.getAppKey(), info.getChannelId());
			notifyInfo.setEvent(EventType.PUBLISH_RESOURCE);
			notifyInfo.setUserId(userId);
			notifyInfo.setToken(info.getToken());
			notifyInfo.setMembers(info.getMembers());
			HttpClientUtil.sendNotifyToRecordNode(gson.toJson(notifyInfo));
			customRecordChannel.add(info.getChannelId());
			return true;
		} catch (Exception e) {
			logger.error("server start record failed!");
			return false;
		}
	}

	public boolean stopRecord(String userId, String cid) {
		try {
			ChannelInfo info = getChannelInfoByUidOrCid(userId, cid);
			if (info == null) {
				return true;
			}
			if (!customRecordChannel.contains(info.getChannelId())) {
				return true;
			}
			ChannelInfo notifyInfo = new ChannelInfo(info.getAppKey(), info.getChannelId());
			notifyInfo.setEvent(EventType.CHANNEL_DESTROYED);
			notifyInfo.setUserId(userId);
			notifyInfo.setToken(info.getToken());
			HttpClientUtil.sendNotifyToRecordNode(gson.toJson(notifyInfo));
			customRecordChannel.remove(info.getChannelId());
			return true;
		} catch (Exception e) {
			logger.error("server end record failed!");
			return false;
		}
	}
}
