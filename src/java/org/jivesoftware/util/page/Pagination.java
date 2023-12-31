package org.jivesoftware.util.page;

import java.io.Serializable;
import java.util.List;

public class Pagination<T> extends SimplePage implements Serializable, Paginable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<T> list;

	public Pagination() {
	}

	public Pagination(int pageNo, int pageSize, int totalCount) {
		super(pageNo, pageSize, totalCount);
	}

	public Pagination(int pageNo, int pageSize, int totalCount, List<T> list) {
		super(pageNo, pageSize, totalCount);
		this.list = list;
	}

	public int getFirstResult() {
		return (this.pageNo - 1) * this.pageSize;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}

}
