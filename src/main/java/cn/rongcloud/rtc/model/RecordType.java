package cn.rongcloud.rtc.model;

public enum RecordType {
	AutoRecord(1), CustomRecord(2);
	private int value;

	RecordType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}
}
