package com.meilele.staff.openfire.iqError;

public enum IQError
{
  NO_STAFF_ACCEPT_USER("mllStaff_1", "未找到导购接受用户!"),
  INVALID_STAFF_STORE_ID("mllStaff_2", "invalid staff store id"),
  DUPLICATE_QUERY_STAFF_IQ("mllStaff_3", "重复的轮询导购请求!"),
  QUERY_STAFF_BY_SYS_UUID_EXCEPTION("mllStaff_4", "根据SysUuid获取导购失败!"),
  NO_STAFF_OF_SYS_UUID("mllStaff_5", "SysUuid对应的导购不存在!"),
  STORE_UUID_NOT_AVALIABLE("mllStaff_6", "门店未激活!"),
  EXCEPTION_DURING_QUERY_STAFF("mllStaff_7", "客服分配导购发生异常!");
  
  private String errorCode;
  private String errorMsg;
  
  private IQError(String errorCode, String errorMsg)
  {
    this.errorCode = errorCode;
    this.errorMsg = errorMsg;
  }
  
  public String getErrorCode()
  {
    return this.errorCode;
  }
  
  public String getErrorMsg()
  {
    return this.errorMsg;
  }
}


/* Location:           C:\Users\Administrator\Desktop\openfire\plugins\mllStaff\lib\mllStaff-lib.jar
 * Qualified Name:     com.meilele.staff.openfire.iqError.IQError
 * JD-Core Version:    0.7.0.1
 */