/*  1:   */ package com.meilele.staff.business.common;
/*  2:   */ 
/*  3:   */ import java.util.ArrayList;
/*  4:   */ import java.util.Date;
/*  5:   */ import java.util.List;
/*  6:   */ 
/*  7:   */ public abstract class DatetimeConver<T extends Date>
/*  8:   */   extends AbstractConver<T>
/*  9:   */ {
/* 10: 9 */   public static String DEFAULT_STYLE = "yyyyMMddHHmmss";
/* 11:18 */   protected static List<String> dateStyles = new ArrayList<String>();
/* 12:   */   
/* 13:   */   static
/* 14:   */   {
/* 15:19 */     dateStyles.add("yyyyMMdd");
/* 16:20 */     dateStyles.add("yyyyMMddHHmmss");
/* 17:21 */     dateStyles.add("yyyy-MM-dd");
/* 18:22 */     dateStyles.add("yyyy-MM-dd HH:mm:ss");
/* 19:   */   }
/* 20:   */   
/* 21:   */   protected Date converDate(String value)
/* 22:   */   {
/* 23:31 */     Date result = null;
/* 24:32 */     result = DateTools.getTime(value, DEFAULT_STYLE);
/* 25:33 */     if (result == null) {
/* 26:34 */       for (String style : dateStyles) {
/* 27:35 */         if (!DEFAULT_STYLE.equals(style))
/* 28:   */         {
/* 29:36 */           result = DateTools.getTime(value, style);
/* 30:37 */           if (result != null) {
/* 31:   */             break;
/* 32:   */           }
/* 33:   */         }
/* 34:   */       }
/* 35:   */     }
/* 36:43 */     return result;
/* 37:   */   }
/* 38:   */   
/* 39:   */   public static List<String> getDateStyles()
/* 40:   */   {
/* 41:51 */     return dateStyles;
/* 42:   */   }
/* 43:   */   
/* 44:   */   public static void setDateStyles(List<String> dateStyles)
/* 45:   */   {
/* 46:59 */     DatetimeConver.dateStyles = dateStyles;
/* 47:   */   }
/* 48:   */ }


/* Location:           C:\Users\Administrator\Desktop\openfire\plugins\mllStaff\lib\mllStaff-lib.jar
 * Qualified Name:     com.meilele.staff.business.common.DatetimeConver
 * JD-Core Version:    0.7.0.1
 */