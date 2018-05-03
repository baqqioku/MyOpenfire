package org.jivesoftware.openfire.plugin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
import org.jivesoftware.util.page.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserStatusServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger Log = LoggerFactory.getLogger(UserStatusServlet.class);
	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	private static final ObjectMapper mapper = new ObjectMapper();

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		AuthCheckFilter.addExclude("userstatus");
		AuthCheckFilter.addExclude("userstatus/status*");
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
		StringWriter writer = new StringWriter();
		Pagination<MllOfUserStatus> pagination = new UserStatusManager().query(request);
		mapper.writeValue(writer, pagination);
		return writer.toString();
	}

	public void destroy() {
		super.destroy();
		AuthCheckFilter.removeExclude("userstatus/status*");
		AuthCheckFilter.removeExclude("userstatus");
	}

	private void replyMessage(String message, HttpServletResponse response, PrintWriter out) {
		response.setContentType("text/json");
		out.println(message);
		out.flush();
	}
}
