/*    */ package org.jivesoftware.openfire.plugin;
/*    */ 
/*    */ import java.util.List;
/*    */ 
/*    */ public class MllChatSession
/*    */ {
/*    */   private Integer id;
/*    */   private String sessionId;
/*    */   private String serviceName;
/*    */   private String customerIp;
/*    */   private String customerCookie;
/*    */   private String customerAddress;
/*    */   private String customerName;
/*    */   private Long sessionDate;
/*    */   private String sessionFormatDate;
/*    */   private String semSrc;
/*    */   private List<Message> messages;
/*    */ 
/*    */   public String getCustomerCookie()
/*    */   {
/* 19 */     return this.customerCookie;
/*    */   }
/*    */   public void setCustomerCookie(String customerCookie) {
/* 22 */     this.customerCookie = customerCookie;
/*    */   }
/*    */   public Integer getId() {
/* 25 */     return this.id;
/*    */   }
/*    */   public void setId(Integer id) {
/* 28 */     this.id = id;
/*    */   }
/*    */   public String getSessionId() {
/* 31 */     return this.sessionId;
/*    */   }
/*    */   public void setSessionId(String sessionId) {
/* 34 */     this.sessionId = sessionId;
/*    */   }
/*    */   public String getCustomerIp() {
/* 37 */     return this.customerIp;
/*    */   }
/*    */   public void setCustomerIp(String customerIp) {
/* 40 */     this.customerIp = customerIp;
/*    */   }
/*    */   public String getCustomerAddress() {
/* 43 */     return this.customerAddress;
/*    */   }
/*    */   public void setCustomerAddress(String customerAddress) {
/* 46 */     this.customerAddress = customerAddress;
/*    */   }
/*    */   public void setServiceName(String serviceName) {
/* 49 */     this.serviceName = serviceName;
/*    */   }
/*    */   public String getServiceName() {
/* 52 */     return this.serviceName;
/*    */   }
/*    */   public void setCustomerName(String customerName) {
/* 55 */     this.customerName = customerName;
/*    */   }
/*    */   public String getCustomerName() {
/* 58 */     return this.customerName;
/*    */   }
/*    */   public Long getSessionDate() {
/* 61 */     return this.sessionDate;
/*    */   }
/*    */   public void setSessionDate(Long sessionDate) {
/* 64 */     this.sessionDate = sessionDate;
/*    */   }
/*    */   public String getSessionFormatDate() {
/* 67 */     return this.sessionFormatDate;
/*    */   }
/*    */   public void setSessionFormatDate(String sessionFormatDate) {
/* 70 */     this.sessionFormatDate = sessionFormatDate;
/*    */   }
/*    */   public List<Message> getMessages() {
/* 73 */     return this.messages;
/*    */   }
/*    */   public void setMessages(List<Message> messages) {
/* 76 */     this.messages = messages;
/*    */   }
/*    */   public String getSemSrc() {
/* 79 */     return this.semSrc;
/*    */   }
/*    */   public void setSemSrc(String semSrc) {
/* 82 */     this.semSrc = semSrc;
/*    */   }
/*    */   public MllChatSession() {
/*    */   }
/*    */ 
/*    */   public MllChatSession(Integer id, String sessionId, String serviceName, String customerIp, String customerAddress, String customerName, Long sessionDate, String sessionFormatDate, String customerCookie, String semSrc) {
/* 88 */     this.id = id;
/* 89 */     this.sessionId = sessionId;
/* 90 */     this.serviceName = serviceName;
/* 91 */     this.customerIp = customerIp;
/* 92 */     this.customerAddress = customerAddress;
/* 93 */     this.customerName = customerName;
/* 94 */     this.sessionDate = sessionDate;
/* 95 */     this.sessionFormatDate = sessionFormatDate;
/* 96 */     this.customerCookie = customerCookie;
/* 97 */     this.semSrc = semSrc;
/*    */   }
/*    */ }

/* Location:           C:\Users\Administrator\AppData\Local\Temp\360zip$Temp\360$9\chatProx-lib.jar
 * Qualified Name:     org.jivesoftware.openfire.plugin.MllChatSession
 * JD-Core Version:    0.6.0
 */