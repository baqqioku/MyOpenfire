/*  1:   */ package com.meilele.staff.business.common;
/*  2:   */ 
/*  3:   */ public class FloatConver
/*  4:   */   extends AbstractConver<Float>
/*  5:   */ {
/*  6:   */   public FloatConver() {}
/*  7:   */   
/*  8:   */   public FloatConver(Float defvalue)
/*  9:   */   {
/* 10:16 */     super.setDefvalue(defvalue);
/* 11:   */   }
/* 12:   */   
/* 13:   */   protected Float converObject(Object obj)
/* 14:   */   {
/* 15:20 */     Float result = null;
/* 16:   */     try
/* 17:   */     {
/* 18:22 */       if (obj != null) {
/* 19:23 */         if ((obj instanceof Float)) {
/* 20:24 */           result = (Float)obj;
/* 21:   */         } else {
/* 22:26 */           result = Float.valueOf(Float.parseFloat(obj.toString()));
/* 23:   */         }
/* 24:   */       }
/* 25:   */     }
/* 26:   */     catch (Exception ex) {}
/* 27:32 */     return result;
/* 28:   */   }
/* 29:   */   
/* 30:   */   protected Float[] getArray(int length)
/* 31:   */   {
/* 32:36 */     return new Float[length];
/* 33:   */   }
/* 34:   */ }


/* Location:           C:\Users\Administrator\Desktop\openfire\plugins\mllStaff\lib\mllStaff-lib.jar
 * Qualified Name:     com.meilele.staff.business.common.FloatConver
 * JD-Core Version:    0.7.0.1
 */