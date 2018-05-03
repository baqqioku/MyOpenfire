/*  1:   */ package com.meilele.staff.business.common;
/*  2:   */ 
/*  3:   */ public class DoubleConver
/*  4:   */   extends AbstractConver<Double>
/*  5:   */ {
/*  6:   */   public DoubleConver() {}
/*  7:   */   
/*  8:   */   public DoubleConver(Double defvalue)
/*  9:   */   {
/* 10:13 */     super.setDefvalue(defvalue);
/* 11:   */   }
/* 12:   */   
/* 13:   */   protected Double converObject(Object obj)
/* 14:   */   {
/* 15:16 */     Double result = null;
/* 16:   */     try
/* 17:   */     {
/* 18:18 */       if (obj != null) {
/* 19:19 */         if ((obj instanceof Double)) {
/* 20:20 */           result = (Double)obj;
/* 21:   */         } else {
/* 22:22 */           result = Double.valueOf(Double.parseDouble(obj.toString()));
/* 23:   */         }
/* 24:   */       }
/* 25:   */     }
/* 26:   */     catch (Exception ex) {}
/* 27:28 */     return result;
/* 28:   */   }
/* 29:   */   
/* 30:   */   protected Double[] getArray(int length)
/* 31:   */   {
/* 32:32 */     return new Double[length];
/* 33:   */   }
/* 34:   */ }


/* Location:           C:\Users\Administrator\Desktop\openfire\plugins\mllStaff\lib\mllStaff-lib.jar
 * Qualified Name:     com.meilele.staff.business.common.DoubleConver
 * JD-Core Version:    0.7.0.1
 */