package cn.rongcloud.rtc.record;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import cn.rongcloud.rtc.config.Config;
import cn.rongcloud.rtc.model.ChannelInfo;
import cn.rongcloud.rtc.model.RecordType;
import cn.rongcloud.rtc.model.http.ResponseEntity;


@RestController
public class CustomRecordController {

	private static Gson gson = new Gson();

	@RequestMapping(value = "/customrecord/start", method = RequestMethod.POST)
	public void startCustomRecord(String userId, String channelId, String fileName, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (Config.instance().getRecordType() != RecordType.CustomRecord.getValue()) {
			response.sendError(403, "server does not support this record type!");
			return;
		}
		if ((userId == null || userId.length() == 0) && (channelId == null || channelId.length() == 0)) {
			response.sendError(405, "userId and channelId is all empty, please check it!");
			return;
		}
		ChannelInfo channelInfo = RecordManager.instance().getChannelInfoByUidOrCid(userId,channelId);
		if (channelInfo == null) {
			response.sendError(404, "the channel is not exist!");
			return;
		}

		boolean bIsSuc = RecordManager.instance().startRecord(userId, channelInfo, fileName);
		if (bIsSuc) {
			sendResponse(200, "OK", response);
		} else {
			response.sendError(500, "start record failed!");
		}
	}

	@RequestMapping(value = "/customrecord/stop", method = RequestMethod.POST)
	public void stopCustomRecord(String userId, String channelId, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (Config.instance().getRecordType() != RecordType.CustomRecord.getValue()) {
			response.sendError(403, "server does not support this record type!");
			return;
		}
		if ((userId == null || userId.length() == 0) && (channelId == null || channelId.length() == 0)) {
			response.sendError(405, "uid and cid is all empty, please check it!");
			return;
		}

		boolean bIsSuc = RecordManager.instance().stopRecord(userId,channelId);
		if (bIsSuc) {
			sendResponse(200, "OK", response);
		} else {
			response.sendError(500, "start record failed!");
		}
	}

	private void sendResponse(int code, String msg, HttpServletResponse response) throws IOException  {
		response.setContentType("application/json");
		ResponseEntity entity = new ResponseEntity(code, msg);
		response.getWriter().write(gson.toJson(entity));
		response.getWriter().flush();
	}
}
