/*   1:    */ package org.jivesoftware.openfire.archive;
/*   2:    */ 
/*   3:    */ import org.jivesoftware.util.StringUtils;
/*   4:    */ 
/*   5:    */ public class ConversationInfo
/*   6:    */ {
/*   7:    */   private long conversationID;
/*   8:    */   private String participant1;
/*   9:    */   private String participant2;
/*  10:    */   private String[] allParticipants;
/*  11:    */   private String date;
/*  12:    */   private String lastActivity;
/*  13:    */   private String body;
/*  14:    */   private int messageCount;
/*  15:    */   private long duration;
/*  16:    */   
/*  17:    */   public long getConversationID()
/*  18:    */   {
/*  19: 42 */     return this.conversationID;
/*  20:    */   }
/*  21:    */   
/*  22:    */   public void setConversationID(long conversationID)
/*  23:    */   {
/*  24: 46 */     this.conversationID = conversationID;
/*  25:    */   }
/*  26:    */   
/*  27:    */   public String getParticipant1()
/*  28:    */   {
/*  29: 50 */     return this.participant1;
/*  30:    */   }
/*  31:    */   
/*  32:    */   public void setParticipant1(String participant1)
/*  33:    */   {
/*  34: 54 */     this.participant1 = participant1;
/*  35:    */   }
/*  36:    */   
/*  37:    */   public String getParticipant2()
/*  38:    */   {
/*  39: 58 */     return this.participant2;
/*  40:    */   }
/*  41:    */   
/*  42:    */   public void setParticipant2(String participant2)
/*  43:    */   {
/*  44: 62 */     this.participant2 = participant2;
/*  45:    */   }
/*  46:    */   
/*  47:    */   public String[] getAllParticipants()
/*  48:    */   {
/*  49: 66 */     return this.allParticipants;
/*  50:    */   }
/*  51:    */   
/*  52:    */   public void setAllParticipants(String[] allParticipants)
/*  53:    */   {
/*  54: 70 */     this.allParticipants = allParticipants;
/*  55:    */   }
/*  56:    */   
/*  57:    */   public String getDate()
/*  58:    */   {
/*  59: 74 */     return this.date;
/*  60:    */   }
/*  61:    */   
/*  62:    */   public void setDate(String date)
/*  63:    */   {
/*  64: 78 */     this.date = date;
/*  65:    */   }
/*  66:    */   
/*  67:    */   public String getBody()
/*  68:    */   {
/*  69: 82 */     return this.body;
/*  70:    */   }
/*  71:    */   
/*  72:    */   public void setBody(String body)
/*  73:    */   {
/*  74: 86 */     this.body = body;
/*  75:    */   }
/*  76:    */   
/*  77:    */   public int getMessageCount()
/*  78:    */   {
/*  79: 90 */     return this.messageCount;
/*  80:    */   }
/*  81:    */   
/*  82:    */   public void setMessageCount(int messageCount)
/*  83:    */   {
/*  84: 94 */     this.messageCount = messageCount;
/*  85:    */   }
/*  86:    */   
/*  87:    */   public String getDuration()
/*  88:    */   {
/*  89: 98 */     return StringUtils.getTimeFromLong(this.duration);
/*  90:    */   }
/*  91:    */   
/*  92:    */   public void setDuration(long duration)
/*  93:    */   {
/*  94:102 */     this.duration = duration;
/*  95:    */   }
/*  96:    */   
/*  97:    */   public String getLastActivity()
/*  98:    */   {
/*  99:106 */     return this.lastActivity;
/* 100:    */   }
/* 101:    */   
/* 102:    */   public void setLastActivity(String lastActivity)
/* 103:    */   {
/* 104:110 */     this.lastActivity = lastActivity;
/* 105:    */   }
/* 106:    */ }


/* Location:           C:\Users\Administrator\Desktop\openfire\plugins\monitoring\lib\monitoring-lib.jar
 * Qualified Name:     org.jivesoftware.openfire.archive.ConversationInfo
 * JD-Core Version:    0.7.0.1
 */