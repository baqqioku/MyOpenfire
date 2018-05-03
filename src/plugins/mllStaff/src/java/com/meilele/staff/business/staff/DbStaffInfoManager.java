package com.meilele.staff.business.staff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.jetty.util.StringUtil;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserAlreadyExistsException;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.util.page.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

import com.alibaba.fastjson.JSON;
import com.meilele.staff.business.common.DateTools;
import com.meilele.staff.business.common.RequestUtils;

public class DbStaffInfoManager {
	private static final Logger Log = LoggerFactory.getLogger(DbStaffInfoManager.class);
	private static final DbStaffInfoManager CHAT_STAFF_INFO = new DbStaffInfoManager();
	private static final String MYSQL_SQL = "select * from ( {0}) sel_tab00 limit {1},{2}";
	private static final String MYSQL_SQL_COUNT = "select count(1) from ( {0}) sel_tab00";
	private static final String STAFF_INFO_FIND_BY_ID = "select * from store_staff_info  where id = ?";
	private static final String STAFF_INFO_REMOVE = "delete from store_staff_info where id=?";
	private static final String STAFF_INFO_INSERT = "INSERT INTO store_staff_info( id,sys_uuid,mch_uuid,store_uuid,store_name,staff_name,staff_phone,password,rol_uuid,rol_name,staff_type,create_id,create_date,update_id,update_date,code_id,code_image,staff_sig,staff_head,staff_label,app_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String STAFF_INFO_QUERY = "SELECT id,sys_uuid,mch_uuid,store_uuid,store_name,staff_name,staff_phone,password,rol_uuid,rol_name,staff_type,create_id,create_date,update_id,update_date,code_id,code_image,staff_sig,staff_head,staff_label FROM store_staff_info where 1=1 ";
	private static final String STAFF_INFO_UPDATE = "UPDATE store_staff_info set sys_uuid=?,mch_uuid=?,store_uuid=?,store_name=?,staff_name=?,staff_phone=?,password=?,rol_uuid=?,rol_name=?,staff_type=?,create_id=?,create_date=?,update_id=?,update_date=?,code_id=?,code_image=?,staff_sig=?,staff_head=?,staff_label=? where id=? ";

	public static DbStaffInfoManager getInstance() {
		return CHAT_STAFF_INFO;
	}

	public boolean add(StaffInfo staffInfo) {
		if (staffInfo == null) {
			return false;
		}
		User user = addOpUserAndInsertProp(staffInfo);
		if (!checkAddOpUserSuccess(user)) {
			return false;
		}
		boolean addStaffRes = addStaff(staffInfo);
		if (!addStaffRes) {
			try {
				deleteOpUser(user);
			} catch (Exception e) {
				Log.error("delete openfire user failed of username " + user.getUsername(), e);
			}
			return false;
		}
		return true;
	}

	private boolean addStaff(StaffInfo staffInfo) {
		Log.info("调用新增导购方法 staffInfo={}", staffInfo);
		Connection con = null;
		try {
			QueryRunner qr = new QueryRunner();
			con = DbConnectionManager.getConnection();
			List<Object> list;
			list = new ArrayList<Object>();
			list.add(staffInfo.getId());
			list.add(staffInfo.getSysUuid());
			list.add(staffInfo.getMchUuid());
			list.add(staffInfo.getStoreUuid());
			list.add(staffInfo.getStoreName());
			list.add(staffInfo.getStaffName());
			list.add(staffInfo.getStaffPhone());
			list.add(staffInfo.getPassword());
			list.add(staffInfo.getRolUuid());
			list.add(staffInfo.getRolName());
			list.add(staffInfo.getStaffType());
			list.add(staffInfo.getCreateId());
			list.add(new Timestamp(DateTools.getTime(staffInfo.getCreateDate(), "yyyy-MM-dd HH:mm:ss").getTime()));
			list.add(staffInfo.getUpdateId());
			list.add(new Timestamp(DateTools.getTime(staffInfo.getUpdateDate(), "yyyy-MM-dd HH:mm:ss").getTime()));
			list.add(staffInfo.getCodeId());
			list.add(staffInfo.getCodeImage());
			list.add(staffInfo.getStaffSig());
			list.add(staffInfo.getStaffHead());
			list.add(staffInfo.getStaffLabel());
			list.add(staffInfo.getAppID());
			qr.update(con, "INSERT INTO store_staff_info( id,sys_uuid,mch_uuid,store_uuid,store_name,staff_name,staff_phone,password,rol_uuid,rol_name,staff_type,create_id,create_date,update_id,update_date,code_id,code_image,staff_sig,staff_head,staff_label,app_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", list.toArray());
			return true;
		} catch (SQLException sqle) {
			Log.error("StaffInfo add exception: {}", sqle);
			return false;
		} finally {
			DbConnectionManager.closeConnection(con);
		}
	}

	private User addOpUserAndInsertProp(StaffInfo staffInfo) {
		Log.info("addOpUserAndInsertProp : {}", JSON.toJSONString(staffInfo));
		UserManager userManager = XMPPServer.getInstance().getUserManager();
		User user = null;
		try {
			user = userManager.createUser(staffInfo.getSysUuid(), staffInfo.getPassword(), staffInfo.getStaffName(), "");
			//Log.info("user : {}", JSON.toJSONString(user));
			if (!insertStaffProp(user)) {
				XMPPServer.getInstance().getUserManager().deleteUser(user);
				Log.error("Insert user : {} staff prop failed!");
				return null;
			}
		} catch (UserAlreadyExistsException e) {
			Log.warn("openfire user already exist of staff sys_uuid:{}, staff phone : {}", staffInfo.getSysUuid(), staffInfo.getStaffPhone());
		} catch (Exception e) {
			Log.error("create openfire user failed of staff sys_uuid:{}, staff phone {},exception :{} ", new Object[] { staffInfo.getSysUuid(), staffInfo.getStaffPhone(), e });
		}
		return user;
	}

	private boolean insertStaffProp(User user) {
		Log.info("insert staff prop : {}", user.getUsername());
		user.getProperties().put("mllUser_type", "staff");
		if ("staff".equals(User.getPropertyValue(user.getUsername(), "mllUser_type"))) {
			return true;
		}
		return false;
	}

	private boolean checkAddOpUserSuccess(User user) {
		return user != null;
	}

	private void deleteOpUser(User user) {
		Log.info("delete openfire user : {}", user.getUsername());
		XMPPServer.getInstance().getUserManager().deleteUser(user);
	}

	public StaffInfo find(int id) {
		Connection con = null;
		PreparedStatement pstmt = null;
		StaffInfo staffInfo = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement("select * from store_staff_info  where id = ?");
			pstmt.setInt(1, id);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				staffInfo = (StaffInfo) RequestUtils.toJavaResultBean(rs, staffInfo, StaffInfo.class);
			}
			return staffInfo;
		} catch (SQLException sqle) {
			Log.error("StaffInfo find exception: {}", sqle);
			return staffInfo;
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
	}

	public boolean update(StaffInfo staffInfo) throws SQLException {
		if ((staffInfo == null) || (StringUtils.isBlank(staffInfo.getSysUuid()))) {
			Log.error("update staff info failed of raw not keywords ");
			return false;
		}
		StaffInfo existStaffInfo = querySysUuid(staffInfo.getSysUuid());
		if (existStaffInfo == null) {
			return false;
		}
		Log.info("update staff,exist staffinfo : {},update staffinfo : {}", JSON.toJSONString(existStaffInfo), JSON.toJSONString(staffInfo));
		if (needUpdateOpUserUsername(existStaffInfo, staffInfo)) {
			if (XMPPServer.getInstance().getUserManager().isRegisteredUser(staffInfo.getSysUuid())) {
				Log.info("update staff info sysUuid already exist : {}", staffInfo.getSysUuid());

				return false;
			}
		}
		boolean needUpdateOpUser = false;
		if (needUpdateOpUser(existStaffInfo, staffInfo)) {
			needUpdateOpUser = true;
			try {
				User existOpUser = XMPPServer.getInstance().getUserManager().getUser(existStaffInfo.getSysUuid());
				if (existOpUser == null) {
					return false;
				}
				deleteOpUser(existOpUser);

				staffInfo = (StaffInfo) RequestUtils.toPoToBean(staffInfo, existStaffInfo, StaffInfo.class);
				addOpUserAndInsertProp(staffInfo);
			} catch (Exception e) {
				Log.error("update staff info failed of raw staff uuid " + existStaffInfo.getSysUuid(), e);

				return false;
			}
		}
		if (!needUpdateOpUser) {
			staffInfo = (StaffInfo) RequestUtils.toPoToBean(staffInfo, existStaffInfo, StaffInfo.class);
		}
		return updateStaff(staffInfo);
	}

	private boolean needUpdateOpUser(StaffInfo existStaffInfo, StaffInfo updateStaffInfo) {
		return ((StringUtils.isNotBlank(updateStaffInfo.getSysUuid())) && (!updateStaffInfo.getSysUuid().equals(existStaffInfo.getSysUuid()))) || ((StringUtils.isNotBlank(updateStaffInfo.getPassword())) && (!updateStaffInfo.getPassword().equals(existStaffInfo.getPassword()))) || ((StringUtils.isNotBlank(updateStaffInfo.getStaffName())) && (!updateStaffInfo.getStaffName().equals(existStaffInfo.getStaffName())));
	}

	private boolean needUpdateOpUserUsername(StaffInfo existStaffInfo, StaffInfo updateStaffInfo) {
		return (StringUtils.isNotBlank(updateStaffInfo.getSysUuid())) && (!updateStaffInfo.getSysUuid().equals(existStaffInfo.getSysUuid()));
	}

	private boolean updateStaff(StaffInfo staffInfo) {
		Connection con = null;
		try {
			QueryRunner qr = new QueryRunner();
			con = DbConnectionManager.getConnection();
			List<Object> list;
			list = new ArrayList<Object>();
			list.add(staffInfo.getSysUuid());
			list.add(staffInfo.getMchUuid());
			list.add(staffInfo.getStoreUuid());
			list.add(staffInfo.getStoreName());
			list.add(staffInfo.getStaffName());
			list.add(staffInfo.getStaffPhone());
			list.add(staffInfo.getPassword());
			list.add(staffInfo.getRolUuid());
			list.add(staffInfo.getRolName());
			list.add(staffInfo.getStaffType());
			list.add(staffInfo.getCreateId());
			list.add(new Timestamp(DateTools.getTime(staffInfo.getCreateDate(), "yyyy-MM-dd HH:mm:ss").getTime()));
			list.add(staffInfo.getUpdateId());
			list.add(new Timestamp(DateTools.getTime(staffInfo.getUpdateDate(), "yyyy-MM-dd HH:mm:ss").getTime()));
			list.add(staffInfo.getCodeId());
			list.add(staffInfo.getCodeImage());
			list.add(staffInfo.getStaffSig());
			list.add(staffInfo.getStaffHead());
			list.add(staffInfo.getStaffLabel());
			list.add(staffInfo.getId());
			qr.update(con, "UPDATE store_staff_info set sys_uuid=?,mch_uuid=?,store_uuid=?,store_name=?,staff_name=?,staff_phone=?,password=?,rol_uuid=?,rol_name=?,staff_type=?,create_id=?,create_date=?,update_id=?,update_date=?,code_id=?,code_image=?,staff_sig=?,staff_head=?,staff_label=? where id=? ", list.toArray());
			return true;
		} catch (SQLException sqle) {

			Log.error("StaffInfo update exception: {}", sqle);
			return false;
		} finally {
			DbConnectionManager.closeConnection(con);
		}
	}

	public boolean delById(String id) {
		int idInt = NumberUtils.toInt(id);
		if (idInt <= 0) {
			Log.error("invalid param in delete staff info by Id : {}", id);
			return false;
		}
		StaffInfo existStaffInfo = find(idInt);
		if (existStaffInfo == null) {
			Log.error("cannot find to be deleted staff of Id : {}", id);
			return false;
		}
		try {
			User user = XMPPServer.getInstance().getUserManager().getUser(existStaffInfo.getSysUuid());
			if (user == null) {
				Log.error("delete staff info cannot find openfire user of staff phone : {}", existStaffInfo.getSysUuid());

				return false;
			}
			deleteOpUser(user);
		} catch (Exception e) {
			Log.error("delete staff info failed of raw staff uuid " + existStaffInfo.getSysUuid(), e);

			return false;
		}
		return deleteStaffById(id);
	}

	private boolean deleteStaffById(String id) {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement("delete from store_staff_info where id=?");
			pstmt.setString(1, id);
			pstmt.execute();
			return true;
		} catch (SQLException sqle) {
			Log.error("StaffInfo remove exception: {}", sqle);
			return false;
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
	}

	public JID getStaff(String phone) {
		return new JID(phone, XMPPServer.getInstance().getServerInfo().getXMPPDomain(), phone);
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
			if (rs.next()) {
				count = rs.getInt(1);
			} else {
				count = 0;
			}
		} catch (SQLException sqle) {
			Log.error(sqle.getMessage(), sqle);
			return 0;
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
		return count;
	}

	public String createEntity(StaffInfo entity) {
		String sql = "SELECT id,sys_uuid,mch_uuid,store_uuid,store_name,staff_name,staff_phone,password,rol_uuid,rol_name,staff_type,create_id,create_date,update_id,update_date,code_id,code_image,staff_sig,staff_head,staff_label FROM store_staff_info where 1=1 ";
		if (entity != null) {
			if (entity.getCreateDate() != null) {
				sql = sql + " and create_date like '" + entity.getCreateDate() + "%'";
			}
			if (StringUtil.isNotBlank(entity.getStoreUuid())) {
				sql = sql + " and store_uuid = '" + entity.getStoreUuid() + "'";
			}
			if (StringUtil.isNotBlank(entity.getStaffPhone())) {
				sql = sql + " and staff_phone = '" + entity.getStaffPhone() + "'";
			}
			if (StringUtil.isNotBlank(entity.getSysUuid())) {
				sql = sql + " and sys_uuid = '" + entity.getSysUuid() + "'";
			}
			if (StringUtil.isNotBlank(entity.getStaffType())) {
				sql = sql + " and staff_type !='3'";
			}
		}
		sql = sql + " order by create_date asc";
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

	public static String createCountPageSql(String sql) {
		Object[] sqlParam = new String[1];
		sqlParam[0] = sql;
		sql = MessageFormat.format("select count(1) from ( {0}) sel_tab00", sqlParam);
		return sql;
	}

	public List<StaffInfo> queryList(String storeUuid) throws SQLException {
		StaffInfo entity = new StaffInfo();
		entity.setStoreUuid(storeUuid);
		entity.setStaffType("2");
		return queryList(entity);
	}

	public StaffInfo queryStaffPhones(String staffPhone) throws SQLException {
		StaffInfo entity = new StaffInfo();
		entity.setStaffPhone(staffPhone);
		List<StaffInfo> staffInfoList = queryList(entity);
		if ((staffInfoList == null) || (staffInfoList.size() < 1)) {
			return null;
		}
		return (StaffInfo) staffInfoList.get(0);
	}

	public StaffInfo querySysUuid(String sysUuid) throws SQLException {
		StaffInfo entity = new StaffInfo();
		entity.setSysUuid(sysUuid);
		List<StaffInfo> staffInfoList = queryList(entity);
		if ((staffInfoList == null) || (staffInfoList.size() < 1)) {
			return null;
		}
		return (StaffInfo) staffInfoList.get(0);
	}

	public List<StaffInfo> queryList(StaffInfo entity) throws SQLException {
		Connection con = null;
		Statement pstmt = null;
		StaffInfo staffInfo = null;
		List<StaffInfo> result = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.createStatement();
			String sql = createEntity(entity);
			Log.info("db staff info query list sql : {}", sql);
			ResultSet rs = pstmt.executeQuery(sql);
			result = new ArrayList<StaffInfo>(rs.getRow());
			while (rs.next()) {
				staffInfo = new StaffInfo();
				staffInfo = (StaffInfo) RequestUtils.toJavaResultBean(rs, staffInfo, StaffInfo.class);
				result.add(staffInfo);
			}
			return result;
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
	}

	public List<StaffInfo> queryListPage(StaffInfo entity, int pageNo, int pageSize) throws SQLException {
		Connection con = null;
		Statement pstmt = null;
		StaffInfo staffInfo = null;
		List<StaffInfo> result = new ArrayList<StaffInfo>();
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.createStatement();
			String sql = createEntity(entity);
			ResultSet rs = pstmt.executeQuery(createPageSql(sql, pageNo, pageSize));
			result = new ArrayList<StaffInfo>(rs.getRow());
			while (rs.next()) {
				staffInfo = new StaffInfo();
				staffInfo = (StaffInfo) RequestUtils.toJavaResultBean(rs, staffInfo, StaffInfo.class);
				result.add(staffInfo);
			}
			return result;
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
	}

	public Pagination<StaffInfo> getPage(StaffInfo entity, int pageNo, int pageSize) throws SQLException {
		int totalCount = getCount(createCountPageSql(createEntity(entity)));
		Pagination<StaffInfo> pageList = new Pagination<StaffInfo>(pageNo, pageSize, totalCount);
		pageList.setList(queryListPage(entity, pageNo, pageSize));
		return pageList;
	}
}

/*
 * Location:
 * C:\Users\Administrator\Desktop\openfire\plugins\mllStaff\lib\mllStaff-lib.jar
 * 
 * Qualified Name: com.meilele.staff.business.staff.DbStaffInfoManager
 * 
 * JD-Core Version: 0.7.0.1
 * 
 */