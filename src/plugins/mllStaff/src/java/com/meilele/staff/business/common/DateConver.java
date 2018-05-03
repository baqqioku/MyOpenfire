package com.meilele.staff.business.common;

import java.util.Date;

public class DateConver extends DatetimeConver<Date> {
	public DateConver() {
	}

	public DateConver(Date defvalue) {
		super.setDefvalue(defvalue);
	}

	protected Date converObject(Object obj) {
		Date result = null;
		try {
			if (obj != null) {
				if ((obj instanceof Date)) {
					result = (Date) obj;
				} else {
					result = super.converDate(obj.toString());
				}
			}
		} catch (Exception ex) {
		}
		return result;
	}

	protected Date[] getArray(int length) {
		return new Date[length];
	}
}

/* Location:           C:\Users\Administrator\Desktop\openfire\plugins\mllStaff\lib\mllStaff-lib.jar
 * Qualified Name:     com.meilele.staff.business.common.DateConver
 * JD-Core Version:    0.7.0.1
 */