/*  1:   */ package com.meilele.staff.business.common;
/*  2:   */ 
/*  3:   */ public enum OperationStatus
/*  4:   */ {
/*  5:14 */   OPERATION_SUCCESS(1),  OPERATION_FAIL(0);
/*  6:   */   
/*  7:   */   private int status;
/*  8:   */   
/*  9:   */   private OperationStatus(int status)
/* 10:   */   {
/* 11:20 */     this.status = status;
/* 12:   */   }
/* 13:   */   
/* 14:   */   public String toString()
/* 15:   */   {
/* 16:25 */     return String.valueOf(this.status);
/* 17:   */   }
/* 18:   */ }


/* Location:           C:\Users\Administrator\Desktop\openfire\plugins\mllStaff\lib\mllStaff-lib.jar
 * Qualified Name:     com.meilele.staff.business.common.OperationStatus
 * JD-Core Version:    0.7.0.1
 */