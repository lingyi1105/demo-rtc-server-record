package cn.rongcloud.rtc;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import cn.rongcloud.rtc.channel.ChannelManager;
import cn.rongcloud.rtc.config.Config;
import cn.rongcloud.rtc.record.RecordManager;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class Entry {
	
	public static Logger logger = LoggerFactory.getLogger(Entry.class);

	public static void main(String[] args) throws Exception {
		
		PropertyConfigurator.configure("log4j.properties");
		//配置信息初始化
		Config.initialize();
		System.setProperty("server.port", Config.instance().getPort() + "");
		SpringApplication.run(Entry.class, args);
		
		//监听会场状态  并在合适的时候通知录像节点进行录像
		ChannelManager.instance().addChannelEventListener(RecordManager.instance());
		//检测房间超时机制，超时的话做清理
		RecordManager.instance().startCheckChannelExpired();
		logger.info("start channel state server successful !");
	}
}
