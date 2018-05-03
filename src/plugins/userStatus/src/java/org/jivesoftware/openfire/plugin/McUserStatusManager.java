package org.jivesoftware.openfire.plugin;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

public class McUserStatusManager extends WebBean {
	private static final Logger log = LoggerFactory.getLogger(McUserStatusManager.class);
	public static final String MYSQL_SQL = "select * from ( {0}) sel_tab00 limit {1},{2}";
	private static final String OFUSERSTATUS_INFO_QUERY = " select shop.username,shop.group_name as resources ,shop.shop_id as ConnectionType ,of.startTime,of.logoutTime,(select online from mll_OfUserStatus where id=of.id) as online ,of.todaytime  from  (select u.username , g.group_name,g.shop_id from  ofUser u   left join mll_kf_group g  on u.kf_group_id=g.id where g.group_type=0) shop left join (select username,min(startTime) as startTime,  max(logoutTime)  as logoutTime ,sum(todaytime) as todaytime,max(id) as id  from mll_OfUserStatus where  1=1  ";
	private static final String OFUSERSTATUS_COUT_QUERY = "select count(1) from  ofUser u  left join mll_kf_group g  on u.kf_group_id=g.id where g.group_type=0 ";
	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	public Pagination<MllOfUserStatus> query()
			throws ParseException, SQLException, JsonGenerationException, JsonMappingException, IOException {
		MllOfUserStatus entity = new MllOfUserStatus();
		String username = ParamUtils.getParameter(this.request, "username");
		String mcusername = ParamUtils.getParameter(this.request, "mcusername");
		String pageNo = ParamUtils.getParameter(this.request, "pageNo");
		String pageSize = ParamUtils.getParameter(this.request, "pageSize");
		String startDate = ParamUtils.getParameter(this.request, "startDate");
		if (StringUtils.isNotBlank(startDate)) {
			entity.setCreateTime(Long.valueOf(df.parse(startDate).getTime()));
		}
		Integer pageNoInteger = Integer.valueOf(1);
		Integer pageSizeInteger = Integer.valueOf(20);
		if (StringUtils.isNotBlank(username)) {
			entity.setUsername(username);
		}
		if (StringUtils.isNotBlank(mcusername)) {
			entity.setResources(mcusername);
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
		Pagination<MllOfUserStatus> pageList = new Pagination<MllOfUserStatus>(pageNo, pageSize,
				getCount(createCountEntity(entity)).intValue());
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
		String sql = " select shop.username,shop.group_name as resources ,shop.shop_id as ConnectionType ,of.startTime,of.logoutTime,(select online from mll_OfUserStatus where id=of.id) as online ,of.todaytime  from  (select u.username , g.group_name,g.shop_id from  ofUser u   left join mll_kf_group g  on u.kf_group_id=g.id where g.group_type=0) shop left join (select username,min(startTime) as startTime,  max(logoutTime)  as logoutTime ,sum(todaytime) as todaytime,max(id) as id  from mll_OfUserStatus where  1=1  ";
		if (entity != null) {
			sql = sql + "and from_unixtime (startTime/1000) > '" + ShopUserStatusManager.currentAddDate(-5)
					+ "' and from_unixtime(startTime/1000) <'" + ShopUserStatusManager.currentAddDate(1) + "' ";
			sql = sql + " group by username ) of on of.username=shop.username where 1=1 ";
			if (StringUtils.isNotBlank(entity.getResources())) {
				sql = sql + " and shop.shop_id  = '" + entity.getResources().trim() + "'";
			}
			sql = sql + " group by shop.username  order by shop_id ";
		}
		log.info("sql:{}", sql);
		return sql;
	}

	public String createCountEntity(MllOfUserStatus entity) {
		String sql = "select count(1) from  ofUser u  left join mll_kf_group g  on u.kf_group_id=g.id where g.group_type=0 ";
		if (entity != null) {
			if (StringUtils.isNotBlank(entity.getUsername())) {
				sql = sql + " and u.username='" + entity.getUsername().trim() + "'";
			}
			if (StringUtils.isNotBlank(entity.getResources())) {
				sql = sql + " and shop.shop_id  = '" + entity.getResources().trim() + "'";
			}
		}
		log.info("sql:{}", sql);
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

	public static String currentLongToDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
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

	public static String currentAddDate(int day) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String createTime = df.format(Long.valueOf(new Date().getTime()));
		Date date;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(createTime);
		} catch (ParseException e) {
			date = new Date();
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(5, day);
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cal.getTime());
	}

	public static String getOnline(String status) {
		String stus = "ont on line";
		if ((!StringUtils.isBlank(status))
				&& ((status.equals("available")) || (status.equals("dnd")) || (status.equals("online")))) {
			stus = "on line";
		}
		return stus;
	}
}
