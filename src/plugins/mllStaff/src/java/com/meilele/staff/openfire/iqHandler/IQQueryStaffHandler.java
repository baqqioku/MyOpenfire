package com.meilele.staff.openfire.iqHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.dom4j.Element;
import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.PacketDeliverer;
import org.jivesoftware.openfire.PacketRouter;
import org.jivesoftware.openfire.PresenceManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.session.LocalClientSession;
import org.jivesoftware.openfire.spi.ConnectionManagerImpl;
import org.jivesoftware.openfire.spi.ConnectionType;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.component.IQResultListener;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.Presence;

import com.alibaba.fastjson.JSON;
import com.meilele.staff.business.common.StoreStaffUserCountManager;
import com.meilele.staff.business.staff.DbStaffInfoManager;
import com.meilele.staff.business.staff.StaffInfo;
import com.meilele.staff.business.store.DbBasicInfoManager;
import com.meilele.staff.openfire.iqError.IQError;

public class IQQueryStaffHandler extends IQHandler {
	private static final Logger Log = LoggerFactory.getLogger(IQQueryStaffHandler.class);
	private static final IQ EMPTY_IQ = new IQ();
	private static final StaffInfo NO_NEXT_QUERY_STAFF = new StaffInfo();
	private static final int STAFF_ACCEPT_USER = 1;
	private final IQHandlerInfo info;
	private volatile PacketDeliverer packetDeliverer;
	private static final int CURRENT_HANDLE_IQ_VALUE = 1;
	private final ConcurrentHashMap<String, Integer> currentHandleIq = new ConcurrentHashMap<String, Integer>();

	private UserManager userManager = null;
	private IQRouter iqRouter = null;
	private PresenceManager presenceManager = null;

	private IQQueryStaffHandler() {
		super("Query Staff handler");
		this.info = new IQHandlerInfo("query", "urn:meilele:iq:queryStaff");
	}

	private static final IQQueryStaffHandler HANDLER = new IQQueryStaffHandler();

	public static IQQueryStaffHandler getInstance() {
		return HANDLER;
	}

	public IQ handleIQ(IQ packet) {
		try {
			ExecutorFilter executorFilter = (ExecutorFilter) ((ConnectionManagerImpl) XMPPServer.getInstance().getConnectionManager()).getListener(ConnectionType.SOCKET_C2S, false).getSocketAcceptor().getFilterChain().get("threadModel");

			Log.info("now thread : {},active count : {}!", Thread.currentThread(), Integer.valueOf(((ThreadPoolExecutor) executorFilter.getExecutor()).getActiveCount()));
		} catch (Exception e) {
			Log.error("Exception when print mina threads info!", e);
		}
		Log.info("receive query staff IQ : {}", packet.toXML());
		if (packet.isResponse()) {
			Log.info("IQQueryStaffHandler receive response packet : {}.", packet.toXML());
			return null;
		}
		LocalClientSession session = (LocalClientSession) this.sessionManager.getSession(packet.getFrom());
		if (session == null) {
			Log.error("Error during query staff. Session not found in {} for key {} for packetId {}", new Object[] { this.sessionManager.getPreAuthenticatedKeys(), packet.getFrom(), packet.getID() });

			return createErrorIQReply(packet, PacketError.Condition.internal_server_error, null);
		}
		try {
			if (checkDuplicateIq(packet)) {
				Log.error("duplicate query staff iq id : {}!", packet.getID());
				return createErrorIQReply(packet, PacketError.Condition.bad_request, IQError.DUPLICATE_QUERY_STAFF_IQ);
			}
			String storeUUId = packet.getChildElement().elementTextTrim("storeUUID");
			if (StringUtils.isBlank(storeUUId)) {
				Log.error("Invalid store UUID.PacketID : {},packet from : {},storeUUId :{}.", new Object[] { packet.getID(), packet.getFrom(), storeUUId });

				return createErrorIQReply(packet, PacketError.Condition.bad_request, IQError.INVALID_STAFF_STORE_ID);
			}
			if (!DbBasicInfoManager.getInstance().querySysUuidStatus(storeUUId)) {
				return createErrorIQReply(packet, PacketError.Condition.bad_request, IQError.STORE_UUID_NOT_AVALIABLE);
			}
			sendIqToStaff(packet, storeUUId);

			return null;
		} catch (Exception e) {
			Log.error("exception during query staff!", e);
			return createErrorIQReply(packet, PacketError.Condition.internal_server_error, IQError.EXCEPTION_DURING_QUERY_STAFF);
		} finally {
			removeCurrentHandleIq(packet);
		}
	}

	private void removeCurrentHandleIq(IQ kfQueryIq) {
		this.currentHandleIq.remove(kfQueryIq.getID());
	}

	private void deliverReturnIq(IQ kfQueryIq, IQ staffReply) {
		IQ resultIq;
		if (staffReply == EMPTY_IQ) {
			resultIq = createErrorIQReply(kfQueryIq, PacketError.Condition.internal_server_error, IQError.NO_STAFF_ACCEPT_USER);
		} else {
			Log.info("query iq id : {},reply : {}", kfQueryIq.getID(), staffReply.toXML());

			StaffInfo staffInfo = null;
			try {
				staffInfo = DbStaffInfoManager.getInstance().querySysUuid(staffReply.getFrom().getNode());
			} catch (SQLException e) {
				Log.error("query staff exception,iq id : " + kfQueryIq.getID(), e);
				resultIq = createErrorIQReply(kfQueryIq, PacketError.Condition.internal_server_error, IQError.QUERY_STAFF_BY_SYS_UUID_EXCEPTION);
			}
			if (staffInfo == null) {
				Log.error("no staff of sys uuid : {},iq id : {}", staffReply.getFrom().getNode(), kfQueryIq.getID());

				resultIq = createErrorIQReply(kfQueryIq, PacketError.Condition.internal_server_error, IQError.NO_STAFF_OF_SYS_UUID);
			} else {
				String landerUUID = DbBasicInfoManager.getInstance().querySysStaffInfoUuid(staffInfo.getSysUuid());

				resultIq = IQ.createResultIQ(kfQueryIq);
				Element resultElement = kfQueryIq.getChildElement().createCopy();
				resultElement.addElement("staff-name").addText(staffInfo.getStaffName());

				resultElement.addElement("staff-uuid").addText(staffInfo.getSysUuid());

				resultElement.addElement("lander-uuid").addText(landerUUID);

				resultIq.setChildElement(resultElement);
				Log.info("query staff success of query iq id : {},result : {}", kfQueryIq.getID(), resultIq.toXML());

				concurrentAddOneStaffUserCount(staffInfo.getSysUuid(), staffInfo.getStoreUuid());
			}
		}
		try {
			this.packetDeliverer.deliver(resultIq);
		} catch (UnauthorizedException e) {
			if (kfQueryIq != null) {
				try {
					IQ response = IQ.createResultIQ(kfQueryIq);
					response.setChildElement(kfQueryIq.getChildElement().createCopy());
					response.setError(PacketError.Condition.not_authorized);
					this.sessionManager.getSession(kfQueryIq.getFrom()).process(response);
				} catch (Exception de) {
					Log.error(LocaleUtils.getLocalizedString("admin.error"), de);
					this.sessionManager.getSession(kfQueryIq.getFrom()).close();
				}
			}
		} catch (Exception e) {
			Log.error(LocaleUtils.getLocalizedString("admin.error"), e);
			try {
				IQ response = IQ.createResultIQ(kfQueryIq);
				response.setChildElement(kfQueryIq.getChildElement().createCopy());
				response.setError(PacketError.Condition.internal_server_error);
				this.sessionManager.getSession(kfQueryIq.getFrom()).process(response);
			} catch (Exception e1) {
			}
		}
	}

	private void concurrentAddOneStaffUserCount(String staffUuid, String storeUuid) {
		StoreStaffUserCountManager.addOneStaffUserCount(staffUuid);
	}

	private void sendIqToStaff(IQ packet, String storeUUId) {
		List<StaffInfo> staffInfos = getAllPresenceAvailableStaffs(storeUUId);

		Log.info("packet id : {} get all presence available staffs : {}.", packet.getID(), JSON.toJSONString(staffInfos));
		if (CollectionUtils.isEmpty(staffInfos)) {
			deliverReturnIq(packet, EMPTY_IQ);
		} else {
			Log.info("packet id : {} get all sorted staffs : {}.", packet.getID(), JSON.toJSONString(staffInfos));

			loopStaff(staffInfos, packet, storeUUId);
		}
	}

	private List<StaffInfo> getAllPresenceAvailableStaffs(String storeUUId) {
		List<StaffInfo> staffInfos = getAllAvaliableStaffs(storeUUId);
		removePresenceUnavailableStaff(staffInfos);
		staffInfos = staffInfos == null ? new ArrayList<StaffInfo>() : staffInfos;
		return staffInfos;
	}

	private List<StaffInfo> getAllAvaliableStaffs(String storeUUId) {
		List<StaffInfo> staffInfos = null;
		try {
			staffInfos = DbStaffInfoManager.getInstance().queryList(storeUUId);
		} catch (SQLException e) {
			Log.error("SQLException in getAllStaffs!", e);
			staffInfos = null;
		}
		return staffInfos;
	}

	private boolean checkDuplicateIq(IQ kfQueryIq) {
		Integer value = (Integer) this.currentHandleIq.putIfAbsent(kfQueryIq.getID(), Integer.valueOf(1));
		return value != null;
	}

	private static StaffInfo getNextNotQueryStaff(List<StaffInfo> staffInfos, String storeUUId) {
		if (CollectionUtils.isEmpty(staffInfos)) {
			return NO_NEXT_QUERY_STAFF;
		}
		StoreStaffUserCountManager.sortStaff(staffInfos);

		return (StaffInfo) staffInfos.remove(0);
	}

	private void removePresenceUnavailableStaff(List<StaffInfo> staffInfos) {
		if (CollectionUtils.isEmpty(staffInfos)) {
			return;
		}
		Iterator<StaffInfo> iterator = staffInfos.iterator();
		while (iterator.hasNext()) {
			StaffInfo staffInfo = (StaffInfo) iterator.next();
			if (this.userManager.isRegisteredUser(staffInfo.getSysUuid())) {
				User user = null;
				try {
					user = this.userManager.getUser(staffInfo.getSysUuid());
				} catch (UserNotFoundException e) {
				}
				if (!this.presenceManager.isAvailable(user)) {
					iterator.remove();
				}
			} else {
				iterator.remove();
			}
		}
	}

	public void initialize(XMPPServer server) {
		super.initialize(server);
		this.userManager = server.getUserManager();
		this.iqRouter = server.getIQRouter();
		this.presenceManager = server.getPresenceManager();
		this.packetDeliverer = server.getPacketDeliverer();
	}

	public IQHandlerInfo getInfo() {
		return this.info;
	}

	private IQ createErrorIQReply(IQ rawIQ, PacketError.Condition errorCondition, IQError iqError) {
		IQ reply = IQ.createResultIQ(rawIQ);
		reply.setChildElement(rawIQ.getChildElement().createCopy());
		reply.setError(errorCondition);
		if (iqError != null) {
			reply.getChildElement().addElement("errorMsg").addAttribute("code", String.valueOf(iqError.getErrorCode())).addText(iqError.getErrorMsg());
		}
		Log.info("error iq reply : {}", reply.toXML());
		return reply;
	}

	private void sendPacket(Packet packet) {
		PacketRouter router = XMPPServer.getInstance().getPacketRouter();
		if (router != null) {
			router.route(packet);
		}
	}

	private List<IQ> createStaffAcceptUserIQ(String sysUuid) {
		Collection<Presence> presences = this.presenceManager.getPresences(sysUuid);
		List<IQ> iqs = new ArrayList<IQ>();
		IQ firstIq = null;
		for (Presence presence : presences) {
			if (presence.isAvailable()) {
				IQ iq = new IQ(IQ.Type.get);
				if (firstIq != null) {
					iq.setID(firstIq.getID());
				}
				iq.setTo(presence.getFrom());
				iq.setChildElement("accept", "urn:meilele:iq:StaffAcceptUser");

				Log.info("server create staff accept user IQ : {}", iq.toXML());
				iqs.add(iq);
				if (firstIq == null) {
					firstIq = iq;
				}
			}
		}
		return iqs;
	}

	private static boolean parseStaffAnswer(IQ staffAnswer) {
		if (staffAnswer.getChildElement() == null) {
			return false;
		}
		String result = staffAnswer.getChildElement().elementTextTrim("result");
		if (StringUtils.isBlank(result)) {
			return false;
		}
		if (!NumberUtils.isNumber(result)) {
			return false;
		}
		int resultInt = NumberUtils.toInt(result);
		return resultInt == 1;
	}

	private static class QueryStaffIqResultListener implements IQResultListener {
		private final List<StaffInfo> remainStaffInfos;
		private final IQ rawPacket;
		private final String storeUUId;

		QueryStaffIqResultListener(List<StaffInfo> remainStaffInfos, IQ rawPacket, String storeUUId) {
			this.remainStaffInfos = remainStaffInfos;
			this.rawPacket = rawPacket;
			this.storeUUId = storeUUId;
		}

		public void receivedAnswer(IQ packet) {
			try {
				IQQueryStaffHandler.Log.info("accept staff iq : {}", packet.toXML());
				if (IQQueryStaffHandler.parseStaffAnswer(packet)) {
					IQQueryStaffHandler.getInstance().deliverReturnIq(this.rawPacket, packet);
				} else {
					continueLoop();
				}
			} catch (Exception e) {
				IQQueryStaffHandler.Log.error("receivedAnswer exception of packet : " + packet.toXML(), e);
				continueLoop();
			}
		}

		public void answerTimeout(String packetId) {
			continueLoop();
		}

		private void continueLoop() {
			IQQueryStaffHandler.getInstance().loopStaff(this.remainStaffInfos, this.rawPacket, this.storeUUId);
		}
	}

	private void loopStaff(List<StaffInfo> staffInfos, IQ rawPacket, String storeUUId) {
		StaffInfo nextStaff = getNextNotQueryStaff(staffInfos, storeUUId);
		if (nextStaff == NO_NEXT_QUERY_STAFF) {
			deliverReturnIq(rawPacket, EMPTY_IQ);
			return;
		}
		Log.info("query iq id : {} get staff : {}", rawPacket.getID(), JSON.toJSONString(nextStaff));

		List<IQ> staffAcceptIQs = createStaffAcceptUserIQ(nextStaff.getSysUuid());
		if (CollectionUtils.isEmpty(staffAcceptIQs)) {
			Log.info("query iq id : {} get staff : {},but not online!", rawPacket.getID(), JSON.toJSONString(nextStaff));

			loopStaff(staffInfos, rawPacket, storeUUId);
		} else {
			this.iqRouter.addIQResultListener(((IQ) staffAcceptIQs.get(0)).getID(), new QueryStaffIqResultListener(staffInfos, rawPacket, storeUUId), 60000L);
			for (IQ staffAcceptIQ : staffAcceptIQs) {
				sendPacket(staffAcceptIQ);
			}
		}
	}

	public static void main(String[] args) {
		IQ iq = new IQ();
		iq.setChildElement("dispatch", "urn:meilele:iq:Dispatch");
		Element resultElement = iq.getChildElement();
		resultElement.addElement("staff-name").addText("luyi");

		resultElement.addElement("staff-uuid").addText("123");

		System.out.println(iq.toXML());
		System.out.println(iq.getChildElement().elementTextTrim("staff-name"));
	}
}
