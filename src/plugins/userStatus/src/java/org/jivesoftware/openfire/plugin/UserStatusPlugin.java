package org.jivesoftware.openfire.plugin;

import java.io.File;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.database.SequenceManager;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.event.SessionEventDispatcher;
import org.jivesoftware.openfire.event.SessionEventListener;
import org.jivesoftware.openfire.http.HttpSession;
import org.jivesoftware.openfire.nio.NIOConnection;
import org.jivesoftware.openfire.session.LocalClientSession;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.websocket.WebSocketConnection;
import org.jivesoftware.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserStatusPlugin implements Plugin, SessionEventListener {
	private static final int MLL_OFUSERSTATUS = 101;
	private static final Logger log = LoggerFactory.getLogger(UserStatusPlugin.class);
	private static final String ADD_USER_STATUS = "Insert INTO mll_OfUserStatus(id, ConnectionType, username, resources, online,IpAddress,startTime,createTime)  VALUES (?, ?, ?, ?,?, ?, ?, ? )";
	private static final String UPDATE_USER_CREATE = "UPDATE mll_OfUserStatus set online=? where logoutTime is null and username=? ";
	private static final String UPDATE_USER_STATUS = "UPDATE mll_OfUserStatus set online=?,logoutTime=?,updateTime=?,todaytime=? where id=? ";
	private static final String OFUSERSTATUS_INFO_QUERY = "SELECT id, ConnectionType , username, resources, online , IpAddress , startTime , logoutTime,createTime,updateTime,todaytime from mll_OfUserStatus where username=? and resources=?  order by id desc ";
	private static final String OFUSERSTATUS_CREATE_QUERY = "SELECT id, ConnectionType , username, resources, online , IpAddress , startTime , logoutTime,createTime,updateTime,todaytime from mll_OfUserStatus where logoutTime is null and username=? and resources=?  order by id desc ";

	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		log.info("UserStatusPlugin initializePlugin: {}", UserStatusPlugin.class);
		SessionEventDispatcher.addListener(this);
	}

	public void destroyPlugin() {
		log.info("UserStatusPlugin destroyPlugin: {}", UserStatusPlugin.class);
		SessionEventDispatcher.removeListener(this);
	}

	public void sessionCreated(Session session) {
		createSession(session);
	}

	public void sessionDestroyed(Session session) {
		destroyedSession(session, "offline");
	}

	public void anonymousSessionCreated(Session session) {
		createSession(session);
	}

	public void anonymousSessionDestroyed(Session session) {
		destroyedSession(session, "offline");
	}

	private String getHostAddress(Session session) {
		try {
			return session.getHostAddress();
		} catch (UnknownHostException e) {
		}
		return "";
	}

	private void createSession(Session session) {
		updateSession(session);
		List<Object> list = new ArrayList<Object>(12);
		String connectionType = null;
		if ((session instanceof LocalClientSession)) {
			LocalClientSession localSession = (LocalClientSession) session;
			org.jivesoftware.openfire.Connection conn = localSession.getConnection();
			if ((conn instanceof NIOConnection)) {
				connectionType = "smack";
			} else if ((conn instanceof WebSocketConnection)) {
				connectionType = "websocket";
			} else if ((conn instanceof HttpSession.HttpVirtualConnection)) {
				connectionType = "bosh";
			} else {
				connectionType = "none";
			}
		}
		list.add(Long.valueOf(SequenceManager.nextID(101)));

		list.add(connectionType);

		list.add(session.getAddress().getNode());

		list.add(session.getAddress().getResource());

		list.add("online");

		list.add(getHostAddress(session));
		if (session.getCreationDate() != null) {
			list.add(Long.valueOf(session.getCreationDate().getTime()));
		} else {
			list.add(Long.valueOf(new Date().getTime()));
		}
		list.add(Long.valueOf(new Date().getTime()));

		QueryRunner qr = new QueryRunner();
		java.sql.Connection con = null;
		try {
			con = DbConnectionManager.getConnection();
			qr.update(con, "Insert INTO mll_OfUserStatus(id, ConnectionType, username, resources, online,IpAddress,startTime,createTime)  VALUES (?, ?, ?, ?,?, ?, ?, ? )", list.toArray());
		} catch (Exception e) {
			log.error("Unable to insert user status for " + session.getAddress(), e);
		} finally {
			DbConnectionManager.closeConnection(con);
		}
	}

	private void destroyedSession(Session session, String online) {
		QueryRunner qr = new QueryRunner();
		java.sql.Connection con = null;
		try {
			List<Object> listQuery = new ArrayList<Object>();

			listQuery.add(session.getAddress().getNode());

			listQuery.add(session.getAddress().getResource());
			con = DbConnectionManager.getConnection();
			MllOfUserStatus mllOfUserStatus = (MllOfUserStatus) qr.query(con, "SELECT id, ConnectionType , username, resources, online , IpAddress , startTime , logoutTime,createTime,updateTime,todaytime from mll_OfUserStatus where username=? and resources=?  order by id desc ", new BeanHandler<MllOfUserStatus>(MllOfUserStatus.class), listQuery.toArray());
			if (mllOfUserStatus != null) {
				List<Object> list = new ArrayList<Object>();
				list.add(online);
				list.add(StringUtils.dateToMillis(new Date()));
				list.add(StringUtils.dateToMillis(new Date()));
				Long longTime = Long.valueOf(new Date().getTime() - mllOfUserStatus.getCreateTime().longValue());
				list.add(longTime);
				list.add(mllOfUserStatus.getId());
				qr.update(con, "UPDATE mll_OfUserStatus set online=?,logoutTime=?,updateTime=?,todaytime=? where id=? ", list.toArray());
			}
		} catch (Exception e) {
			log.error("Unable to update user status for " + session.getAddress(), e);
		} finally {
			DbConnectionManager.closeConnection(con);
		}
	}

	private void updateSession(Session session) {
		QueryRunner qr = new QueryRunner();
		java.sql.Connection con = null;
		try {
			List<Object> listQuery = new ArrayList<Object>();

			listQuery.add(session.getAddress().getNode());

			listQuery.add(session.getAddress().getResource());
			con = DbConnectionManager.getConnection();
			MllOfUserStatus mllOfUserStatus = (MllOfUserStatus) qr.query(con, "SELECT id, ConnectionType , username, resources, online , IpAddress , startTime , logoutTime,createTime,updateTime,todaytime from mll_OfUserStatus where logoutTime is null and username=? and resources=?  order by id desc ", new BeanHandler<MllOfUserStatus>(MllOfUserStatus.class), listQuery.toArray());
			if (mllOfUserStatus != null) {
				List<Object> list = new ArrayList<Object>();
				list.add("offline");

				list.add(mllOfUserStatus.getUsername());
				qr.update(con, "UPDATE mll_OfUserStatus set online=? where logoutTime is null and username=? ", list.toArray());
			}
		} catch (Exception e) {
			log.error("Unable to update user status for " + session.getAddress(), e);
		} finally {
			DbConnectionManager.closeConnection(con);
		}
	}

	public static String getCurrentDate() {
		SimpleDateFormat d = new SimpleDateFormat();
		d.applyPattern("yyyy-MM-dd");
		Date nowdate = new Date();
		String str_date = d.format(nowdate);
		return str_date;
	}

	public void resourceBound(Session session) {
	}
}
