package org.jivesoftware.openfire.plugin;

import org.apache.commons.lang.StringUtils;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.user.User;
import org.xmpp.packet.JID;

public class MllUserUtils
{
  public static final String MLL_USER_TYPE_PROP_NAME = "mllUser_type";
  public static final String MLL_USER_PROP_STAFF_VALUE = "staff";

  public static boolean isStaff(String node)
  {
    if (StringUtils.isBlank(node)) {
      return false;
    }
    if (XMPPServer.getInstance().getUserManager().isRegisteredUser(node)) {
      return "staff".equals(User.getPropertyValue(node, "mllUser_type"));
    }

    return false;
  }

  public static boolean isCustomer(JID customerJID)
  {
    if (customerJID == null) {
      return false;
    }
    return XMPPServer.getInstance().getSessionManager().isAnonymousRoute(customerJID);
  }
}