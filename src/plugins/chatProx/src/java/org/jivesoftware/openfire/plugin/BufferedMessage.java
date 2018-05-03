package org.jivesoftware.openfire.plugin;

import java.util.Date;
import org.xmpp.packet.Message;

public class BufferedMessage {
	private Message message;
	private Date bufferedDate;
	private int retryCount;

	public Message getMessage() {
		return this.message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public Date getBufferedDate() {
		return this.bufferedDate;
	}

	public void setBufferedDate(Date bufferedDate) {
		this.bufferedDate = bufferedDate;
	}

	public int getRetryCount() {
		return this.retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public BufferedMessage(Message msg, Date bufferedDate, int retryCount) {
		this.message = msg;
		this.bufferedDate = bufferedDate;
		this.retryCount = retryCount;
	}
}
