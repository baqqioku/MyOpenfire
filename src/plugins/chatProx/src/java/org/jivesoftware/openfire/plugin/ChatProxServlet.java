package org.jivesoftware.openfire.plugin;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
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

public class ChatProxServlet extends HttpServlet
{
  private static final long serialVersionUID = 1L;
  private static final Logger Log = LoggerFactory.getLogger(ChatProxServlet.class);
  private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
  private static final ObjectMapper mapper = new ObjectMapper();
  private static DbChatProxManager logsManager;

  public void init(ServletConfig config)
    throws ServletException
  {
    super.init(config);
    logsManager = DbChatProxManager.getInstance();
    AuthCheckFilter.addExclude("chatprox");
    AuthCheckFilter.addExclude("chatprox/history");
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    doPost(request, response);
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    request.setCharacterEncoding("utf-8");
    response.setCharacterEncoding("utf-8");
    PrintWriter out = response.getWriter();
    String callback = request.getParameter("callback");
    try
    {
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

  protected String query(HttpServletRequest request, HttpServletResponse response) throws ParseException, SQLException, JsonGenerationException, JsonMappingException, IOException {
    ChatProx entity = new ChatProx();
    String sender = ParamUtils.getParameter(request, "sender");
    String receiver = ParamUtils.getParameter(request, "receiver");
    String content = ParamUtils.getParameter(request, "content");
    String createDate = ParamUtils.getParameter(request, "createDate");
    String pageNoStr = ParamUtils.getParameter(request, "pageNo");
    String pageSizeStr = ParamUtils.getParameter(request, "pageSize");
    String groupType = ParamUtils.getParameter(request, "groupType");

    int pageNo = 1;
    int pageSize = 20;

    if ((StringUtils.isNotBlank(pageNoStr)) && (Integer.parseInt(pageNoStr) > 0)) pageNo = Integer.parseInt(pageNoStr);

    if ((StringUtils.isNotBlank(pageSizeStr)) && (Integer.parseInt(pageSizeStr) > 0)) pageSize = Integer.parseInt(pageSizeStr);

    if (StringUtils.isNotBlank(createDate)) entity.setCreateDate(new Timestamp(df.parse(createDate).getTime()));

    if (StringUtils.isNotBlank(groupType)) entity.setGroupType(groupType);

    entity.setContent(content);
    entity.setReceiver(receiver);
    entity.setSender(sender);
    Pagination<Message> pagination = logsManager.getPage(entity, pageNo, pageSize);
    StringWriter writer = new StringWriter();
    mapper.writeValue(writer, pagination);
    return writer.toString();
  }

  public void destroy()
  {
    super.destroy();
    AuthCheckFilter.removeExclude("chatprox/history");
    AuthCheckFilter.removeExclude("chatprox");
  }

  private void replyMessage(String message, HttpServletResponse response, PrintWriter out) {
    response.setContentType("text/json");
    out.println(message);
    out.flush();
  }
}