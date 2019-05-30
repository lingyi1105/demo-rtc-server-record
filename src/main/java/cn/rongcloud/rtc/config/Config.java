package cn.rongcloud.rtc.config;

import cn.rongcloud.rtc.model.RecordType;

public class Config {
	
	private static Config instance;
	private int port = 8801;
	@RequiredConfig
	private String appKey;
	@RequiredConfig
	private String secret;
	
	private String recordNodeAddr;
	
	private int recordType = RecordType.AutoRecord.getValue();
	
	private int checkExpiredTimeSec = 120;
	private int channelExpiredTimeSec = 400;

	public static void initialize() throws Exception {
		instance = new Config();
		ConfigUitl.initLocalConfig(instance, "ServiceSettings.properties");
		ConfigUitl.checkRequiredConfig(instance);
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
