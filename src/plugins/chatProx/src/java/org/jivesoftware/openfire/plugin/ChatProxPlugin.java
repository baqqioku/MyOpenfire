package org.jivesoftware.openfire.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.util.JiveConstants;
import org.jivesoftware.util.TaskEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Message.Type;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketExtension;

public class ChatProxPlugin implements PacketInterceptor, Plugin {
	private static final Logger log = LoggerFactory.getLogger(ChatProxPlugin.class);
	private DbChatProxManager logsManager;
	private InterceptorManager interceptorManager;
	private ScheduledExecutorService scheduExec;
	//private static MesageOfflineHandler hander;
	private static final String USER_NAME = "h5_SYSTEM_";
	private static final String LowerCase_USER_NAME ="h5_system_";
	private TaskEngine taskEngine;
	private TimerTask msgTask;
	
	

	public ChatProxPlugin() {
		this.interceptorManager = InterceptorManager.getInstance();
		logsManager = DbChatProxManager.getInstance();
		//hander = MesageOfflineHandler.getHandler();
		taskEngine = TaskEngine.getInstance();
		msgTask = new TimerTask() {
			@Override
			public void run() {
				new OffMessageStoreTask().run();
			}
		};
	}

	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		this.interceptorManager.addInterceptor(this);
		taskEngine.scheduleAtFixedRate(msgTask, JiveConstants.SECOND*30, JiveConstants.SECOND*30);
		//scheduExec.scheduleAtFixedRate(new OffMessageStoreTask(), 60, 60,TimeUnit.SECONDS);
	}
	
	public void destroyPlugin() {
		this.interceptorManager.removeInterceptor(this);
		this.scheduExec.shutdown();
		log.info("服务器停止,销毁ChatProx插件!");
	}

	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed) throws PacketRejectedException {
		processMessage(packet,session,incoming,processed);
		processReceived(packet, session, incoming, processed);
		receivedPackets(packet, session, incoming, processed);
		doAction(packet, incoming, processed, session);
	}

	public boolean isReceiptMessage(Message message) {
		boolean isReceiptMessage = false;
		if (((message != null) && (org.apache.commons.lang.StringUtils.isEmpty(message.getBody())) && (message.getExtension("properties", "http://www.jivesoftware.com/xmlns/xmpp/properties") != null)) || message.getExtension(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE) != null) {
			isReceiptMessage = true;
		}
		return isReceiptMessage;
	}
	
	/**
	 * 服务器接收到消息 先讲消息存起来，定时去检测 时候有回执，有回执就删掉，没有就存离线表，初定是两分钟轮询检测一次
	 * @param packet
	 * @param session
	 * @param incoming
	 * @param processed
	 */
	
	public void processMessage(Packet packet, Session session, boolean incoming, boolean processed){
		if((packet != null) && ((packet instanceof Message)) && (processed) && (!incoming)){
			Message message = (Message)packet;
			if(message.getBody()!=null){
				JID from = message.getFrom();
				String fromUser = from.toString().toLowerCase();
				if(fromUser.contains(LowerCase_USER_NAME)){
					//RedisMessageStoreManager.setMessage(message.getID());
					if ((processed) || (!incoming)){
						if(logsManager.getMessage(message.getID()) == null){
							MesageOfflineHandler.getHandler().putMsg(message);
						}
					}
				}
			}
		}
	}
	
	/**
	 * 接收到客户端的的回执，表示消息已经确认收到，再将消息从全局的消息map 里删掉 (针对于手机收消息)
	 * @param packet
	 * @param session
	 * @param incoming
	 * @param processed
	 */
	public void processReceived(Packet packet, Session session, boolean incoming, boolean processed) {
		//log.info("incoming:{},processed:{}",incoming,processed);
		if ((packet != null) && ((packet instanceof Message))) {
			Message message = (Message) packet;
			PacketExtension receipt = message.getExtension(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE);
			JID to = message.getTo();
			String toUser = to.toString().toLowerCase();
			if (receipt != null && toUser.contains(LowerCase_USER_NAME)) {
				MesageOfflineHandler.getHandler().removeMsgByReceipt(message);
				log.info("回执:{}", message.toXML());
				return;
			}
		}
	}
	
	/**
	 * 接收到客户端的消息，服务器给客户端发送回执，客户端收到回执，表示发送成功  （针对于手机发消息）
	 * @param packet
	 * @param session
	 * @param incoming
	 * @param processed
	 * @throws PacketRejectedException
	 */
	public void receivedPackets(Packet packet, Session session, boolean incoming, boolean processed) throws PacketRejectedException {

		if ((packet != null) && ((packet instanceof Message))) {
			Message message = (Message) packet;
			log.info("Received message XML:{},is receipt message:{}", message.toXML(), Boolean.valueOf(isReceiptMessage(message)));
		}
		if ((packet != null) && ((packet instanceof Message)) && (incoming == true) && (!processed)) {
			Message message = (Message) packet;

			if (message.getID() == null) {
				return;
			}

			if (!isReceiptMessage(message)) {
				Message receiptMessage = new Message();
				// log.info("isReceiptMessage from
				// before:{}",message.getFrom().toString());
				String fromUser = message.getFrom().getNode();
				String toUser = message.getTo().getNode();

				// log.info("isReceiptMessage to
				// before:{}",message.getTo().toString());
				if (StringUtils.isBlank(fromUser)) {
					JID from = new JID(USER_NAME, XMPPServer.getInstance().getServerInfo().getXMPPDomain(), USER_NAME, true);
					message.setFrom(from);
					if (message.getType() == Message.Type.error) {
						message.setType(Type.chat);
					}
				}

				if (StringUtils.isBlank(toUser)) {
					JID to = new JID(USER_NAME, XMPPServer.getInstance().getServerInfo().getXMPPDomain(), USER_NAME, true);
					message.setTo(to);
					if (message.getType() == Message.Type.error) {
						message.setType(Type.chat);
					}
				}

				/*
				 * log.info("isReceiptMessage from after:{}"
				 * ,message.getFrom().toString()); log.info(
				 * "isReceiptMessage to after:{}",message.getTo().toString());
				 */
				receiptMessage.setTo(message.getFrom());
				receiptMessage.setFrom(message.getTo());
				PacketExtension packetExtension = new PacketExtension("properties", "http://www.jivesoftware.com/xmlns/xmpp/properties");
				Element root = packetExtension.getElement();
				Element propertyElement = root.addElement("property");
				Element nameElement = propertyElement.addElement("name");
				Element valueElement = propertyElement.addElement("value");
				nameElement.setText("messageId");
				valueElement.addAttribute("type", "string");
				valueElement.setText(message.getID());
				receiptMessage.addExtension(packetExtension);
				try {
					log.info("ChatProxPlugin deliver receiptMessage : {}.", receiptMessage.toXML());
					XMPPServer.getInstance().getPacketDeliverer().deliver(receiptMessage);
				} catch (Exception e) {
					log.error("ChatProxPlugin receiptMessage fail of {}.", receiptMessage.toXML());
				}
			}
		}
	}

	private void doAction(Packet packet, boolean incoming, boolean processed, Session session) {
		Packet copyPacket = packet.createCopy();
		if ((packet != null) && ((packet instanceof Message))) {
			Message message = (Message) copyPacket;

			if (message.getType() == Message.Type.chat) {
				if ((processed) || (!incoming)) {
					return;
				}

				log.info("用户 {},在线状态 {}", message.getTo().getNode(), Boolean.valueOf(checkUserOnLine(message)));

				if (!checkUserOnLine(message)) {
					JID jid = message.getFrom();
					JID recipient = message.getTo();
					String sender = jid.getNode();
					String receiver = recipient.getNode();
					if (XMPPServer.getInstance().getUserManager().isRegisteredUser(receiver)) {
						//log.info("增加离线消息记录:" + message.toXML());
						logsManager.insertOffline(message);
						log.info("增加离线消息记录:{}"+message.getBody());
					}
					MesageOfflineHandler.getHandler().removeMsgById(message);//删消息 数据  对方已经离线，已经将消息转存到离线表，消息列表就丢弃这消息
					if ((org.apache.commons.lang.StringUtils.isNotBlank(message.getID())) && (org.apache.commons.lang.StringUtils.isNotBlank(message.getBody()))) {
						Map<String, String> map = new HashMap<String, String>();
						map.put("newsId", message.getID());
						map.put("essageTime", org.jivesoftware.util.StringUtils.dateToMillis(new Date()));
						map.put("newsContent", message.getBody());
						// log.info("调用发送离线消息接口 ：newsID {}, staPhone {},
						// staCookie {},essageTime {},newsContent {} ", new
						// Object[] { message.getID(), receiver, sender,
						// org.jivesoftware.util.StringUtils.dateToMillis(new
						// Date()), message.getBody() });
						if (MllUserUtils.isStaff(sender)) {
							map.put("staPhone", sender);
							map.put("staCookie", receiver);
							log.info("发给微信接口：{}", new Object[] { CustomConstant.WX_POST_URI });
							DbChatProxManager.resultByPostRequest(map, CustomConstant.WX_POST_URI);
						}
						/*
						 * if (MllUserUtils.isStaff(receiver)) {
						 * map.put("staPhone", receiver); map.put("staCookie",
						 * sender); DbChatProxManager.resultByPostRequest(map,
						 * CustomConstant.STAFFAPP_POST_URI);
						 * log.info("发给友盟接口：{}", new Object[] {
						 * CustomConstant.STAFFAPP_POST_URI }); }
						 */
					}
				}
			} else {
				log.info("其他信息：{}", message.toXML());
			}
		}
	}

	public boolean checkUserOnLine(Message message) {
		SessionManager sessionManager = XMPPServer.getInstance().getSessionManager();
		int count = sessionManager.getSessionCount(message.getTo().getNode());
		return count > 0;
	}

	public static int IsUserOnLine(String jid) {
		String url = CustomConstant.SERVER_URL + "/plugins/presence/status?jid=" + jid + "&type=xml";
		System.out.println(url);
		int state = 0;
		try {
			URL oUrl = new URL(url);
			URLConnection oConn = oUrl.openConnection();
			if (oConn != null) {
				BufferedReader oIn = new BufferedReader(new InputStreamReader(oConn.getInputStream()));
				if (null != oIn) {
					String strFlag = oIn.readLine();
					oIn.close();
					if (strFlag.indexOf("type=\"unavailable\"") >= 0) {
						state = 2;
					}
					if (strFlag.indexOf("type=\"error\"") >= 0)
						state = 0;
					else if ((strFlag.indexOf("priority") >= 0) || (strFlag.indexOf("id=\"") >= 0))
						state = 1;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(state);
		return state;
	}

	/*
	 * public static void main(String[] args) {
	 * IsUserOnLine("guoguo@192.168.1.11"); // SessionManager sessionManager =
	 * // XMPPServer.getInstance().getSessionManager();
	 * 
	 * int count = sessionManager.getSessionCount("guoguo");
	 * System.out.println("count:"+count);
	 * 
	 * 
	 * }
	 */
}
