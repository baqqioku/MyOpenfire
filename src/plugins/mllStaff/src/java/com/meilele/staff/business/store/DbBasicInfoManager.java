package com.meilele.staff.business.store;

import com.meilele.staff.business.common.DateTools;
import com.meilele.staff.business.common.RequestUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang.StringUtils;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.util.page.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbBasicInfoManager {
	private static final Logger Log = LoggerFactory.getLogger(DbBasicInfoManager.class);
	private static final DbBasicInfoManager BASIC_INFO_MANAGER = new DbBasicInfoManager();
	private static final int COUNTRY_PROVINCE = 1;
	private static final int COUNTRY_CITY = 2;
	private static final int COUNTRY_AREA = 3;
	private static Map<Integer, String> BASIC_PROVINCE_MAP;
	private static Map<Integer, String> BASIC_CITY_MAP;
	private static Map<Integer, String> BASIC_AREA_MAP;
	private static final String SQL_FIELD = " id,sys_uuid,landed_uuid,store_name,store_type ,store_id ,store_higher,store_status,store_phone,contact_staff,father_id,father_name,scope_id,scope_name,store_logo,province_id,store_province,city_id,store_city,area_id,store_area,street_id,store_street,store_address,longitude,latitude,create_id,create_date,update_id,update_date ";
	private static final String MYSQL_SQL = "select * from ( {0}) sel_tab00 limit {1},{2}";
	private static final String MYSQL_SQL_COUNT = "select count(1) from ( {0}) sel_tab00";
	private static final String BASIC_INFO_QUERE = "select  id,sys_uuid,landed_uuid,store_name,store_type ,store_id ,store_higher,store_status,store_phone,contact_staff,father_id,father_name,scope_id,scope_name,store_logo,province_id,store_province,city_id,store_city,area_id,store_area,street_id,store_street,store_address,longitude,latitude,create_id,create_date,update_id,update_date  from store_basic_info where 1=1 ";
	private static final String BASIC_INFO_QUERE_STAFFINFO = "select  id,sys_uuid,landed_uuid,store_name,store_type ,store_id ,store_higher,store_status,store_phone,contact_staff,father_id,father_name,scope_id,scope_name,store_logo,province_id,store_province,city_id,store_city,area_id,store_area,street_id,store_street,store_address,longitude,latitude,create_id,create_date,update_id,update_date  from store_basic_info where sys_uuid = (select store_uuid from store_staff_info where sys_uuid=?)";
	private static final String BASIC_INFO_REMOVE = "delete from store_basic_info where id=?";
	private static final String BASIC_INFO_INSERT = "insert into store_basic_info( id,sys_uuid,landed_uuid,store_name,store_type ,store_id ,store_higher,store_status,store_phone,contact_staff,father_id,father_name,scope_id,scope_name,store_logo,province_id,store_province,city_id,store_city,area_id,store_area,street_id,store_street,store_address,longitude,latitude,create_id,create_date,update_id,update_date ) VALUES( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	private static final String BASIC_INFO_BYIDUPDTE = "UPDATE store_basic_info set sys_uuid=?,landed_uuid=?,store_name=?,store_type=?,store_id=?,store_higher=?,store_status=?,store_phone=?,contact_staff=?,father_id=?,father_name=?,scope_id=?,scope_name=?,store_logo=?,province_id=?,store_province=?,city_id=?,store_city=?,area_id=?,store_area=?,street_id=?,store_street=?,store_address=?,longitude=?,latitude=?,create_id=?,create_date=?,update_id=?,update_date=? where id=? ";
	private static final String BASIC_PROVINCE = "select * from ( select c.name,c.id  from circle c where c.LEVEL=2 ) c,(select distinct(m.scountryName) countryName  from mll_city m) cc where c.name  like CONCAT( cc.countryName, '%')";
	private static final String BASIC_CITY = "select * from ( select c.name,c.id  from circle c where c.`LEVEL`=3 ) c,(select distinct(m.countryName) countryName  from mll_city m) cc where c.name  like CONCAT( cc.countryName, '%')";
	private static final String BASIC_AREA = "select * from ( select c.name,c.id  from circle c where c.LEVEL=4 ) c,(select distinct(m.qcountryName) countryName   from mll_city m) cc where c.name  like CONCAT( cc.countryName, '%')";

	private DbBasicInfoManager() {
		BASIC_PROVINCE_MAP = getCountry(1);
		BASIC_CITY_MAP = getCountry(2);
		BASIC_AREA_MAP = getCountry(3);
	}

	public static DbBasicInfoManager getInstance() {
		return BASIC_INFO_MANAGER;
	}

	public boolean add(BasicInfo basicInfo) {
		Connection con = null;
		if (basicInfo == null) {
			return false;
		}
		try {
			QueryRunner qr = new QueryRunner();
			con = DbConnectionManager.getConnection();
			List<Object> list;
			list = new ArrayList<Object>();
			list.add(basicInfo.getId());
			list.add(basicInfo.getSysUuid());
			list.add(basicInfo.getLandedUuid());
			list.add(basicInfo.getStoreName());
			list.add(basicInfo.getStoreType());
			list.add(Integer.valueOf(basicInfo.getStoreId() == null ? 0 : basicInfo.getStoreId().intValue()));
			list.add(basicInfo.getStoreHigher());
			list.add(basicInfo.getStoreStatus());
			list.add(basicInfo.getStorePhone());
			list.add(basicInfo.getContactStaff());
			list.add(basicInfo.getFatherId());
			list.add(basicInfo.getFatherName());
			list.add(basicInfo.getScopeId());
			list.add(basicInfo.getScopeName());
			list.add(basicInfo.getStoreLogo());
			list.add(Integer.valueOf(basicInfo.getProvinceId() == null ? 0 : basicInfo.getProvinceId().intValue()));
			list.add(basicInfo.getStoreProvince());
			list.add(Integer.valueOf(basicInfo.getCityId() == null ? 0 : basicInfo.getCityId().intValue()));
			list.add(basicInfo.getStoreCity());
			list.add(Integer.valueOf(basicInfo.getAreaId() == null ? 0 : basicInfo.getAreaId().intValue()));
			list.add(basicInfo.getStoreArea());
			list.add(Integer.valueOf(basicInfo.getStreetId() == null ? 0 : basicInfo.getStreetId().intValue()));
			list.add(basicInfo.getStoreStreet());
			list.add(basicInfo.getStoreAddress());
			list.add(basicInfo.getLongitude());
			list.add(basicInfo.getLatitude());
			list.add(basicInfo.getCreateId());
			list.add(new Timestamp(DateTools.getTime(basicInfo.getCreateDate(), "yyyy-MM-dd HH:mm:ss").getTime()));
			list.add(basicInfo.getUpdateId());
			list.add(new Timestamp(DateTools.getTime(basicInfo.getUpdateDate(), "yyyy-MM-dd HH:mm:ss").getTime()));
			qr.update(con,
					"insert into store_basic_info( id,sys_uuid,landed_uuid,store_name,store_type ,store_id ,store_higher,store_status,store_phone,contact_staff,father_id,father_name,scope_id,scope_name,store_logo,province_id,store_province,city_id,store_city,area_id,store_area,street_id,store_street,store_address,longitude,latitude,create_id,create_date,update_id,update_date ) VALUES( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
					list.toArray());
			return true;
		} catch (SQLException sqle) {

			Log.error("BasicInfo add exception: {}", sqle);
			return false;
		} finally {
			DbConnectionManager.closeConnection(con);
		}
	}

	private static Map<Integer, String> getCountry(int id) {
		Connection con = null;
		PreparedStatement pstmt = null;
		Map<Integer, String> map = new HashMap<Integer, String>();
		ResultSet rs = null;
		try {
			con = DbConnectionManager.getConnection();
			String sql = "";
			if (id == 1) {
				sql = "select * from ( select c.name,c.id  from circle c where c.LEVEL=2 ) c,(select distinct(m.scountryName) countryName  from mll_city m) cc where c.name  like CONCAT( cc.countryName, '%')";
			} else if (id == 2) {
				sql = "select * from ( select c.name,c.id  from circle c where c.`LEVEL`=3 ) c,(select distinct(m.countryName) countryName  from mll_city m) cc where c.name  like CONCAT( cc.countryName, '%')";
			} else if (id == 3) {
				sql = "select * from ( select c.name,c.id  from circle c where c.LEVEL=4 ) c,(select distinct(m.qcountryName) countryName   from mll_city m) cc where c.name  like CONCAT( cc.countryName, '%')";
			} else {
				return new HashMap<Integer, String>();
			}
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (((ResultSet) rs).next()) {
				map.put(Integer.valueOf(((ResultSet) rs).getInt("id")), ((ResultSet) rs).getString("countryName"));
			}
			return map;
		} catch (SQLException sqle) {

			Log.error("cicle find exception: {}", sqle);
			return map;
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
	}

	public boolean update(BasicInfo newbasicInfo) throws SQLException {
		BasicInfo basicInfo = querySysUuid(newbasicInfo.getSysUuid());
		Connection con = null;
		try {
			if (basicInfo == null) {
				return false;
			}
			basicInfo = (BasicInfo) RequestUtils.toPoToBean(newbasicInfo, basicInfo, BasicInfo.class);
			QueryRunner qr = new QueryRunner();
			con = DbConnectionManager.getConnection();
			List<Object> list;
			list = new ArrayList<Object>();
			list.add(basicInfo.getSysUuid());
			list.add(basicInfo.getLandedUuid());
			list.add(basicInfo.getStoreName());
			list.add(basicInfo.getStoreType());
			list.add(Integer.valueOf(basicInfo.getStoreId() == null ? 0 : basicInfo.getStoreId().intValue()));
			list.add(basicInfo.getStoreHigher());
			list.add(basicInfo.getStoreStatus());
			list.add(basicInfo.getStorePhone());
			list.add(basicInfo.getContactStaff());
			list.add(basicInfo.getFatherId());
			list.add(basicInfo.getFatherName());
			list.add(basicInfo.getScopeId());
			list.add(basicInfo.getScopeName());
			list.add(basicInfo.getStoreLogo());
			list.add(Integer.valueOf(basicInfo.getProvinceId() == null ? 0 : basicInfo.getProvinceId().intValue()));
			list.add(basicInfo.getStoreProvince());
			list.add(Integer.valueOf(basicInfo.getCityId() == null ? 0 : basicInfo.getCityId().intValue()));
			list.add(basicInfo.getStoreCity());
			list.add(Integer.valueOf(basicInfo.getAreaId() == null ? 0 : basicInfo.getAreaId().intValue()));
			list.add(basicInfo.getStoreArea());
			list.add(Integer.valueOf(basicInfo.getStreetId() == null ? 0 : basicInfo.getStreetId().intValue()));
			list.add(basicInfo.getStoreStreet());
			list.add(basicInfo.getStoreAddress());
			list.add(basicInfo.getLongitude());
			list.add(basicInfo.getLatitude());
			list.add(basicInfo.getCreateId());
			list.add(new Timestamp(DateTools.getTime(basicInfo.getCreateDate(), "yyyy-MM-dd HH:mm:ss").getTime()));
			list.add(basicInfo.getUpdateId());
			list.add(new Timestamp(DateTools.getTime(basicInfo.getUpdateDate(), "yyyy-MM-dd HH:mm:ss").getTime()));
			list.add(basicInfo.getId());
			qr.update(con,
					"UPDATE store_basic_info set sys_uuid=?,landed_uuid=?,store_name=?,store_type=?,store_id=?,store_higher=?,store_status=?,store_phone=?,contact_staff=?,father_id=?,father_name=?,scope_id=?,scope_name=?,store_logo=?,province_id=?,store_province=?,city_id=?,store_city=?,area_id=?,store_area=?,street_id=?,store_street=?,store_address=?,longitude=?,latitude=?,create_id=?,create_date=?,update_id=?,update_date=? where id=? ",
					list.toArray());
			return true;
		} catch (SQLException sqle) {

			Log.error("basicInfo update exception: {}", sqle);
			return false;
		} finally {
			DbConnectionManager.closeConnection(con);
		}
	}

	public boolean delById(String id) {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement("delete from store_basic_info where id=?");
			pstmt.setString(1, id);
			return pstmt.execute();
		} catch (SQLException sqle) {
			Log.error("BasicInfo remove exception: {}", sqle);
			return false;
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
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

	public BasicInfo querySysUuid(String sysUuid) throws SQLException {
		BasicInfo entity = new BasicInfo();
		entity.setSysUuid(sysUuid);
		List<BasicInfo> basicInfoList = queryList(entity);
		if ((basicInfoList == null) || (basicInfoList.size() < 1)) {
			return null;
		}
		return (BasicInfo) basicInfoList.get(0);
	}

	public String querySysStaffInfoUuid(String sysUuid) {
		Connection con = null;
		PreparedStatement pstmt = null;
		BasicInfo basicInfo = null;
		String landedUuid = null;
		if (StringUtils.isBlank(sysUuid)) {
			return landedUuid;
		}
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement(
					"select  id,sys_uuid,landed_uuid,store_name,store_type ,store_id ,store_higher,store_status,store_phone,contact_staff,father_id,father_name,scope_id,scope_name,store_logo,province_id,store_province,city_id,store_city,area_id,store_area,street_id,store_street,store_address,longitude,latitude,create_id,create_date,update_id,update_date  from store_basic_info where sys_uuid = (select store_uuid from store_staff_info where sys_uuid=?)");
			pstmt.setString(1, sysUuid);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				basicInfo = new BasicInfo();
				basicInfo = (BasicInfo) RequestUtils.toJavaResultBean(rs, basicInfo, BasicInfo.class);
			}
			if (basicInfo != null) {
				landedUuid = basicInfo.getLandedUuid();
			}
			return landedUuid;
		} catch (SQLException sqle) {
			Log.error("BasicInfo find exception: {}", sqle);
			return landedUuid;
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
	}

	public boolean querySysUuidStatus(String sysUuid) throws SQLException {
		if (StringUtils.isBlank(sysUuid)) {
			return false;
		}
		BasicInfo entity = new BasicInfo();
		entity.setSysUuid(sysUuid);
		entity.setStoreStatus("已经开业");
		List<BasicInfo> basicInfoList = queryList(entity);
		if ((basicInfoList == null) || (basicInfoList.size() < 1)) {
			return false;
		}
		return true;
	}

	public List<BasicInfo> queryList(BasicInfo entity) throws SQLException {
		Connection con = null;
		Statement pstmt = null;
		BasicInfo basicInfo = null;
		List<BasicInfo> result = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.createStatement();
			String sql = createEntity(entity);
			Log.info("db basic info query sql : {}", sql);
			ResultSet rs = pstmt.executeQuery(sql);
			result = new ArrayList<BasicInfo>(rs.getRow());
			while (rs.next()) {
				basicInfo = new BasicInfo();
				basicInfo = (BasicInfo) RequestUtils.toJavaResultBean(rs, basicInfo, BasicInfo.class);
				result.add(basicInfo);
			}
			return result;
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
	}

	public String createEntity(BasicInfo entity) {
		String sql = "select  id,sys_uuid,landed_uuid,store_name,store_type ,store_id ,store_higher,store_status,store_phone,contact_staff,father_id,father_name,scope_id,scope_name,store_logo,province_id,store_province,city_id,store_city,area_id,store_area,street_id,store_street,store_address,longitude,latitude,create_id,create_date,update_id,update_date  from store_basic_info where 1=1 ";
		if (entity != null) {
			if (entity.getCreateDate() != null) {
				sql = sql + " and create_date like '" + entity.getCreateDate() + "%'";
			}
			if (StringUtils.isNotBlank(entity.getSysUuid())) {
				sql = sql + " and sys_uuid = '" + entity.getSysUuid() + "'";
			}
			if (StringUtils.isNotBlank(entity.getStoreStatus())) {
				sql = sql + " and store_status = '" + entity.getStoreStatus() + "'";
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

	public List<BasicInfo> queryList(BasicInfo entity, int pageNo, int pageSize) throws SQLException {
		Connection con = null;
		Statement pstmt = null;
		BasicInfo basicInfo = null;
		List<BasicInfo> result = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.createStatement();
			String sql = createEntity(entity);
			ResultSet rs = pstmt.executeQuery(createPageSql(sql, pageNo, pageSize));
			result = new ArrayList<BasicInfo>(rs.getRow());
			while (rs.next()) {
				basicInfo = new BasicInfo();
				basicInfo = (BasicInfo) RequestUtils.toJavaResultBean(rs, basicInfo, BasicInfo.class);
				if ((basicInfo.getProvinceId() != null)
						&& (StringUtils.isNotBlank((String) BASIC_PROVINCE_MAP.get(basicInfo.getProvinceId())))) {
					basicInfo.setStoreProvince((String) BASIC_PROVINCE_MAP.get(basicInfo.getProvinceId()));
				}
				if ((basicInfo.getCityId() != null)
						&& (StringUtils.isNotBlank((String) BASIC_CITY_MAP.get(basicInfo.getCityId())))) {
					basicInfo.setStoreCity((String) BASIC_CITY_MAP.get(basicInfo.getCityId()));
				}
				if ((basicInfo.getAreaId() != null)
						&& (StringUtils.isNotBlank((String) BASIC_AREA_MAP.get(basicInfo.getAreaId())))) {
					basicInfo.setStoreArea((String) BASIC_AREA_MAP.get(basicInfo.getAreaId()));
				}
				result.add(basicInfo);
			}
			return result;
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
	}

	public Pagination<BasicInfo> getPage(BasicInfo entity, int pageNo, int pageSize) throws SQLException {
		int totalCount = getCount(createCountPageSql(createEntity(entity)));
		Pagination<BasicInfo> pageList = new Pagination<BasicInfo>(pageNo, pageSize, totalCount);
		pageList.setList(queryList(entity, pageNo, pageSize));
		return pageList;
	}
}
