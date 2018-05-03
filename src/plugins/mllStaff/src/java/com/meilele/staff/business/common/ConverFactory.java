 package com.meilele.staff.business.common;
 
 import java.util.Date;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 public final class ConverFactory
 {
   private static Map<Class<?>, Conver<?>> CONVER_MAP = new ConcurrentHashMap<Class<?>, Conver<?>>();
   
   static
   {
     CONVER_MAP.put(Boolean.TYPE, new BooleanConver(Boolean.valueOf(false)));
     CONVER_MAP.put(Boolean.class, new BooleanConver());
     CONVER_MAP.put(Double.TYPE, new DoubleConver(Double.valueOf(0.0D)));
     CONVER_MAP.put(Double.class, new DoubleConver());
     CONVER_MAP.put(Float.TYPE, new FloatConver(Float.valueOf(0.0F)));
     CONVER_MAP.put(Float.class, new FloatConver());
     CONVER_MAP.put(Integer.TYPE, new IntegerConver(Integer.valueOf(0)));
     CONVER_MAP.put(Integer.class, new IntegerConver());
     CONVER_MAP.put(Long.TYPE, new LongConver(Long.valueOf(0L)));
     CONVER_MAP.put(Long.class, new LongConver());
     CONVER_MAP.put(String.class, new StringConver());
     CONVER_MAP.put(Date.class, new DateConver());
   }
   
   public static Conver<?> getConver(Class<?> clazz)
   {
     Conver<?> conver = (Conver)CONVER_MAP.get(clazz);
     if (conver == null) {
       conver = new StringConver();
     }
     return conver;
   }
 }



