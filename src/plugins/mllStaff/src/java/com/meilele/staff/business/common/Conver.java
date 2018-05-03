package com.meilele.staff.business.common;

public abstract interface Conver<T> {
	public abstract T conver(Object paramObject);

	public abstract T conver(Object paramObject, T paramT);

	public abstract T[] converArray(Object paramObject);
}
