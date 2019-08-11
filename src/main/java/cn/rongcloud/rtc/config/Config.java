package cn.rongcloud.rtc.config;

import cn.rongcloud.rtc.model.RecordType;

public class Config {
	
	private static Config instance;
	private int port = 8801;
	@RequiredConfig
	private String appKey;
	@RequiredConfig
	private String secret;
	private boolean appFilter = true;
	
	private String recordNodeAddr;
	
	private int recordType = RecordType.AutoRecord.getValue();
	
	private int checkExpiredTimeSec = 60;
	private int channelExpiredTimeSec = 150;

	public static void initialize() throws Exception {
		instance = new Config();
		ConfigUitl.initLocalConfig(instance, "ServiceSettings.properties");
		if (instance.isAppFilter()) {
			ConfigUitl.checkRequiredConfig(instance);
		}
	}

	public static Config instance() {
		return instance;
	}
	
	public String getRecordNodeAddr() {
		return recordNodeAddr;
	}
	
	public int getPort() {
		return port;
	}

	public String getAppKey() {
		return appKey;
	}

	public String getSecret() {
		return secret;
	}
	
	public boolean isAppFilter() {
		return appFilter;
	}

	public int getRecordType() {
		return recordType;
	}
	
	public int getCheckExpiredTimeSec() {
		return checkExpiredTimeSec;
	}

	public int getChannelExpiredTimeSec() {
		return channelExpiredTimeSec;
	}
	
	public boolean bIsCustomRecord() {
		return this.recordType == RecordType.CustomRecord.getValue();
	}

}
