/*   1:    */ package org.jivesoftware.openfire.archive;
/*   4:    */ import java.util.Date;

/*   5:    */ import org.jivesoftware.openfire.XMPPServer;
/*   6:    */ import org.jivesoftware.openfire.cluster.ClusterManager;
/*   7:    */ import org.jivesoftware.openfire.muc.MUCEventDispatcher;
/*   8:    */ import org.jivesoftware.openfire.muc.MUCEventListener;
/*   9:    */ import org.jivesoftware.openfire.muc.MUCRoom;
/*  12:    */ import org.picocontainer.Startable;
/*  13:    */ import org.xmpp.packet.JID;
/*  14:    */ import org.xmpp.packet.Message;
/*  15:    */ 
/*  16:    */ public class GroupConversationInterceptor
/*  17:    */   implements MUCEventListener, Startable
/*  18:    */ {
/*  19:    */   private ConversationManager conversationManager;
/*  20:    */   
/*  21:    */   public GroupConversationInterceptor(ConversationManager conversationManager)
/*  22:    */   {
/*  23: 45 */     this.conversationManager = conversationManager;
/*  24:    */   }
/*  25:    */   
/*  26:    */   public void roomCreated(JID roomJID) {}
/*  27:    */   
/*  28:    */   public void roomDestroyed(JID roomJID)
/*  29:    */   {
/*  30: 54 */     if (ClusterManager.isSeniorClusterMember())
/*  31:    */     {
/*  32: 55 */       this.conversationManager.roomConversationEnded(roomJID, new Date());
/*  33:    */     }
/*  34:    */     else
/*  35:    */     {
/*  36: 58 */       ConversationEventsQueue eventsQueue = this.conversationManager.getConversationEventsQueue();
/*  37: 59 */       eventsQueue.addGroupChatEvent(this.conversationManager.getRoomConversationKey(roomJID), ConversationEvent.roomDestroyed(roomJID, new Date()));
/*  38:    */     }
/*  39:    */   }
/*  40:    */   
/*  41:    */   public void occupantJoined(JID roomJID, JID user, String nickname)
/*  42:    */   {
/*  43: 66 */     if (ClusterManager.isSeniorClusterMember())
/*  44:    */     {
/*  45: 67 */       this.conversationManager.joinedGroupConversation(roomJID, user, nickname, new Date());
/*  46:    */     }
/*  47:    */     else
/*  48:    */     {
/*  49: 70 */       ConversationEventsQueue eventsQueue = this.conversationManager.getConversationEventsQueue();
/*  50: 71 */       eventsQueue.addGroupChatEvent(this.conversationManager.getRoomConversationKey(roomJID), ConversationEvent.occupantJoined(roomJID, user, nickname, new Date()));
/*  51:    */     }
/*  52:    */   }
/*  53:    */   
/*  54:    */   public void occupantLeft(JID roomJID, JID user)
/*  55:    */   {
/*  56: 78 */     if (ClusterManager.isSeniorClusterMember())
/*  57:    */     {
/*  58: 79 */       this.conversationManager.leftGroupConversation(roomJID, user, new Date());
/*  59:    */       
/*  60: 81 */       MUCRoom mucRoom = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(roomJID).getChatRoom(roomJID.getNode());
/*  61: 82 */       if ((mucRoom != null) && (mucRoom.getOccupantsCount() == 0)) {
/*  62: 83 */         this.conversationManager.roomConversationEnded(roomJID, new Date());
/*  63:    */       }
/*  64:    */     }
/*  65:    */     else
/*  66:    */     {
/*  67: 87 */       ConversationEventsQueue eventsQueue = this.conversationManager.getConversationEventsQueue();
/*  68: 88 */       eventsQueue.addGroupChatEvent(this.conversationManager.getRoomConversationKey(roomJID), ConversationEvent.occupantLeft(roomJID, user, new Date()));
/*  69:    */     }
/*  70:    */   }
/*  71:    */   
/*  72:    */   public void nicknameChanged(JID roomJID, JID user, String oldNickname, String newNickname)
/*  73:    */   {
/*  74: 95 */     if (ClusterManager.isSeniorClusterMember())
/*  75:    */     {
/*  76: 96 */       occupantLeft(roomJID, user);
/*  77:    */       try
/*  78:    */       {
/*  79: 99 */         Thread.sleep(1L);
/*  80:    */       }
/*  81:    */       catch (InterruptedException e) {}
/*  82:103 */       occupantJoined(roomJID, user, newNickname);
/*  83:    */     }
/*  84:    */     else
/*  85:    */     {
/*  86:106 */       ConversationEventsQueue eventsQueue = this.conversationManager.getConversationEventsQueue();
/*  87:107 */       eventsQueue.addGroupChatEvent(this.conversationManager.getRoomConversationKey(roomJID), ConversationEvent.nicknameChanged(roomJID, user, newNickname, new Date()));
/*  88:    */     }
/*  89:    */   }
/*  90:    */   
/*  91:    */   public void messageReceived(JID roomJID, JID user, String nickname, Message message)
/*  92:    */   {
/*  93:114 */     if (ClusterManager.isSeniorClusterMember())
/*  94:    */     {
/*  95:115 */       this.conversationManager.processRoomMessage(roomJID, user, nickname, message.getBody(), new Date(), message.getID());
/*  96:    */     }
/*  97:    */     else
/*  98:    */     {
/*  99:118 */       boolean withBody = (this.conversationManager.isRoomArchivingEnabled()) && ((this.conversationManager.getRoomsArchived().isEmpty()) || (this.conversationManager.getRoomsArchived().contains(roomJID.getNode())));
/* 100:    */       
/* 101:    */ 
/* 102:    */ 
/* 103:122 */       ConversationEventsQueue eventsQueue = this.conversationManager.getConversationEventsQueue();
/* 104:123 */       eventsQueue.addGroupChatEvent(this.conversationManager.getRoomConversationKey(roomJID), ConversationEvent.roomMessageReceived(roomJID, user, nickname, withBody ? message.getBody() : null, new Date()));
/* 105:    */     }
/* 106:    */   }
/* 107:    */   
/* 108:    */   public void privateMessageRecieved(JID toJID, JID fromJID, Message message)
/* 109:    */   {
/* 110:129 */     if (message.getBody() != null) {
/* 111:130 */       if (ClusterManager.isSeniorClusterMember())
/* 112:    */       {
/* 113:131 */         this.conversationManager.processMessage(fromJID, toJID, message.getBody(), message.toXML(), new Date(), message.getID());
/* 114:    */       }
/* 115:    */       else
/* 116:    */       {
/* 117:134 */         ConversationEventsQueue eventsQueue = this.conversationManager.getConversationEventsQueue();
/* 118:135 */         eventsQueue.addChatEvent(this.conversationManager.getConversationKey(fromJID, toJID), ConversationEvent.chatMessageReceived(toJID, fromJID, this.conversationManager.isMessageArchivingEnabled() ? message.getBody() : null, new Date()));
/* 119:    */       }
/* 120:    */     }
/* 121:    */   }
/* 122:    */   
/* 123:    */   public void roomSubjectChanged(JID roomJID, JID user, String newSubject) {}
/* 124:    */   
/* 125:    */   public void start()
/* 126:    */   {
/* 127:148 */     MUCEventDispatcher.addListener(this);
/* 128:    */   }
/* 129:    */   
/* 130:    */   public void stop()
/* 131:    */   {
/* 132:152 */     MUCEventDispatcher.removeListener(this);
/* 133:153 */     this.conversationManager = null;
/* 134:    */   }
/* 135:    */ }


/* Location:           C:\Users\Administrator\Desktop\openfire\plugins\monitoring\lib\monitoring-lib.jar
 * Qualified Name:     org.jivesoftware.openfire.archive.GroupConversationInterceptor
 * JD-Core Version:    0.7.0.1
 */