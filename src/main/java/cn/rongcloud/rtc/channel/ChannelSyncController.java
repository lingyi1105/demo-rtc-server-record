package cn.rongcloud.rtc.channel;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import cn.rongcloud.rtc.config.Config;
import cn.rongcloud.rtc.model.ChannelStateNotify;
import cn.rongcloud.rtc.model.http.ResponseEntity;
import cn.rongcloud.rtc.util.SignUtil;

@CrossOrigin
@Controller
public class ChannelSyncController {

	private static Logger logger = LoggerFactory.getLogger(ChannelSyncController.class);

	private static Gson gson = new Gson();

	@RequestMapping("/recv")
	@ResponseBody
	public ResponseEntity recvChannelNotify(@RequestBody String body, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String appKey = request.getHeader("appKey");
		if (!Config.instance().getAppKey().equals(appKey)) {
			logger.warn("channel sysn message appKey is error {}", appKey);
			response.setStatus(403);
			return new ResponseEntity(403, "appKey is error");
		}
		logger.info(body);
		//校验签名
		if (!SignUtil.checkSign(request)) {
			logger.warn("check sign failed!");
			response.setStatus(403);
			return new ResponseEntity(403, "check sign failed!");
		}
		ChannelStateNotify notify = gson.fromJson(body, ChannelStateNotify.class);
		
		ChannelManager.instance().onChannelNotify(notify);

		return new ResponseEntity(200, "ok");
	}
}
