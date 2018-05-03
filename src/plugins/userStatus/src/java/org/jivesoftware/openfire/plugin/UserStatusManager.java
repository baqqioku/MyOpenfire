package org.jivesoftware.openfire.plugin;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.util.ParamUtils;
import org.jivesoftware.util.WebBean;
import org.jivesoftware.util.page.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserStatusManager extends WebBean {
	private static final Logger log = LoggerFactory.getLogger(UserStatusManager.class);
	public static final String MYSQL_SQL = "select * from ( {0}) sel_tab00 limit {1},{2}";
	private static final String OFUSERSTATUS_INFO_QUERY = "SELECT id, ConnectionType , username, resources, online , IpAddress , startTime , logoutTime,createTime,updateTime,todaytime from mll_OfUserStatus where 1=1 ";
	private static final String OFUSERSTATUS_COUT_QUERY = "SELECT count(1) from mll_OfUserStatus where 1=1 ";
	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public Pagination<MllOfUserStatus> query()
			throws ParseException, SQLException, JsonGenerationException, JsonMappingException, IOException {
		MllOfUserStatus entity = new MllOfUserStatus();
		String username = ParamUtils.getParameter(this.request, "username");
		String pageNo = ParamUtils.getParameter(this.request, "pageNo");
		String pageSize = ParamUtils.getParameter(this.request, "pageSize");
		String startDate = ParamUtils.getParameter(this.request, "startDate");
		String endDate = ParamUtils.getParameter(this.request, "endDate");
		if (StringUtils.isNotBlank(startDate)) {
			entity.setCreateTime(Long.valueOf(df.parse(startDate).getTime()));
		}
		if (StringUtils.isNotBlank(endDate)) {
			entity.setLogoutTime(Long.valueOf(df.parse(endDate).getTime()));
		}
		Integer pageNoInteger = Integer.valueOf(1);
		Integer pageSizeInteger = Integer.valueOf(20);
		if (StringUtils.isNotBlank(username)) {
			entity.setUsername(username);
		}
		if (StringUtils.isNotBlank(pageNo)) {
			pageNoInteger = Integer.valueOf(Integer.parseInt(pageNo));
		}
		if (StringUtils.isNotBlank(pageSize)) {
			pageSizeInteger = Integer.valueOf(Integer.parseInt(pageSize));
		}
		return getPage(entity, pageNoInteger.intValue(), pageSizeInteger.intValue());
	}

	public Pagination<MllOfUserStatus> query(HttpServletRequest request)
			throws ParseException, SQLException, JsonGenerationException, JsonMappingException, IOException {
		MllOfUserStatus entity = new MllOfUserStatus();
		String username = ParamUtils.getParameter(request, "username");
		String pageNo = ParamUtils.getParameter(request, "pageNo");
		String pageSize = ParamUtils.getParameter(request, "pageSize");
		String startDate = ParamUtils.getParameter(request, "startDate");
		String endDate = ParamUtils.getParameter(request, "endDate");
		if (StringUtils.isNotBlank(startDate)) {
			entity.setCreateTime(Long.valueOf(df.parse(startDate).getTime()));
		}
		if (StringUtils.isNotBlank(endDate)) {
			entity.setLogoutTime(Long.valueOf(df.parse(endDate).getTime()));
		}
		Integer pageNoInteger = Integer.valueOf(1);
		Integer pageSizeInteger = Integer.valueOf(20);
		if (StringUtils.isNotBlank(username)) {
			entity.setUsername(username);
		}
		if (StringUtils.isNotBlank(pageNo)) {
			pageNoInteger = Integer.valueOf(Integer.parseInt(pageNo));
		}
		if (StringUtils.isNotBlank(pageSize)) {
			pageSizeInteger = Integer.valueOf(Integer.parseInt(pageSize));
		}
		return getPage(entity, pageNoInteger.intValue(), pageSizeInteger.intValue());
	}

	public Pagination<MllOfUserStatus> getPage(MllOfUserStatus entity, int pageNo, int pageSize) {
		Pagination<MllOfUserStatus> pageList = new Pagination<MllOfUserStatus>(pageNo, pageSize, getCount(createCountEntity(entity)).intValue());
		try {
			pageList.setList(queryList(entity, pageNo, pageSize));
		} catch (Exception e) {
			log.error("getPage Exception {}", e.getMessage());
		}
		return pageList;
	}

	public List<MllOfUserStatus> queryList(MllOfUserStatus entity, int pageNo, int pageSize) throws SQLException {
		Connection con = null;
		try {
			con = DbConnectionManager.getConnection();
			String sql = createEntity(entity);
			List<MllOfUserStatus> mllOfUserStatusList = new ArrayList<MllOfUserStatus>();
			QueryRunner runner = new QueryRunner();
			mllOfUserStatusList = (List<MllOfUserStatus>) runner.query(con, createPageSql(sql, pageNo, pageSize),
					new BeanListHandler<MllOfUserStatus>(MllOfUserStatus.class));

			return mllOfUserStatusList;
		} finally {
			DbConnectionManager.closeConnection(con);
		}
	}

	public Integer getCount(String sql) {
		Long topicNum = Long.valueOf(0L);
		QueryRunner runner = new QueryRunner();
		Connection con = null;
		try {
			con = DbConnectionManager.getConnection();
			topicNum = (Long) runner.query(con, sql, new ScalarHandler<Object>());
			log.info("topicNum:{}", topicNum);
			return Integer.valueOf(Integer.parseInt(String.valueOf(topicNum)));
		} catch (Exception e) {
			log.error("getCount Exception {}", e.getMessage());
			return Integer.valueOf(Integer.parseInt(String.valueOf(topicNum)));
		} finally {
			DbConnectionManager.closeConnection(con);
		}
	}

	public String createEntity(MllOfUserStatus entity) {
		String sql = "SELECT id, ConnectionType , username, resources, online , IpAddress , startTime , logoutTime,createTime,updateTime,todaytime from mll_OfUserStatus where 1=1 ";
		if (entity != null) {
			if (StringUtils.isNotBlank(entity.getUsername())) {
				sql = sql + " and username='" + entity.getUsername().trim() + "'";
			}
			if ((entity.getCreateTime() != null) && (entity.getLogoutTime() != null)) {
				sql = sql + "and  startTime between " + entity.getCreateTime() + " and " + entity.getLogoutTime() + "";
			} else if (entity.getCreateTime() != null) {
				sql = sql + "and  startTime> " + entity.getCreateTime() + "";
			}
			sql = sql + " order by id desc";
		}
		return sql;
	}

	public String createCountEntity(MllOfUserStatus entity) {
		String sql = "SELECT count(1) from mll_OfUserStatus where 1=1 ";
		if (entity != null) {
			if (StringUtils.isNotBlank(entity.getUsername())) {
				sql = sql + " and username='" + entity.getUsername().trim() + "'";
			}
			if ((entity.getCreateTime() != null) && (entity.getLogoutTime() != null)) {
				sql = sql + "and  startTime between " + entity.getCreateTime() + " and " + entity.getLogoutTime() + "";
			} else if (entity.getCreateTime() != null) {
				sql = sql + "and  startTime> " + entity.getCreateTime() + "";
			}
		}
		return sql;
	}

	public static String createPageSql(String sql, int pageNo, int pageSize) {
		int beginNum = (pageNo - 1) * pageSize;
		Object[] sqlParam = new String[3];
		sqlParam[0] = sql;
		sqlParam[1] = String.valueOf(beginNum);
		sqlParam[2] = String.valueOf(pageSize);
		sql = MessageFormat.format("select * from ( {0}) sel_tab00 limit {1},{2}", sqlParam);
		return sql;
	}

	public static String transferLongToDate(Long millSec) {
		if (millSec == null) {
			return "";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date(millSec.longValue());
		return sdf.format(date);
	}

	public static String isNull(String str) {
		if (StringUtils.isNotBlank(str)) {
			return str;
		}
		return "";
	}

	public static String getTimeFromLong(String diffstr) {
		Long diff = Long.valueOf(0L);
		try {
			diff = Long.valueOf(Long.parseLong(diffstr));
		} catch (Exception e) {
			return diffstr;
		}
		String HOURS = "h";
		String MINUTES = "min";
		String SECONDS = "sec";
		long MS_IN_A_DAY = 86400000L;
		long MS_IN_AN_HOUR = 3600000L;
		long MS_IN_A_MINUTE = 60000L;
		long MS_IN_A_SECOND = 1000L;
		diff = Long.valueOf(diff.longValue() % 86400000L);
		long numHours = diff.longValue() / 3600000L;
		diff = Long.valueOf(diff.longValue() % 3600000L);
		long numMinutes = diff.longValue() / 60000L;
		diff = Long.valueOf(diff.longValue() % 60000L);
		long numSeconds = diff.longValue() / 1000L;
		diff = Long.valueOf(diff.longValue() % 1000L);
		StringBuilder buf = new StringBuilder();
		if (numHours > 0L) {
			buf.append(numHours).append(' ').append("h").append(", ");
		}
		if (numMinutes > 0L) {
			buf.append(numMinutes).append(' ').append("min").append(' ');
		}
		buf.append(numSeconds + " " + "sec");
		String result = buf.toString();
		return result;
	}

	public static String getOnline(String status) {
		String stus = "not on line";
		if ((StringUtils.isNotBlank(status)) && (status.equals("online"))) {
			stus = "on line";
		}
		return stus;
	}
}
