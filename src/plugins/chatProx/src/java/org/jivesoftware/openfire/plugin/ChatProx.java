package org.jivesoftware.openfire.plugin;

import java.sql.Timestamp;
import org.jivesoftware.util.JiveConstants;

public class ChatProx {
	private long messageId;
	private String sessionJID;
	private String sender;
	private String receiver;
	private Timestamp createDate;
	private String content;
	private String receipt_id;
	private String groupType;
	private String detail;
	private int length;
	private int state;

	public String getGroupType() {
		return this.groupType;
	}

	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}

	public String getReceipt_id() {
		return this.receipt_id;
	}

	public void setReceipt_id(String receipt_id) {
		this.receipt_id = receipt_id;
	}

	public String getSessionJID() {
		return this.sessionJID;
	}

	public void setSessionJID(String sessionJID) {
		this.sessionJID = sessionJID;
	}

	public long getMessageId() {
		return this.messageId;
	}

	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}

	public String getSender() {
		return this.sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getReceiver() {
		return this.receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public Timestamp getCreateDate() {
		return this.createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDetail() {
		return this.detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public int getLength() {
		return this.length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getState() {
		return this.state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public ChatProx() {
	}

	public ChatProx(String sessionJID, Timestamp createDate, String content, String detail, int length) {
		this.sessionJID = sessionJID;
		this.createDate = createDate;
		this.content = content;
		this.detail = detail;
		this.length = length;
	}

	public ChatProx(long messageId, String sessionJID, Timestamp createDate, String content, String detail, int length,
			int state) {
		this.messageId = messageId;
		this.sessionJID = sessionJID;
		this.createDate = createDate;
		this.content = content;
		this.detail = detail;
		this.length = length;
		this.state = state;
	}

	public class ChatProxConstants extends JiveConstants {
		public static final int CHAT_LOGS = 52;
		public static final int USER_ONLINE_STATE = 53;

		public ChatProxConstants() {
		}
	}

	public static abstract interface LogState {
		public static final int show = 0;
		public static final int remove = 1;
	}
}