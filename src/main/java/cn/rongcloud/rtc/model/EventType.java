package cn.rongcloud.rtc.model;

public class  EventType {

	//整个会场状态同步
	public static final int CHANNEL_SYNC = 1;
	//会场创建
	public static final int CHANNEL_CREATED = 2;
	//会场销毁
	public static final int CHANNEL_DESTROYED = 3;
	//人员加入会场
	public static final int MEMBER_JOINED = 11;
	//人员离开会场
	public static final int MEMBER_LEFT = 12;
	//人员被踢出会场（暂时不支持）
	public static final int MEMBER_KICKED = 13;
	//人员改变角色（暂时不支持）
	public static final int MEMBER_TYPE_UPDATED = 14;
	//人员发布资源
	public static final int PUBLISH_RESOURCE = 20;
	//人员取消发布资源
	public static final int UNPUBLISH_RESOURCE = 21;
	//资源状态发生改变
	public static final int RESOURCE_STATE_CHANGED = 22;
}
