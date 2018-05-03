package org.jivesoftware.openfire.archive;

import java.util.Date;

public abstract interface ConversationListener
{
  public abstract void conversationCreated(Conversation paramConversation);
  
  public abstract void conversationUpdated(Conversation paramConversation, Date paramDate);
  
  public abstract void conversationEnded(Conversation paramConversation);
}

