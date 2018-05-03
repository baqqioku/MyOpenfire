package com.meilele.staff.openfire.servlet;

import com.meilele.staff.business.common.JSONUtil;
import com.meilele.staff.business.common.OperationInfo;
import com.meilele.staff.business.common.OperationStatus;
import com.meilele.staff.business.staff.DbStaffInfoManager;
import com.meilele.staff.business.staff.StaffInfo;
import com.meilele.staff.business.staff.StaffInfoList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jivesoftware.admin.AuthCheckFilter;
import org.jivesoftware.util.ParamUtils;
import org.jivesoftware.util.page.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaffInfoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger Log = LoggerFactory.getLogger(StaffInfoServlet.class);
	private static final String CHARACTER_ENCODING = "utf-8";
	private static final String AUTH_CHECK_PATH = "mllstaff";
	private static final String AUTH_CHECK_STAFFINFO = "mllstaff/staffinfo";
	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private static final ObjectMapper mapper = new ObjectMapper();
	private static DbStaffInfoManager staffInfoManager; 
	private static final String REQUEST_ACTION = "action";
	private static final String REQUEST_CALLBACK = "callback";

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		staffInfoManager = DbStaffInfoManager.getInstance();
		Log.info("StaffInfoServlet add exclude!");
		AuthCheckFilter.addExclude(AUTH_CHECK_STAFFINFO);
		AuthCheckFilter.addExclude(AUTH_CHECK_PATH);
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
		String handleResult = "";
		try {
			handleResult = handleRequest(request);
		} catch (Exception ex) {
			Log.error("ERROR:", ex);
			OperationInfo info = new OperationInfo();
			StringWriter writer = new StringWriter();
			info.setStatus(OperationStatus.OPERATION_FAIL.toString());
			info.setMessage(ex.getMessage());
			mapper.writeValue(writer, info);
			handleResult = writer.toString();
		} finally {
			replyMessage(handleResult, response, out);
		}
	}

	private String handleRequest(HttpServletRequest request) throws Exception {
		Log.info("进入导购新方法............");
		String action = request.getParameter("action");
		String handleResult = "";
		switch (com.meilele.staff.business.common.Action.fromAction(action)) {
		case ACTION_ADD:
			handleResult = add(request);
			break;
		case ACTION_DELETE:
			handleResult = delete(request);
			break;
		case ACTION_UPDATE:
			handleResult = update(request);
			break;
		case ACTION_BATCHADD:
			handleResult = addbetch(request);
			break;
		default:
			handleResult = query(request);
		}
		String callback = request.getParameter("callback");
		if (StringUtils.isNotBlank(callback)) {
			handleResult = callback + "(" + handleResult + ")";
		}
		Log.info("staff handleResult={}, action={}, callback={}", new Object[] { handleResult, action, callback });
		return handleResult;
	}

	protected String query(HttpServletRequest request)
			throws ParseException, SQLException, JsonGenerationException, JsonMappingException, IOException {
		StaffInfo entity = new StaffInfo();

		String createDate = ParamUtils.getParameter(request, "createDate");

		String pageNoStr = ParamUtils.getParameter(request, "pageNo");

		String pageSizeStr = ParamUtils.getParameter(request, "pageSize");
		int pageNo = 1;
		int pageSize = 20;
		if ((StringUtils.isNotBlank(pageNoStr)) && (Integer.parseInt(pageNoStr) > 0)) {
			pageNo = Integer.parseInt(pageNoStr);
		}
		if ((StringUtils.isNotBlank(pageSizeStr)) && (Integer.parseInt(pageSizeStr) > 0)) {
			pageSize = Integer.parseInt(pageSizeStr);
		}
		if (StringUtils.isNotBlank(createDate)) {
			entity.setCreateDate(createDate);
		}
		Pagination<StaffInfo> pagination = staffInfoManager.getPage(entity, pageNo, pageSize);
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, pagination);
		return writer.toString();
	}

	protected String add(HttpServletRequest request)
			throws ParseException, JsonGenerationException, JsonMappingException, IOException, SQLException {
		OperationInfo info = new OperationInfo();
		StaffInfo staffInfo = requestToStaffInfo(request);
		if (checkAddStaffInfo(staffInfo)) {
			info.setStatus(OperationStatus.OPERATION_FAIL.toString());
			info.setMessage("导购信息 新增失败!");
		} else if (staffInfoManager.add(staffInfo)) {
			info.setStatus(OperationStatus.OPERATION_SUCCESS.toString());
			info.setMessage("导购信息 新增成功!");
		} else {
			info.setStatus(OperationStatus.OPERATION_FAIL.toString());
			info.setMessage("导购信息 新增失败!");
		}
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, info);
		return writer.toString();
	}

	protected String addbetch(HttpServletRequest request)
			throws ParseException, JsonGenerationException, JsonMappingException, IOException {
		OperationInfo info = new OperationInfo();
		List<StaffInfo> staffInfo = requestToListStaffInfo(request);
		for (int i = 0; i < staffInfo.size(); i++) {
			if (staffInfoManager.add((StaffInfo) staffInfo.get(i))) {
				info.setStatus(OperationStatus.OPERATION_SUCCESS.toString());
				info.setMessage("批量增加导购信息 新增成功!");
			} else {
				info.setStatus(OperationStatus.OPERATION_FAIL.toString());
				info.setMessage("批量增加导购信息 新增失败!");
			}
		}
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, info);
		return writer.toString();
	}

	protected String update(HttpServletRequest request)
			throws ParseException, JsonGenerationException, JsonMappingException, IOException, SQLException {
		OperationInfo info = new OperationInfo();
		StaffInfo staffInfo = requestToStaffInfo(request);
		if (checkUpdateStaffInfo(staffInfo)) {
			info.setStatus(OperationStatus.OPERATION_FAIL.toString());
			info.setMessage("导购信息 修改失败!");
		} else if (staffInfoManager.update(staffInfo)) {
			info.setStatus(OperationStatus.OPERATION_SUCCESS.toString());
		} else {
			info.setStatus(OperationStatus.OPERATION_FAIL.toString());
			info.setMessage("导购信息 修改失败!");
		}
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, info);
		return writer.toString();
	}

	protected String delete(HttpServletRequest request)
			throws ParseException, JsonGenerationException, JsonMappingException, IOException {
		OperationInfo info = new OperationInfo();
		if (staffInfoManager.delById(request.getParameter("id"))) {
			info.setStatus(OperationStatus.OPERATION_SUCCESS.toString());
		} else {
			info.setStatus(OperationStatus.OPERATION_FAIL.toString());
			info.setMessage("导购信息 删除失败!");
		}
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, info);
		return writer.toString();
	}

	protected StaffInfo requestToStaffInfo(HttpServletRequest request)
			throws ParseException, UnsupportedEncodingException, IOException {
		String jsonStr = receivePost(request);
		Log.info("operate : {},param : {}", request.getParameter("action"), jsonStr);
		StaffInfo staffInfo = (StaffInfo) JSONUtil.JSONToObj(jsonStr, StaffInfo.class);

		return staffInfo;
	}

	protected List<StaffInfo> requestToListStaffInfo(HttpServletRequest request)
			throws ParseException, UnsupportedEncodingException, IOException {
		String jsonStr = receivePost(request);
		Log.info(jsonStr);
		StaffInfoList staffInfo = (StaffInfoList) JSONUtil.JSONToObj(jsonStr, StaffInfoList.class);

		return staffInfo.getStaffInfo();
	}

	public void destroy() {
		super.destroy();
		Log.info("StaffInfoServlet remove exclude!");
		AuthCheckFilter.removeExclude("mllstaff/staffinfo");
		AuthCheckFilter.removeExclude("mllstaff");
	}

	private boolean checkAddStaffInfo(StaffInfo staffInfo) throws SQLException {
		Boolean flag = Boolean.valueOf(false);
		StringBuilder sb = new StringBuilder();
		if (staffInfo == null) {
			flag = Boolean.valueOf(true);
			sb.append(" 检查传入信息有误 :传入信息不正确!");
		}
		if (StringUtils.isBlank(staffInfo.getSysUuid())) {
			flag = Boolean.valueOf(true);
			sb.append(" 检查传入信息有误 :帐号 SysUuid 为空!");
		}
		if (StringUtils.isBlank(staffInfo.getPassword())) {
			flag = Boolean.valueOf(true);
			sb.append(" 检查传入信息有误 :帐号 Password 为空!");
		}
		if (StringUtils.isBlank(staffInfo.getStoreUuid())) {
			flag = Boolean.valueOf(true);
			sb.append(" 检查传入信息有误 :帐号 StoreUuid 为空!");
		}
		if (StringUtils.isBlank(staffInfo.getStaffType())) {
			flag = Boolean.valueOf(true);
			sb.append(" 检查传入信息有误 :帐号 StaffType 为空!");
		}
		if (StringUtils.isNotBlank(staffInfo.getSysUuid())) {
			StaffInfo oldStaffInfo = staffInfoManager.querySysUuid(staffInfo.getSysUuid());
			if (oldStaffInfo != null) {
				flag = Boolean.valueOf(true);
				sb.append(" 检查传入信息有误 :新增帐号 SysUuid 已存在,请检查!");
			}
		}
		if (StringUtils.isNotBlank(sb.toString())) {
			Log.info(sb.toString());
		}
		return flag.booleanValue();
	}

	private boolean checkUpdateStaffInfo(StaffInfo staffInfo) throws SQLException {
		Boolean flag = Boolean.valueOf(false);
		StringBuilder sb = new StringBuilder();
		if (staffInfo == null) {
			flag = Boolean.valueOf(true);
			sb.append("检查传入信息有误:传入信息不正确!");
		}
		if (StringUtils.isBlank(staffInfo.getSysUuid())) {
			flag = Boolean.valueOf(true);
			sb.append("检查传入信息有误:帐号 SysUuid 为空!");
		}
		if (StringUtils.isNotBlank(staffInfo.getSysUuid())) {
			StaffInfo oldStaffInfo = staffInfoManager.querySysUuid(staffInfo.getSysUuid());
			if (oldStaffInfo == null) {
				flag = Boolean.valueOf(true);
				sb.append("检查传入信息 :修改帐号 SysUuid 不存在,请检查!");
			}
		}
		if (StringUtils.isNotBlank(sb.toString())) {
			Log.info(sb.toString());
		}
		return flag.booleanValue();
	}

	private void replyMessage(String message, HttpServletResponse response, PrintWriter out) {
		response.setContentType("text/json");
		out.println(message);
		out.flush();
	}

	public String receivePost(HttpServletRequest request) throws IOException, UnsupportedEncodingException {
		BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
		String line = null;
		StringBuilder sb = new StringBuilder();
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		String reqBody = sb.toString();
		return URLDecoder.decode(reqBody, "UTF-8");
	}
}

