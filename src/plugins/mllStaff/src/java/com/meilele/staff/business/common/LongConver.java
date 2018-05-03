/*  1:   */ package com.meilele.staff.business.common;
/*  2:   */ 
/*  3:   */ public class LongConver
/*  4:   */   extends AbstractConver<Long>
/*  5:   */ {
/*  6:   */   public LongConver() {}
/*  7:   */   
/*  8:   */   public LongConver(Long defvalue)
/*  9:   */   {
/* 10:15 */     super.setDefvalue(defvalue);
/* 11:   */   }
/* 12:   */   
/* 13:   */   protected Long converObject(Object obj)
/* 14:   */   {
/* 15:19 */     Long result = null;
/* 16:   */     try
/* 17:   */     {
/* 18:21 */       if (obj != null) {
/* 19:22 */         if ((obj instanceof Long)) {
/* 20:23 */           result = (Long)obj;
/* 21:   */         } else {
/* 22:25 */           result = Long.valueOf(Long.parseLong(obj.toString()));
/* 23:   */         }
/* 24:   */       }
/* 25:   */     }
/* 26:   */     catch (Exception ex) {}
/* 27:31 */     return result;
/* 28:   */   }
/* 29:   */   
/* 30:   */   protected Long[] getArray(int length)
/* 31:   */   {
/* 32:35 */     return new Long[length];
/* 33:   */   }
/* 34:   */ }


/* Location:           C:\Users\Administrator\Desktop\openfire\plugins\mllStaff\lib\mllStaff-lib.jar
 * Qualified Name:     com.meilele.staff.business.common.LongConver
 * JD-Core Version:    0.7.0.1
 */