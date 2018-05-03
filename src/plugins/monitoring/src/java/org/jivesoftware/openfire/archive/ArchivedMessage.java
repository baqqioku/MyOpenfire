package org.jivesoftware.openfire.archive;

import java.util.Date;
import org.xmpp.packet.JID;

public class ArchivedMessage {
	private long conversationID;
	private JID fromJID;
	private JID toJID;
	private Date sentDate;
	private String body;
	private boolean roomEvent;
	private String receipt_id;

	public ArchivedMessage(long conversationID, JID fromJID, JID toJID, Date sentDate, String body, boolean roomEvent,
			String receipt_id) {
		this.conversationID = conversationID;

		this.fromJID = fromJID;
		this.toJID = toJID;
		this.sentDate = sentDate;
		this.body = body;
		this.roomEvent = roomEvent;
		this.receipt_id = receipt_id;
	}

	public long getConversationID() {
		return this.conversationID;
	}

	public JID getFromJID() {
		return this.fromJID;
	}

	public JID getToJID() {
		return this.toJID;
	}

	public Date getSentDate() {
		return this.sentDate;
	}

	public String getBody() {
		return this.body;
	}

	public boolean isRoomEvent() {
		return this.roomEvent;
	}

	public String getReceipt_id() {
		return this.receipt_id;
	}

	public void setReceipt_id(String receipt_id) {
		this.receipt_id = receipt_id;
	}
}
