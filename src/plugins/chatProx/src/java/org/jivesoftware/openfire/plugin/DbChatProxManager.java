package org.jivesoftware.openfire.plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.database.SequenceManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.util.LocaleUtils;
import org.jivesoftware.util.page.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

public class DbChatProxManager {
	private static final Logger Log = LoggerFactory.getLogger(DbChatProxManager.class);
	private static final DbChatProxManager CHAT_LOGS_MANAGER = new DbChatProxManager();
	public static final String MYSQL_SQL = "{0} limit {1},{2}";
	private static final String LOGS_QUERY = "SELECT conversationID, fromJID, toJID, sentDate, body, receipt_id FROM ofMessageArchive  ";
	private static final String INSERT_OFFLINE = "INSERT INTO ofOffline (username, messageID, creationDate, messageSize, stanza,receipt_id) VALUES (?, ?, ?, ?, ?,?)";
	private static final String XMPPDOMAIN = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
	private static final String STAFF_QUER_FIRST = "SELECT * FROM ( ";
	private static final String STAFF_QUER_LAST = " ) tmp order by sentDate desc";
	private static final String STAFF_QUERY_GROUP_TYPE = "SELECT conversationID, fromJID, toJID, sentDate, body, receipt_id FROM ofMessageArchive  ";
	private static final String MYSQL_SQL_COUNT = "select count(1) from ({0}) sel_tab00";

	private static final String CHECK_OFFLINE_MESSAGE = "select  receipt_id from ofOffline where  receipt_id= ?";

	private static final String SELECT_MESSAGE = "SELECT conversationID, fromJID, toJID, sentDate, body, receipt_id FROM ofMessageArchive where receipt_id = ?";

	private static final String DELETE_MESSAGE = "delete from ofMessageArchive where receipt_id = ? ";
	private static final String USER_NAME = "h5_SYSTEM_";

	public static DbChatProxManager getInstance() {
		return CHAT_LOGS_MANAGER;
	}

	public int getCount(String sql) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int count = -1;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next())
				count = rs.getInt(1);
			else
				count = 0;
		} catch (SQLException sqle) {
			Log.error(sqle.getMessage(), sqle);
			int i = 0;
			return i;
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
		return count;
	}

	public String createEntity(ChatProx entity) {
		String sql = "";
		if (entity != null) {
			if ((!org.apache.commons.lang.StringUtils.isEmpty(entity.getSender())) && (!org.apache.commons.lang.StringUtils.isEmpty(entity.getReceiver()))) {
				sql = "SELECT conversationID, fromJID, toJID, sentDate, body, receipt_id FROM ofMessageArchive  ";
				sql = sql + " where  fromJID = '" + addDomain(entity.getSender()) + "' and toJID = '" + addDomain(entity.getReceiver()) + "'";
				sql = sql + " UNION SELECT conversationID, fromJID, toJID, sentDate, body, receipt_id FROM ofMessageArchive  ";
				sql = sql + " where  fromJID = '" + addDomain(entity.getReceiver()) + "' and toJID = '" + addDomain(entity.getSender()) + "'";
			} else {
				sql = "SELECT conversationID, fromJID, toJID, sentDate, body, receipt_id FROM ofMessageArchive  ";
				sql = sql + " where fromJID = '" + addDomain(entity.getSender()) + "'";
				sql = sql + " UNION SELECT conversationID, fromJID, toJID, sentDate, body, receipt_id FROM ofMessageArchive  ";
				sql = sql + " where  toJID = '" + addDomain(entity.getSender()) + "'";
			}
		}
		sql = sql + " order by sentDate desc";
		Log.info("ofMessageArchive sql={}", sql);
		return sql;
	}

	public static String createPageSql(String sql, int pageNo, int pageSize) {
		int beginNum = (pageNo - 1) * pageSize;
		Object[] sqlParam = new String[3];
		sqlParam[0] = sql;
		sqlParam[1] = String.valueOf(beginNum);
		sqlParam[2] = String.valueOf(pageSize);
		sql = MessageFormat.format("{0} limit {1},{2}", sqlParam);
		return sql;
	}

	public List<Message> queryList(ChatProx entity, int pageNo, int pageSize) throws SQLException {
		Connection con = null;
		Statement pstmt = null;
		Message message = null;
		List<Message> result = new ArrayList<Message>();
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.createStatement();
			String sql = createEntity(entity);
			ResultSet rs = pstmt.executeQuery(createPageSql(sql, pageNo, pageSize));
			result = new ArrayList<Message>(rs.getRow());
			while (rs.next()) {
				message = new Message();
				message.setMessageId(rs.getLong("conversationID"));
				message.setContent(rs.getString("body"));

				message.setCreateDate(Long.valueOf(rs.getLong("sentDate")));

				message.setSender(cancelDomain(rs.getString("fromJID")));

				message.setReceiver(cancelDomain(rs.getString("toJID")));

				message.setReceipt_id(rs.getString("receipt_id"));
				result.add(message);
			}

			return result;
		} catch (SQLException sqle) {
			Log.error(sqle.getMessage(), sqle);
			throw new SQLException(sqle);
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
	}

	public List<MllChatSession> queryMessageSessions(String sql) throws SQLException {
		if (org.apache.commons.lang.StringUtils.isEmpty(sql)) {
			return null;
		}
		Connection con = null;
		Statement pstmt = null;
		ResultSet ret = null;
		List<MllChatSession> sessions = new ArrayList<MllChatSession>();
		List<Message> msgs = new ArrayList<Message>();
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.createStatement();
			ret = pstmt.executeQuery(sql);
			while (ret.next()) {
				String sessionId = ret.getString(3);
				Message msg = new Message();
				msg.setCreateDate(Long.valueOf(ret.getLong(11)));
				msg.setSender(ret.getString(12));
				msg.setReceiver(ret.getString(13));
				msg.setContent(ret.getString(14));
				msg.setMessageId(ret.getInt(15));
				msg.setReceipt_id(ret.getString(16));
				boolean isHaveSession = false;
				for (MllChatSession mllChatSession : sessions) {
					if ((mllChatSession.getSessionId().equals(sessionId)) && (mllChatSession.getMessages() != null) && (mllChatSession.getMessages().size() > 0)) {
						isHaveSession = true;
						mllChatSession.getMessages().add(msg);
						break;
					}
				}

				if (!isHaveSession) {
					String sessionFormatDate = ret.getString(1);
					int id = ret.getInt(2);
					String customerCookie = ret.getString(4);
					String customerIp = ret.getString(5);
					String customerAddress = ret.getString(6);
					String customerName = ret.getString(7);
					long sessionDate = ret.getLong(8);
					String serviceName = ret.getString(9);
					String semSrc = ret.getString(10);
					MllChatSession session = new MllChatSession(Integer.valueOf(id), sessionId, serviceName, customerIp, customerAddress, customerName, Long.valueOf(sessionDate), sessionFormatDate, customerCookie, semSrc);
					msgs.add(msg);
					session.setMessages(msgs);
					sessions.add(session);
				}
			}
			ret.close();
			pstmt.close();
			con.close();
		} catch (Exception sqle) {
			Log.error("QueryMessageSessions exception:{}", sqle);
			throw new SQLException(sqle);
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
		return sessions;
	}

	public Pagination<Message> getPage(ChatProx entity, int pageNo, int pageSize) throws SQLException {
		Pagination<Message> pageList = new Pagination<Message>(pageNo, pageSize, 10000);
		pageList.setList(queryList(entity, pageNo, pageSize));
		return pageList;
	}

	public void insertOffline(org.xmpp.packet.Message message) {
		if (message == null) {
			return;
		}
		JID recipient = message.getTo();
		String username = recipient.getNode();

		JID from = message.getFrom();
		String fromUserName = from.getNode();

		if (StringUtils.isBlank(fromUserName)) {
			// from =
			// USER_NAME+"@"+XMPPServer.getInstance().getServerInfo().getXMPPDomain().
			from = new JID(USER_NAME, XMPPServer.getInstance().getServerInfo().getXMPPDomain(), USER_NAME, true);
		}
		if ((org.apache.commons.lang.StringUtils.isBlank(username)) || (org.apache.commons.lang.StringUtils.isBlank(message.getBody())) || (org.apache.commons.lang.StringUtils.isBlank(message.getID()))) {
			return;
		}
		long messageID = SequenceManager.nextID(19);

		String msgXML = message.getElement().asXML();
		
		// Log.info("insert offline message : {},from : {},to : {},message ID :
		// {}", new Object[] { msgXML, message.getFrom(), message.getTo(),
		// Long.valueOf(messageID) });
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement("INSERT INTO ofOffline (username, messageID, creationDate, messageSize, stanza,receipt_id) VALUES (?, ?, ?, ?, ?,?)");
			pstmt.setString(1, username);
			pstmt.setLong(2, messageID);
			pstmt.setString(3, org.jivesoftware.util.StringUtils.dateToMillis(new Date()));
			pstmt.setInt(4, msgXML.length());
			pstmt.setString(5, msgXML);
			pstmt.setString(6, message.getID());
			pstmt.executeUpdate();
		} catch (Exception e) {
			Log.error(LocaleUtils.getLocalizedString("admin.error"), e);
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
	}

	public static String resultByPostRequest(Map<String, String> params, String url) {
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
		HttpConnectionParams.setSoTimeout(httpParameters, 10000);
		HttpClient httpclient = new DefaultHttpClient(httpParameters);
		HttpPost request = new HttpPost(url);
		String result = String.valueOf(404);
		HttpEntity resEntity = null;
		try {
			request.getParams().setParameter("http.method.retry-handler", new DefaultHttpMethodRetryHandler(3, true));

			if (params != null) {
				List<BasicNameValuePair> paramsList = new ArrayList<BasicNameValuePair>();
				for (String key : params.keySet()) {
					paramsList.add(new BasicNameValuePair(key, (String) params.get(key)));
				}
				request.setEntity(new UrlEncodedFormEntity(paramsList, "UTF-8"));
			}
			HttpResponse response = httpclient.execute(request);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				resEntity = response.getEntity();
				result = EntityUtils.toString(resEntity);
			} else {
				result = String.valueOf(statusCode);
			}
			Log.info("发送离线信息接口:{}", result);
		} catch (Exception e) {
			Log.error("发送离线信息接口:{}", e);
		} finally {
			request.abort();
			httpclient.getConnectionManager().shutdown();
		}
		return result;
	}

	public static String addDomain(String str) {
		return str + "@" + XMPPDOMAIN;
	}

	public static String cancelDomain(String str) {
		return str.split("@")[0];
	}

	public Pagination<Message> getPageStaff(ChatStaff entity, int pageNo, int pageSize) throws SQLException {
		int totalCount = getCount(createCountPageSql(createEntityStaff(entity)));
		Pagination<Message> pageList = new Pagination<Message>(pageNo, pageSize, totalCount);
		pageList.setList(queryListStaff(entity, pageNo, pageSize));
		return pageList;
	}

	public static String createCountPageSql(String sql) {
		Object[] sqlParam = new String[1];
		sqlParam[0] = sql;
		sql = MessageFormat.format("select count(1) from ({0}) sel_tab00", sqlParam);
		return sql;
	}

	public static String createEntityStaff(ChatStaff entity) {
		String sql = "";
		if (entity != null) {
			if ((org.apache.commons.lang.StringUtils.isNotBlank(entity.getStartDate())) && (org.apache.commons.lang.StringUtils.isNotBlank(entity.getEndDate()))) {
				sql = "SELECT * FROM ( ";
				sql = sql + "SELECT conversationID, fromJID, toJID, sentDate, body, receipt_id FROM ofMessageArchive  ";
				sql = sql + " where  toJID like 'h5_%' and sentDate > UNIX_TIMESTAMP('" + entity.getStartDate() + "')*1000 and sentDate < UNIX_TIMESTAMP('" + entity.getEndDate() + "')*1000 ";
				sql = sql + " UNION SELECT conversationID, fromJID, toJID, sentDate, body, receipt_id FROM ofMessageArchive  ";
				sql = sql + " where  fromJID like 'h5_%' and sentDate > UNIX_TIMESTAMP('" + entity.getStartDate() + "')*1000 and sentDate < UNIX_TIMESTAMP('" + entity.getEndDate() + "')*1000 ";
				sql = sql + " ) tmp order by sentDate desc";
			} else if (org.apache.commons.lang.StringUtils.isNotBlank(entity.getSender())) {
				sql = "SELECT conversationID, fromJID, toJID, sentDate, body, receipt_id FROM ofMessageArchive  ";
				sql = sql + " where fromJID = '" + addDomain(entity.getSender()) + "'";
				sql = sql + " UNION SELECT conversationID, fromJID, toJID, sentDate, body, receipt_id FROM ofMessageArchive  ";
				sql = sql + " where  toJID = '" + addDomain(entity.getSender()) + "'";
				sql = sql + " order by sentDate desc";
			}
		}

		Log.info("ofMessageArchive sql={}", sql);
		return sql;
	}

	public List<Message> queryListStaff(ChatStaff entity, int pageNo, int pageSize) throws SQLException {
		Connection con = null;
		Statement pstmt = null;
		Message message = null;
		List<Message> result = new ArrayList<Message>();
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.createStatement();
			String sql = createEntityStaff(entity);
			ResultSet rs = pstmt.executeQuery(createPageSql(sql, pageNo, pageSize));
			result = new ArrayList<Message>(rs.getRow());
			while (rs.next()) {
				message = new Message();
				message.setMessageId(rs.getLong("conversationID"));
				message.setContent(rs.getString("body"));
				message.setCreateDate(Long.valueOf(rs.getLong("sentDate")));
				message.setSender(cancelDomain(rs.getString("fromJID")));
				message.setReceiver(cancelDomain(rs.getString("toJID")));
				message.setReceipt_id(rs.getString("receipt_id"));
				result.add(message);
			}
			return result;
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
	}

	public int delMessageArchive(String receivedId) {
		Message message = getMessage(receivedId);
		int i = 0;
		if (message != null) {
			Connection con = null;
			PreparedStatement pstmt = null;
			try {
				con = DbConnectionManager.getConnection();
				pstmt = con.prepareStatement(DELETE_MESSAGE);
				pstmt.setString(1, receivedId);
				i = pstmt.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				DbConnectionManager.closeConnection(pstmt, con);
			}
		}
		return i;
	}

	public Message getMessage(String receivedId) {
		Connection con = null;
		PreparedStatement pstmt = null;
		Message message = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement(SELECT_MESSAGE);
			pstmt.setString(1, receivedId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				message = new Message();
				message.setMessageId(rs.getLong("conversationID"));
				message.setContent(rs.getString("body"));

				message.setCreateDate(Long.valueOf(rs.getLong("sentDate")));

				message.setSender(cancelDomain(rs.getString("fromJID")));

				message.setReceiver(cancelDomain(rs.getString("toJID")));

				message.setReceipt_id(rs.getString("receipt_id"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
		return message;
	}

	public boolean checkOfflienMessage(String receivedId) {
		boolean flag = false;
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement(CHECK_OFFLINE_MESSAGE);
			pstmt.setString(1, receivedId);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				String receipt_id = rs.getString("receipt_id");
				flag = StringUtils.isNotBlank(receipt_id) ? true : false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
		return flag;
	}

	public static void main(String[] args) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("newsId", "123456");
		map.put("essageTime", org.jivesoftware.util.StringUtils.dateToMillis(new Date()));
		map.put("newsContent", "{\"content\":{\"picUrl\":\"http://7xotwj.com1.z0.glb.clouddn.com/2fb45020-c3cb-4467-b450-20c3cb846725.jpeg?imageMogr2/crop/!480x320a0a0\",\"now\":\"1477657767698\",\"title\":\"那A\",\"clickUrl\":\"http://mllmtest.com/pmwap/con/init?pmConSysUuid=de02dbae-af3c-46ce-adba-9a2df3cad7b6&amp;isShowApp=true\",\"shareUrl\":\"http://mllmtest.com/pmwap/con/init?pmConSysUuid=de02dbae-af3c-46ce-adba-9a2df3cad7b6\",\"canShare\":\"0\",\"remark\":\"这里是套图介绍\",\"pushSysUuid\":\"ba845bd2-f605-4637-bf6e-8c245407547b\"},\"mType\":3002}");
		map.put("staPhone", "57a47606-175f-47c6-9187-b6ef5ce6ffbd");
		map.put("staCookie", "57a47606-175f-47c6-9187-b6ef5ce6ffbd");
		resultByPostRequest(map, "http://192.168.1.6:8070/im/mengOfflinePush");
	}
}