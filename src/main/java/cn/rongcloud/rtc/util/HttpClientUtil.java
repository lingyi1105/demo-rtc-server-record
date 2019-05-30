package cn.rongcloud.rtc.util;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.rongcloud.rtc.config.Config;
import cn.rongcloud.rtc.model.http.ResponseEntity;

public class HttpClientUtil {
	private static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
	public static void sendNotifyToRecordNode(String content) {
		String url = Config.instance().getRecordNodeAddr();
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		HttpPost post = new HttpPost(url);
		try {
			post.addHeader("Content-Type", "application/json;charset=UTF-8");
			post.setEntity(new StringEntity(content));
			logger.info("sent notify to record, content:{}", content);
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000).build();
			post.setConfig(requestConfig);
			response = httpClient.execute(post);
			if (response.getStatusLine().getStatusCode() != ResponseEntity.CODE_OK) {
				logger.error("send notity to record not OK , code:{}, content:{}",
						response.getStatusLine().getStatusCode(), content);
			} 
		} catch (Exception e) {
			logger.error("send http req to record node exception,content:{},exception:{}", content, e);
		} finally {
			try {
				response.close();
				post.abort();
				httpClient.close();
			} catch (Exception e) {
				e.getStackTrace();
			}
		}
	}
}
