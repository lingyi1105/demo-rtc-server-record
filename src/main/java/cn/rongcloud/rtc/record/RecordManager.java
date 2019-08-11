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

	private static final String SEPARATOR = ";;;";
	// key: appKey + SEPARATOR + sessionId
	private Map<String, ChannelInfo> channelInfoMap = new ConcurrentHashMap<>();

	// key: appKey + SEPARATOR + sessionId
	private List<String> customRecordings = new CopyOnWriteArrayList<>();

	private Map<String, String> userId2ChannelKey = new ConcurrentHashMap<>();
	private Map<String, String> channelId2ChannelKey = new ConcurrentHashMap<>();

	private RecordManager() {
	}

	public static RecordManager instance() {
		return instance;
	}

	@Override
	public void onChannelSync(ChannelStateNotify notify) {
		ChannelInfo info = getChannelInfoFromMap(notify.getAppKey(), notify.getChannelId(), notify.getSessionId());
		info.setLastTimestamp(System.currentTimeMillis());
	}

	@Override
	public void onChannelCreated(ChannelStateNotify notify) {
		channelInfoMap.computeIfAbsent(getChannelKey(notify),
				k -> new ChannelInfo(notify.getAppKey(), notify.getChannelId(), notify.getSessionId()));
		if (Config.instance().bIsCustomRecord()) {
			channelId2ChannelKey.computeIfAbsent(notify.getAppKey() + SEPARATOR + notify.getChannelId(),
					k -> getChannelKey(notify));
		}
		HttpClientUtil.sendNotifyToRecordNode(gson.toJson(notify));
	}

	@Override
	public void onChannelDestroyed(ChannelStateNotify notify) {
		boolean needNotifyToRecord = true;
		ChannelInfo info = channelInfoMap.remove(getChannelKey(notify));
		if (info == null) {
			return;
		}

		if (Config.instance().bIsCustomRecord()) {
			removeUserByChannelDestroy(info);
			if (customRecordings.contains(getChannelKey(notify))) {
				customRecordings.remove(getChannelKey(notify));
			} else {
				needNotifyToRecord = false;
			}
			channelId2ChannelKey.remove(notify.getAppKey() + SEPARATOR + notify.getChannelId());
		}

		if (needNotifyToRecord) {
			HttpClientUtil.sendNotifyToRecordNode(gson.toJson(notify));
		}
	}

	@Override
	public void onMemberJoined(ChannelStateNotify notify) {
		if (Config.instance().bIsCustomRecord()) {
			userId2ChannelKey.computeIfAbsent(notify.getAppKey() + SEPARATOR + notify.getUserId(),
					k -> getChannelKey(notify));
		}
		ChannelInfo info = getChannelInfoFromMap(notify.getAppKey(), notify.getChannelId(), notify.getSessionId());
		info.setLastTimestamp(System.currentTimeMillis());
	}

	@Override
	public void onMemberLeft(ChannelStateNotify notify) {
		boolean needNotifyToRecord = true;
		ChannelInfo info = getChannelInfoFromMap(notify.getAppKey(), notify.getChannelId(), notify.getSessionId());
		RecordMember member = info.removeMemberByUserId(notify.getUserId());
		if (member == null || member.getUris().isEmpty()) {
			needNotifyToRecord = false;
		}
		// 自定义开始结束录像特殊处理
		if (Config.instance().bIsCustomRecord()) {
			if (!customRecordings.contains(getChannelKey(notify))) {
				needNotifyToRecord = false;
			}
			userId2ChannelKey.remove(notify.getAppKey() + SEPARATOR + notify.getUserId());
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
		ChannelInfo info = getChannelInfoFromMap(notify.getAppKey(), notify.getChannelId(), notify.getSessionId());
		info.udpateUserResourceURI(notify);
		boolean needNotifyToRecord = true;
		if (Config.instance().bIsCustomRecord() && !customRecordings.contains(getChannelKey(notify))) {
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
		ChannelInfo info = getChannelInfoFromMap(notify.getAppKey(), notify.getChannelId(), notify.getSessionId());
		info.udpateUserResourceURI(notify);
		boolean needNotifyToRecord = true;
		if (Config.instance().bIsCustomRecord() && !customRecordings.contains(getChannelKey(notify))) {
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

	private String getChannelKey(ChannelStateNotify notify) {
		return notify.getAppKey() + SEPARATOR + notify.getSessionId();
	}

	private String getChannelKey(ChannelInfo info) {
		return info.getAppKey() + SEPARATOR + info.getSessionId();
	}

	private String getChannelKey(String appKey, String sessionId) {
		return appKey + SEPARATOR + sessionId;
	}

	private ChannelInfo getChannelInfoFromMap(String appKey, String channelId, String sessionId) {
		return channelInfoMap.computeIfAbsent(getChannelKey(appKey, sessionId),
				k -> new ChannelInfo(appKey, channelId, sessionId));
	}

	private ChannelInfo createChannelInfoForRecord(ChannelStateNotify notify, ChannelInfo info) {
		ChannelInfo notifyInfo = new ChannelInfo(info.getAppKey(), info.getChannelId(), info.getSessionId());
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
				channelInfoMap.remove(getChannelKey(info));
				
				if (Config.instance().bIsCustomRecord()) {
					removeUserByChannelDestroy(info);
					if (customRecordings.contains(getChannelKey(info))) {
						customRecordings.remove(getChannelKey(info));
					} else {
						needNotifyToRecord = false;
					}
					channelId2ChannelKey.remove(info.getAppKey() + SEPARATOR + info.getChannelId());
				}

				if (needNotifyToRecord) {
					ChannelInfo notifyInfo = new ChannelInfo(info.getAppKey(), info.getChannelId(), info.getSessionId());
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
			userId2ChannelKey.remove(info.getAppKey() + SEPARATOR + member.getUserId());
		}
	}

	public ChannelInfo getChannelInfo(String appKey, String sessionId) {
		return channelInfoMap.get(getChannelKey(appKey, sessionId));
	}

	public ChannelInfo getChannelInfoByUidOrCid(String appKey, String uid, String cid) {
		String channelKey = null;
		if (channelId2ChannelKey.containsKey(appKey + SEPARATOR + cid)) {
			channelKey = channelId2ChannelKey.get(appKey + SEPARATOR + cid);
		}
		if (userId2ChannelKey.containsKey(appKey + SEPARATOR + uid)) {
			channelKey = userId2ChannelKey.get(appKey + SEPARATOR + uid);
		}
		if (channelKey != null && channelInfoMap.containsKey(channelKey)) {
			return channelInfoMap.get(channelKey);
		}
		
		return null;
	}

	public boolean startRecord(ChannelInfo info, String userId, String fileName) {
		try {
			if (customRecordings.contains(getChannelKey(info))) {
				return true;
			}
			ChannelInfo notifyInfo = new ChannelInfo(info.getAppKey(), info.getChannelId(), info.getSessionId());
			notifyInfo.setEvent(EventType.PUBLISH_RESOURCE);
			notifyInfo.setUserId(userId);
			notifyInfo.setToken(info.getToken());
			notifyInfo.setMembers(info.getMembers());
			HttpClientUtil.sendNotifyToRecordNode(gson.toJson(notifyInfo));
			customRecordings.add(getChannelKey(info));
			return true;
		} catch (Exception e) {
			logger.error("server start record failed!");
			return false;
		}
	}

	public boolean stopRecord(ChannelInfo info, String userId) {
		try {
			if (!customRecordings.contains(getChannelKey(info))) {
				return true;
			}
			ChannelInfo notifyInfo = new ChannelInfo(info.getAppKey(), info.getChannelId(), info.getSessionId());
			notifyInfo.setEvent(EventType.CHANNEL_DESTROYED);
			notifyInfo.setUserId(userId);
			notifyInfo.setToken(info.getToken());
			HttpClientUtil.sendNotifyToRecordNode(gson.toJson(notifyInfo));
			customRecordings.remove(getChannelKey(info));
			return true;
		} catch (Exception e) {
			logger.error("server end record failed!");
			return false;
		}
	}
}
