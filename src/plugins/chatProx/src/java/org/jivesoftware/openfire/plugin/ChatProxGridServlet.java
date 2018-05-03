package org.jivesoftware.openfire.plugin;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.jivesoftware.admin.AuthCheckFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatProxGridServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger Log = LoggerFactory.getLogger(ChatProxGridServlet.class);
	private static DbChatProxManager logsManager;
	private static final Calendar CALENDAR = Calendar.getInstance();
	public static final Date GET_CONVERSATION_ROOM_DATE;
	private static final String ORDER_BY_CONDITION = "ORDER BY sessionDate DESC,sentDate ASC";
	private static final String CHAT_RECORD_SQL_NEW_FROM_SERVICE_COOKIE_FORMAT = "mll_session ms join ofMessageArchive oa on oa.fromJID = CONCAT(ms.service_name,'@','"
			+ ServiceUrlUtils.getChatDomain() + "') AND oa.toJID = CONCAT(ms.customer_cookie,'@','"
			+ ServiceUrlUtils.getChatDomain()
			+ "') AND oa.sentDate >= CAST(ms.session_date AS UNSIGNED) AND oa.sentDate <= (CAST(ms.session_date AS UNSIGNED) + 86400000)";

	private static final String CHAT_RECORD_SQL_NEW_FROM_COOKIE_SERVICE_FORMAT = "mll_session ms join ofMessageArchive oa  on oa.fromJID = CONCAT(ms.customer_cookie,'@','"
			+ ServiceUrlUtils.getChatDomain() + "')AND oa.toJID = CONCAT(ms.service_name,'@','"
			+ ServiceUrlUtils.getChatDomain()
			+ "') AND oa.sentDate >= CAST(ms.session_date AS UNSIGNED) AND oa.sentDate <= (CAST(ms.session_date AS UNSIGNED) + 86400000)";
	private static final String BASE_CHAT_RECORD_SQL_SELECT = "SELECT FROM_UNIXTIME(session_date/1000,'%%Y-%%m-%%d') AS sessionDateFormat,id, session_id AS sessionId,customer_cookie AS customerCookie,customer_ip AS customerIp,customer_address AS customerAddress,customer_name AS customerName,session_date AS sessionDate,service_name AS serviceName,sem_src AS semSrc,oa.sentDate AS sentDate,oa.fromJID as fromJID,oa.toJID as toJID,oa.body as body,oa.messageID as messageId,oa.receipt_id as receipt_id";
	private static final String BASE_CHAT_RECORD_SQL_NEW_FORMAT = "SELECT FROM_UNIXTIME(session_date/1000,'%%Y-%%m-%%d') AS sessionDateFormat,id, session_id AS sessionId,customer_cookie AS customerCookie,customer_ip AS customerIp,customer_address AS customerAddress,customer_name AS customerName,session_date AS sessionDate,service_name AS serviceName,sem_src AS semSrc,oa.sentDate AS sentDate,oa.fromJID as fromJID,oa.toJID as toJID,oa.body as body,oa.messageID as messageId,oa.receipt_id as receipt_id FROM "
			+ CHAT_RECORD_SQL_NEW_FROM_SERVICE_COOKIE_FORMAT + " WHERE %s AND oa.body LIKE '%%%s%%' UNION "
			+ "SELECT FROM_UNIXTIME(session_date/1000,'%%Y-%%m-%%d') AS sessionDateFormat,id, session_id AS sessionId,customer_cookie AS customerCookie,customer_ip AS customerIp,customer_address AS customerAddress,customer_name AS customerName,session_date AS sessionDate,service_name AS serviceName,sem_src AS semSrc,oa.sentDate AS sentDate,oa.fromJID as fromJID,oa.toJID as toJID,oa.body as body,oa.messageID as messageId,oa.receipt_id as receipt_id"
			+ " FROM " + CHAT_RECORD_SQL_NEW_FROM_COOKIE_SERVICE_FORMAT + " WHERE %s AND oa.body LIKE '%%%s%%' "
			+ "ORDER BY sessionDate DESC,sentDate ASC";

	private static final String BASE_CHAT_RECORD_SQL_OLD_FORMAT = "SELECT FROM_UNIXTIME(session_date/1000,'%%Y-%%m-%%d') AS sessionDateFormat,id, session_id AS sessionId,customer_cookie AS customerCookie,customer_ip AS customerIp,customer_address AS customerAddress,customer_name AS customerName,session_date AS sessionDate,service_name AS serviceName,sem_src AS semSrc,oa.sentDate AS sentDate,oa.fromJID as fromJID,oa.toJID as toJID,oa.body as body,oa.messageID as messageId,oa.receipt_id as receipt_id FROM "
			+ CHAT_RECORD_SQL_NEW_FROM_SERVICE_COOKIE_FORMAT + " WHERE %s UNION "
			+ "SELECT FROM_UNIXTIME(session_date/1000,'%%Y-%%m-%%d') AS sessionDateFormat,id, session_id AS sessionId,customer_cookie AS customerCookie,customer_ip AS customerIp,customer_address AS customerAddress,customer_name AS customerName,session_date AS sessionDate,service_name AS serviceName,sem_src AS semSrc,oa.sentDate AS sentDate,oa.fromJID as fromJID,oa.toJID as toJID,oa.body as body,oa.messageID as messageId,oa.receipt_id as receipt_id"
			+ " FROM " + CHAT_RECORD_SQL_NEW_FROM_COOKIE_SERVICE_FORMAT + " WHERE %s "
			+ "ORDER BY sessionDate DESC,sentDate ASC";

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		logsManager = DbChatProxManager.getInstance();
		AuthCheckFilter.addExclude("chatprox");
		AuthCheckFilter.addExclude("chatprox/grid");
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		String callback = request.getParameter("callback");
		try {
			if (StringUtils.isNotBlank(callback)) {
				String message = callback + "(" + query(request, response) + ")";
				replyMessage(message, response, out);
			} else {
				String message = query(request, response);
				replyMessage(message, response, out);
			}
		} catch (Exception ex) {
			Log.error(ex.toString());
			replyMessage(ex.toString(), response, out);
		}
	}

	protected String query(HttpServletRequest request, HttpServletResponse response)
			throws ParseException, SQLException, JsonGenerationException, JsonMappingException, IOException {
		Log.info("In query method!");

		Date d = new Date();
		Long endTime_l = Long.valueOf(d.getTime());
		Long begainTime_l = Long.valueOf(endTime_l.longValue() - 604800000L);
		String startTime = request.getParameter("starttime");
		String endTime = request.getParameter("endtime");
		if (StringUtils.isNotEmpty(startTime)) {
			begainTime_l = parseStrToLong(startTime);
		}
		if (StringUtils.isNotEmpty(endTime)) {
			endTime_l = parseStrToLong(endTime);
		}
		Log.info("startTime:{},endTime:{}", begainTime_l, endTime_l);
		MllSearchParameter parameter = new MllSearchParameter();

		String content = request.getParameter("content");
		if (StringUtils.isNotEmpty(content)) {
			parameter.setContent(content);
		}

		String fromCity = request.getParameter("city");
		if (StringUtils.isNotEmpty(fromCity)) {
			parameter.setCustomerAddress(fromCity);
		}

		String from = request.getParameter("from");
		if (StringUtils.isNotEmpty(from)) {
			parameter.setServiceName(replaceUsernameOfSpecialChar(from));
		}

		String ip = request.getParameter("ip");
		if (StringUtils.isNotEmpty(ip)) {
			parameter.setCustomerIp(ip);
		}

		String customerName = request.getParameter("customer");
		if (StringUtils.isNotEmpty(customerName)) {
			parameter.setCustomerName(replaceUsernameOfSpecialChar(customerName));
		}
		Log.info("parameters:{}", parameter.toString());
		String baseSql = getBaseSql(begainTime_l, endTime_l, parameter);
		Log.info("mll chat record base sql : {}", baseSql);
		List<MllChatSession> sessionList = logsManager.queryMessageSessions(baseSql);
		return JSONArray.fromObject(sessionList).toString();
	}

	public static Long parseStrToLong(String dateStr) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date date = sdf.parse(dateStr);
			return Long.valueOf(date.getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Long.valueOf(0L);
	}

	private String createTotalSqlWhere(Long begainTime_l, Long endTime_l, MllSearchParameter parameter) {
		StringBuffer count = new StringBuffer();

		count.append("session_date >='" + begainTime_l.toString() + "' ");
		count.append(" AND session_date <='" + endTime_l.toString() + "' ");
		if (StringUtils.isNotEmpty(parameter.getCustomerName())) {
			count.append(" AND customer_name ='" + parameter.getCustomerName() + "' ");
		}
		if (StringUtils.isNotBlank(parameter.getServiceName())) {
			count.append(" AND service_name = '" + parameter.getServiceName() + "' ");
		}

		if (StringUtils.isNotBlank(parameter.getCustomerIp())) {
			count.append(" AND customer_ip ='" + parameter.getCustomerIp() + "'");
		}

		if (StringUtils.isNotBlank(parameter.getCustomerAddress())) {
			count.append(" AND customer_address like '%" + parameter.getCustomerAddress() + "%' ");
		}
		return count.toString();
	}

	private String getBaseSql(Long begainTime_l, Long endTime_l, MllSearchParameter parameter) {
		String content = parameter.getContent();
		if ((new Date(begainTime_l.longValue()).compareTo(GET_CONVERSATION_ROOM_DATE) >= 0)
				&& (StringUtils.isNotBlank(content))) {
			Log.info("BASE_CHAT_RECORD_SQL_NEW_FORMAT:" + BASE_CHAT_RECORD_SQL_NEW_FORMAT);
			return String.format(BASE_CHAT_RECORD_SQL_NEW_FORMAT,
					new Object[] { createTotalSqlWhere(begainTime_l, endTime_l, parameter), content,
							createTotalSqlWhere(begainTime_l, endTime_l, parameter), content });
		}

		return String.format(BASE_CHAT_RECORD_SQL_OLD_FORMAT,
				new Object[] { createTotalSqlWhere(begainTime_l, endTime_l, parameter),
						createTotalSqlWhere(begainTime_l, endTime_l, parameter) });
	}

	public void destroy() {
		super.destroy();
		AuthCheckFilter.removeExclude("chatprox/grid");
		AuthCheckFilter.removeExclude("chatprox");
	}

	private String replaceUsernameOfSpecialChar(String name) {
		name = name.replace("@", "_");
		name = name.replace("&", "_");
		name = name.replace("%", "_");
		name = name.replace(":","_");
		return name;
	}

	private void replyMessage(String message, HttpServletResponse response, PrintWriter out) {
		response.setContentType("text/json");
		out.println(message);
		out.flush();
	}

	static {
		CALENDAR.set(2016, 1, 28, 23, 59, 59);
		GET_CONVERSATION_ROOM_DATE = CALENDAR.getTime();
		Log.info("GET_CONVERSATION_ROOM_DATE date is {}.", GET_CONVERSATION_ROOM_DATE);
		CALENDAR.clear();
	}
}
