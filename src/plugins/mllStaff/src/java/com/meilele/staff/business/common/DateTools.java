/*  1:   */ package com.meilele.staff.business.common;
/*  2:   */ 
/*  3:   */ import java.text.ParseException;
/*  4:   */ import java.text.SimpleDateFormat;
/*  5:   */ import java.util.Date;
/*  6:   */ import org.apache.commons.lang.StringUtils;
/*  7:   */ 
/*  8:   */ public class DateTools
/*  9:   */ {
/* 10:   */   public static Date getTime(String value, String style)
/* 11:   */   {
/* 12:19 */     SimpleDateFormat d = new SimpleDateFormat();
/* 13:20 */     if (StringUtils.isBlank(value)) {
/* 14:21 */       return new Date();
/* 15:   */     }
/* 16:   */     try
/* 17:   */     {
/* 18:23 */       d.applyPattern(style);
/* 19:24 */       return d.parse(value);
/* 20:   */     }
/* 21:   */     catch (ParseException e) {}
/* 22:26 */     return null;
/* 23:   */   }
/* 24:   */   
/* 25:   */   public static String getCurrentDateTime()
/* 26:   */   {
/* 27:35 */     SimpleDateFormat d = new SimpleDateFormat();
/* 28:36 */     d.applyPattern("HH:mm:ss");
/* 29:37 */     Date nowdate = new Date();
/* 30:38 */     String str_date = d.format(nowdate);
/* 31:39 */     return str_date;
/* 32:   */   }
/* 33:   */   
/* 34:   */   public static String getTimeString(String time)
/* 35:   */   {
/* 36:48 */     String[] ti = time.split(":");
/* 37:49 */     if (ti[1].length() == 1) {
/* 38:50 */       time = ti[0] + "0" + ti[1];
/* 39:   */     } else {
/* 40:52 */       time = ti[0] + ti[1];
/* 41:   */     }
/* 42:54 */     return time;
/* 43:   */   }
/* 44:   */   
/* 45:   */   public static String TimeString()
/* 46:   */   {
/* 47:62 */     Date date = new Date();
/* 48:63 */     return getTimeString(date.getHours() + ":" + date.getMinutes());
/* 49:   */   }
/* 50:   */ }


/* Location:           C:\Users\Administrator\Desktop\openfire\plugins\mllStaff\lib\mllStaff-lib.jar
 * Qualified Name:     com.meilele.staff.business.common.DateTools
 * JD-Core Version:    0.7.0.1
 */