/*  1:   */ package com.meilele.staff.business.common;
/*  2:   */ 
/*  3:   */ import org.apache.commons.lang.StringUtils;
/*  4:   */ import org.jivesoftware.openfire.SessionManager;
/*  5:   */ import org.jivesoftware.openfire.XMPPServer;
/*  6:   */ import org.jivesoftware.openfire.user.User;
/*  7:   */ import org.jivesoftware.openfire.user.UserManager;
/*  8:   */ import org.xmpp.packet.JID;
/*  9:   */ 
/* 10:   */ public class MllUserUtils
/* 11:   */ {
/* 12:   */   public static final String MLL_USER_TYPE_PROP_NAME = "mllUser_type";
/* 13:   */   public static final String MLL_USER_PROP_STAFF_VALUE = "staff";
/* 14:   */   
/* 15:   */   public static boolean isStaff(String node)
/* 16:   */   {
/* 17:37 */     if (StringUtils.isBlank(node)) {
/* 18:38 */       return false;
/* 19:   */     }
/* 20:40 */     if (XMPPServer.getInstance().getUserManager().isRegisteredUser(node)) {
/* 21:41 */       return "staff".equals(User.getPropertyValue(node, "mllUser_type"));
/* 22:   */     }
/* 23:44 */     return false;
/* 24:   */   }
/* 25:   */   
/* 26:   */   public static boolean isCustomer(JID customerJID)
/* 27:   */   {
/* 28:55 */     if (customerJID == null) {
/* 29:56 */       return false;
/* 30:   */     }
/* 31:58 */     return XMPPServer.getInstance().getSessionManager().isAnonymousRoute(customerJID);
/* 32:   */   }
/* 33:   */ }


/* Location:           C:\Users\Administrator\Desktop\openfire\plugins\mllStaff\lib\mllStaff-lib.jar
 * Qualified Name:     com.meilele.staff.business.common.MllUserUtils
 * JD-Core Version:    0.7.0.1
 */