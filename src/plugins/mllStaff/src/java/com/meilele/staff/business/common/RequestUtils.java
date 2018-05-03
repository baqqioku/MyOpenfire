package com.meilele.staff.business.common;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestUtils {
	private static final Logger Log = LoggerFactory.getLogger(RequestUtils.class);

	public static <T> T toJavaBean(Map<?, ?> map, T object, Class<T> clazz) {
		T t = object;
		try {
			if (t == null) {
				t = clazz.newInstance();
			}
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				String methodName = method.getName();
				Type[] types = method.getGenericParameterTypes();
				if ((methodName.startsWith("set")) && (methodName.length() > 3) && (types != null)
						&& (types.length == 1)) {
					try {
						Class<?> pclass = (Class<?>) types[0];
						String fieldName = methodName.toLowerCase().charAt(3) + methodName.substring(4);

						Object value = map.get(fieldName);
						if (value != null) {
							Class<?> _clazz = pclass;
							Object _value = null;
							Conver<?> conver = ConverFactory.getConver(_clazz);
							if (pclass.isArray()) {
								_value = conver.converArray(value);
							} else {
								_value = conver.conver(value);
							}
							method.invoke(t, new Object[] { _value });
						}
					} catch (Exception ex) {
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return t;
	}

	public static <T> Map<String, Object> toModelBean(T object, Class<T> clazz) {
		T t = object;
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			if (t == null) {
				t = clazz.newInstance();
			}
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				String methodName = method.getName();
				if ((methodName.startsWith("get")) && (methodName.length() > 3)) {
					try {
						String fieldName = methodName.toLowerCase().charAt(3) + methodName.substring(4);
						if (!fieldName.equals("class")) {
							Object v = method.invoke(t, new Object[0]);
							if (v != null) {
								map.put(converFieldDb(fieldName), v);
							}
						}
					} catch (Exception ex) {
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return map;
	}

	public static <T> T toPoToBean(T object, T target, Class<T> clazz) {
		T t = object;
		try {
			if (t == null) {
				t = clazz.newInstance();
			}
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				String methodName = method.getName();
				if ((methodName.startsWith("get")) && (methodName.length() > 3)) {
					try {
						String fieldName = methodName.toLowerCase().charAt(3) + methodName.substring(4);
						if (!fieldName.equals("class")) {
							Object v = method.invoke(t, new Object[0]);
							if (v != null) {
								BeanUtils.setProperty(target, fieldName, v);
							}
						}
					} catch (Exception ex) {
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return target;
	}

	public static <T> T toJavaResultBean(ResultSet rs, T object, Class<T> clazz) {
		T t = object;
		try {
			if (t == null) {
				t = clazz.newInstance();
			}
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				String methodName = method.getName();
				Type[] types = method.getGenericParameterTypes();
				if ((methodName.startsWith("set")) && (methodName.length() > 3) && (types != null)
						&& (types.length == 1)) {
					try {
						Class<?> pclass = (Class<?>) types[0];
						String fieldName = methodName.toLowerCase().charAt(3) + methodName.substring(4);

						Object value = rs.getObject(converFieldDb(fieldName));
						if (value != null) {
							Class<?> _clazz = pclass;
							Object _value = null;
							Conver<?> conver = ConverFactory.getConver(_clazz);
							if (pclass.isArray()) {
								_value = conver.converArray(value);
							} else {
								_value = conver.conver(value);
							}
							method.invoke(t, new Object[] { _value });
						}
					} catch (Exception ex) {
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return t;
	}

	public static Map<String, String> getRequest(HttpServletRequest request) {
		Map<String, String> map = new HashMap<String, String>();
		Map<String, String[]> paramMap = request.getParameterMap();
		for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
			String paramName = (String) entry.getKey();
			String paramValue = "";
			String[] paramValueArr = (String[]) entry.getValue();
			for (int i = 0; (paramValueArr != null) && (i < paramValueArr.length); i++) {
				if (i == paramValueArr.length - 1) {
					paramValue = paramValue + paramValueArr[i];
				} else {
					paramValue = paramValue + paramValueArr[i] + ",";
				}
			}
			map.put(paramName, paramValue);
		}
		return map;
	}

	public static String converField(String fieldName) {
		if (fieldName.indexOf("_") > 0) {
			fieldName = fieldName.substring(0, fieldName.indexOf("_"))
					+ toUpperCaseFirstOne(fieldName.substring(fieldName.indexOf("_") + 1, fieldName.length()));
		}
		if (fieldName.indexOf("_") > 0) {
			fieldName = converField(fieldName);
		}
		return fieldName;
	}

	public static String toUpperCaseFirstOne(String str) {
		StringBuffer sb = new StringBuffer(str);
		sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
		str = sb.toString();
		return str;
	}

	public static int getCasePostion(String word) {
		for (int i = 0; i < word.length(); i++) {
			char c = word.charAt(i);
			if ((isEnglish(c)) && (!Character.isLowerCase(c))) {
				return i;
			}
		}
		return 0;
	}

	public static boolean isEnglish(char word) {
		if (((word < 'A') || (word > 'Z')) && ((word < 'a') || (word > 'z'))) {
			return false;
		}
		return true;
	}

	public static String converFieldDb(String fieldname) {
		if (getCasePostion(fieldname) > 0) {
			fieldname = fieldname.substring(0, getCasePostion(fieldname)) + "_"
					+ fieldname.substring(getCasePostion(fieldname), getCasePostion(fieldname) + 1).toLowerCase()
					+ fieldname.substring(getCasePostion(fieldname) + 1, fieldname.length());
		}
		return fieldname;
	}
}
