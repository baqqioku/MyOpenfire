/*   1:    */ package com.meilele.staff.business.common;
/*   2:    */ 
/*   3:    */ import java.lang.reflect.ParameterizedType;
/*   4:    */ 
/*   5:    */ public abstract class AbstractConver<T>
/*   6:    */   implements Conver<T>
/*   7:    */ {
	private Class<T> entityClass;
	private T defvalue;

	public AbstractConver() {
		ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
		this.entityClass = (Class<T>) type.getActualTypeArguments()[0];
	}

	public T conver(Object obj) {
		Object _obj = array2Object(obj);
		T result = converObject(_obj);
		if (result == null) {
			result = this.defvalue;
		}
		return result;
	}

	public T conver(Object obj, T defvalue) {
		Object _obj = array2Object(obj);
		T result = converObject(_obj);
		if (result == null) {
			result = defvalue;
		}
		return result;
	}

	public T[] converArray(Object obj) {
		T[] result = null;
		if (obj != null) {
			if (obj.getClass().isArray()) {
				Object[] _array = (Object[]) obj;
				result = getArray(_array.length);
				for (int i = 0; i < _array.length; i++) {
					result[i] = converObject(_array[i]);
				}
			} else {
				result = getArray(1);
				result[0] = converObject(obj);
			}
		}
		return result;
	}

	protected Object array2Object(Object obj) {
		if ((obj != null) && (obj.getClass().isArray())) {
			if (((Object[]) obj).length > 0) {
				obj = ((Object[]) (Object[]) obj)[0];
			} else {
				obj = null;
			}
		}
		return obj;
	}

	protected abstract T converObject(Object paramObject);

	protected abstract T[] getArray(int paramInt);

	public Class<T> getEntityClass() {
		return this.entityClass;
	}

	public T getDefvalue() {
		return this.defvalue;
	}

	public void setDefvalue(T defvalue) {
		this.defvalue = defvalue;
	}
 }


/* Location:           C:\Users\Administrator\Desktop\openfire\plugins\mllStaff\lib\mllStaff-lib.jar
 * Qualified Name:     com.meilele.staff.business.common.AbstractConver
 * JD-Core Version:    0.7.0.1
 */