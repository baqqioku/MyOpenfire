package org.jivesoftware.openfire.plugin;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.jivesoftware.openfire.XMPPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Message;
import org.xmpp.packet.PacketExtension;

public class MessageBufferHandler {
	private static ConcurrentHashMap<String, BufferedMessage> msgMap;
	private static MessageBufferHandler handler;
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageBufferHandler.class);

	public static final Long RETRY_MIN_TIME = 5000L;
	public static final Long RETRY_LIMIT_TIME = 120000L;

	private MessageBufferHandler() {
		if (msgMap == null)
			msgMap = new ConcurrentHashMap<String, BufferedMessage>();
	}

	public static MessageBufferHandler getHandler() {
		if (handler == null) {
			synchronized (MessageBufferHandler.class) {
				if (handler == null) {
					handler = new MessageBufferHandler();
				}
			}
		}
		return handler;
	}

	public void putMsg(Message msg) {
		String msgId = msg.getID();
		if (StringUtils.isNotEmpty(msgId)) {
			BufferedMessage bm = new BufferedMessage(msg, new Date(), -1);
			LOGGER.info("Put msg into msgMap:" + msgId);
			msgMap.put(msgId, bm);
		} else {
			LOGGER.warn("NULL id msg!");
		}
	}

	public void removeMsgByReceipt(Message msg) {
		try {
			PacketExtension extension = msg.getExtension("properties", "http://www.jivesoftware.com/xmlns/xmpp/properties");
			String msgId = extension.getElement().element("property").element("value").getStringValue();
			String msgIdStr = extension.getElement().element("property").element("name").getStringValue();
			LOGGER.info("Parsed msg :{}, str:{}!", msgId, msgIdStr);
			if ((StringUtils.isNotEmpty(msgId)) && (StringUtils.isNotEmpty(msgIdStr))) {
				LOGGER.info("Remove msg by Receipt buffer for :{}, before remove buffer size is:{}!", msgId, Integer.valueOf(msgMap.size()));
				msgMap.remove(msgId);
			}
		} catch (Exception e) {
			LOGGER.warn("Parse msg error:{}", msg.toXML());
		}
	}

	public void retryOnce() {
		if ((msgMap != null) && (msgMap.size() > 0))
			for (String key : msgMap.keySet()) {
				BufferedMessage bufferedMessage = (BufferedMessage) msgMap.get(key);
				Date now = new Date();

				if ((bufferedMessage.getBufferedDate() != null) && (bufferedMessage.getBufferedDate().getTime() > 0L)) {
					if ((now.getTime() - bufferedMessage.getBufferedDate().getTime() < RETRY_LIMIT_TIME.longValue()) && (now.getTime() - bufferedMessage.getBufferedDate().getTime() > RETRY_MIN_TIME.longValue())) {
						retrySend(bufferedMessage.getMessage());
					} else if (now.getTime() - bufferedMessage.getBufferedDate().getTime() >= RETRY_LIMIT_TIME.longValue())
						removeMsgById(bufferedMessage.getMessage());
				}
			}
	}

	public void removeMsgById(Message msg) {
		if (StringUtils.isNotEmpty(msg.getID())) {
			LOGGER.info("Remove msg by retry time out buffer for :{}, before remove buffer size is:{}!", msg.getID(), Integer.valueOf(msgMap.size()));
			msgMap.remove(msg.getID());
		}
	}

	private void retrySend(Message message) {
		if ((message != null) && (StringUtils.isNotEmpty(message.getID())))
			try {
				LOGGER.info("Retry deliver receiptMessage : {}, buffer size is:{}", message.toXML(), Integer.valueOf(msgMap.size()));
				XMPPServer.getInstance().getPacketDeliverer().deliver(message);
			} catch (Exception e) {
				LOGGER.error("Retry receiptMessage fail of {}.", message.toXML());
			}
	}
}
