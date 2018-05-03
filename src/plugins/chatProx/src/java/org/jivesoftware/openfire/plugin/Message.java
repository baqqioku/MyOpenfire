package org.jivesoftware.openfire.plugin;

public class Message
{
  private long messageId;
  private String sender;
  private String receiver;
  private Long createDate;
  private String content;
  private String receipt_id;

  public String getReceipt_id()
  {
    return this.receipt_id;
  }

  public void setReceipt_id(String receipt_id) {
    this.receipt_id = receipt_id;
  }

  public long getMessageId() {
    return this.messageId;
  }

  public void setMessageId(long messageId) {
    this.messageId = messageId;
  }

  public String getSender() {
    return this.sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public String getReceiver() {
    return this.receiver;
  }

  public void setReceiver(String receiver) {
    this.receiver = receiver;
  }

  public Long getCreateDate() {
    return this.createDate;
  }

  public void setCreateDate(Long createDate) {
    this.createDate = createDate;
  }

  public String getContent() {
    return this.content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}