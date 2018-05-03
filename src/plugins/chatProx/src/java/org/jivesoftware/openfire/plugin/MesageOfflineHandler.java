package org.jivesoftware.openfire.plugin;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Message;
import org.xmpp.packet.PacketExtension;

public class MesageOfflineHandler {
	private static ConcurrentHashMap<String, BufferedMessage> msgMap;
	private static DbChatProxManager logManager;
	private static MesageOfflineHandler handler = new MesageOfflineHandler();
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageBufferHandler.class);

	public static final Long RETRY_MIN_TIME = 6000L;
	private static Long RETRY_LIMIT_TIME = 0L;
	ExecutorService pool = Executors.newFixedThreadPool(10);

	private MesageOfflineHandler() {
		if (msgMap == null) {
			msgMap = new ConcurrentHashMap<String, BufferedMessage>();
			RETRY_LIMIT_TIME = JiveGlobals.getLongProperty("waite.received.time", 120000L);
		}
		logManager = DbChatProxManager.getInstance();
	}

	public static MesageOfflineHandler getHandler() {
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

	public void removeMsgByReceipt(final Message msg) {
		pool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					PacketExtension receipt = msg.getExtension(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE);
					String receivedId = null;
					if (receipt != null) {
						receivedId = receipt.getElement().attributeValue("id");
					}
					LOGGER.info("Parsed msg :{}, ", receivedId);
					if ((StringUtils.isNotEmpty(receivedId)) && (StringUtils.isNotEmpty(receivedId))) {
						LOGGER.info("Remove msg by Receipt buffer for :{}, before remove buffer size is:{}!", receivedId, Integer.valueOf(msgMap.size()));
						msgMap.remove(receivedId);
					}
				} catch (Exception e) {
					LOGGER.warn("Parse msg error:{}", msg.toXML());
				}
			}
		});
	}

	public void StoreOffMessage() {
		pool.execute(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if ((msgMap != null) && (msgMap.size() > 0)) {
					for (String key : msgMap.keySet()) {
						BufferedMessage bufferedMessage = (BufferedMessage) msgMap.get(key);
						Date now = new Date();
						if ((bufferedMessage.getBufferedDate() != null) && (bufferedMessage.getBufferedDate().getTime() > 0L)) {

							if (now.getTime() - bufferedMessage.getBufferedDate().getTime() >= RETRY_LIMIT_TIME.longValue()) {
								if (!logManager.checkOfflienMessage(bufferedMessage.getMessage().getID())) {
									logManager.delMessageArchive(bufferedMessage.getMessage().getID());
									logManager.insertOffline(bufferedMessage.getMessage());
								}
								removeMsgById(bufferedMessage.getMessage());
							}
						}
					}
				}
			}
		});
	}

	public void removeMsgById(Message msg) {
		if (StringUtils.isNotEmpty(msg.getID())) {
			LOGGER.info("Remove msg by retry time out buffer for :{}, before remove buffer size is:{}!", msg.getID(), Integer.valueOf(msgMap.size()));
			msgMap.remove(msg.getID());
		}
	}

}
