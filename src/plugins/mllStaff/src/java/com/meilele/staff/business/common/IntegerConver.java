/*  1:   */ package com.meilele.staff.business.common;
/*  2:   */ 
/*  3:   */ public class IntegerConver
/*  4:   */   extends AbstractConver<Integer>
/*  5:   */ {
/*  6:   */   public IntegerConver() {}
/*  7:   */   
/*  8:   */   public IntegerConver(Integer defvalue)
/*  9:   */   {
/* 10:16 */     super.setDefvalue(defvalue);
/* 11:   */   }
/* 12:   */   
/* 13:   */   protected Integer converObject(Object obj)
/* 14:   */   {
/* 15:20 */     Integer result = null;
/* 16:   */     try
/* 17:   */     {
/* 18:22 */       if (obj != null) {
/* 19:23 */         if ((obj instanceof Integer)) {
/* 20:24 */           result = (Integer)obj;
/* 21:   */         } else {
/* 22:27 */           result = Integer.valueOf(Integer.parseInt(obj.toString()));
/* 23:   */         }
/* 24:   */       }
/* 25:   */     }
/* 26:   */     catch (Exception ex) {}
/* 27:34 */     return result;
/* 28:   */   }
/* 29:   */   
/* 30:   */   protected Integer[] getArray(int length)
/* 31:   */   {
/* 32:38 */     return new Integer[length];
/* 33:   */   }
/* 34:   */ }


/* Location:           C:\Users\Administrator\Desktop\openfire\plugins\mllStaff\lib\mllStaff-lib.jar
 * Qualified Name:     com.meilele.staff.business.common.IntegerConver
 * JD-Core Version:    0.7.0.1
 */