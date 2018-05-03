package com.meilele.staff.openfire.servlet;

import com.meilele.staff.business.common.JSONUtil;
import com.meilele.staff.business.common.OperationInfo;
import com.meilele.staff.business.common.OperationStatus;
import com.meilele.staff.business.store.BasicInfo;
import com.meilele.staff.business.store.DbBasicInfoManager;
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

public class BasicInfoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger Log = LoggerFactory.getLogger(BasicInfoServlet.class);
	private static final String CHARACTER_ENCODING = "utf-8";
	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final String REQUEST_PARAM = "action";
	private static final String REQUEST_CALL_BACK = "callback";
	private static DbBasicInfoManager basicInfoManager;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		basicInfoManager = DbBasicInfoManager.getInstance();
		Log.info("BasicInfoServlet add exclude!");
		AuthCheckFilter.addExclude("mllstaff");
		AuthCheckFilter.addExclude("mllstaff/basicinfo");
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
		try {
			String handleRequestResult = handleRequest(request);
			if (hasCallBack(request.getParameter("callback"))) {
				handleRequestResult = addCallBack(handleRequestResult, request.getParameter("callback"));
			}
			replyMessage(handleRequestResult, response, out);
		} catch (Exception ex) {
			Log.error("Basic info oprate exception:", ex);
			OperationInfo info = new OperationInfo();
			StringWriter writer = new StringWriter();
			info.setStatus(OperationStatus.OPERATION_FAIL.toString());
			info.setMessage(ex.getMessage());
			mapper.writeValue(writer, info);
			replyMessage(writer.toString(), response, out);
		}
	}

	private String handleRequest(HttpServletRequest request)
			throws JsonGenerationException, JsonMappingException, ParseException, IOException, SQLException {
		switch (com.meilele.staff.business.common.Action.fromAction(request.getParameter("action"))) {
		case ACTION_ADD:
			return add(request);
		case ACTION_DELETE:
			return delete(request);
		case ACTION_UPDATE:
			return update(request);
		case ACTION_BATCHADD:
			//return update(request);
		default:
			return query(request);
		}
	}

	private boolean hasCallBack(String callBack) {
		return StringUtils.isNotBlank(callBack);
	}

	private String addCallBack(String handleRequestResult, String callBack) {
		return callBack + "(" + handleRequestResult + ")";
	}

	private String query(HttpServletRequest request)
			throws ParseException, SQLException, JsonGenerationException, JsonMappingException, IOException {
		BasicInfo entity = new BasicInfo();

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
		entity.setStoreStatus("已经开业");
		Pagination<BasicInfo> pagination = basicInfoManager.getPage(entity, pageNo, pageSize);
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, pagination);
		return writer.toString();
	}

	private String add(HttpServletRequest request)
			throws ParseException, JsonGenerationException, JsonMappingException, IOException {
		OperationInfo info = new OperationInfo();
		BasicInfo basicInfo = requestToBasicInfo(request);
		if (basicInfoManager.add(basicInfo)) {
			info.setStatus(OperationStatus.OPERATION_SUCCESS.toString());
		} else {
			info.setStatus(OperationStatus.OPERATION_FAIL.toString());
			info.setMessage("门店管理新增失败!");
		}
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, info);
		return writer.toString();
	}

	protected String update(HttpServletRequest request)
			throws ParseException, JsonGenerationException, JsonMappingException, IOException, SQLException {
		OperationInfo info = new OperationInfo();
		BasicInfo basicInfo = requestToBasicInfo(request);
		if (basicInfoManager.update(basicInfo)) {
			info.setStatus(OperationStatus.OPERATION_SUCCESS.toString());
		} else {
			info.setStatus(OperationStatus.OPERATION_FAIL.toString());
			info.setMessage("门店管理修改失败!");
		}
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, info);
		return writer.toString();
	}

	protected String delete(HttpServletRequest request)
			throws ParseException, JsonGenerationException, JsonMappingException, IOException {
		OperationInfo info = new OperationInfo();
		if ((StringUtils.isNotBlank(request.getParameter("id")))
				&& (basicInfoManager.delById(request.getParameter("id")))) {
			info.setStatus(OperationStatus.OPERATION_SUCCESS.toString());
		} else {
			info.setStatus(OperationStatus.OPERATION_FAIL.toString());
			info.setMessage("门店管理删除失败!");
		}
		StringWriter writer = new StringWriter();
		mapper.writeValue(writer, info);
		return writer.toString();
	}

	protected BasicInfo requestToBasicInfo(HttpServletRequest request)
			throws ParseException, UnsupportedEncodingException, IOException {
		String jsonStr = receivePost(request);
		Log.info(jsonStr);
		BasicInfo staffInfo = (BasicInfo) JSONUtil.JSONToObj(jsonStr, BasicInfo.class);

		return staffInfo;
	}

	public void destroy() {
		super.destroy();
		Log.info("BasicInfoServlet remove exclude!");
		AuthCheckFilter.removeExclude("mllstaff/basicinfo");
		AuthCheckFilter.removeExclude("mllstaff");
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

/* Location:           C:\Users\Administrator\Desktop\openfire\plugins\mllStaff\lib\mllStaff-lib.jar
 * Qualified Name:     com.meilele.staff.openfire.servlet.BasicInfoServlet
 * JD-Core Version:    0.7.0.1
 */