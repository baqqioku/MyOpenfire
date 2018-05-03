package com.meilele.staff.business.common;

public class StringConver extends AbstractConver<String> {
	public StringConver() {
	}

	public StringConver(String defvalue) {
		super.setDefvalue(defvalue);
	}

	protected String converObject(Object obj) {
		String result = null;
		try {
			if (obj != null) {
				if ((obj instanceof String)) {
					result = (String) obj;
				} else {
					result = String.valueOf(obj);
				}
			}
		} catch (Exception ex) {
		}
		return result;
	}

	protected String[] getArray(int length) {
		return new String[length];
	}
}
/* Location:           C:\Users\Administrator\Desktop\openfire\plugins\mllStaff\lib\mllStaff-lib.jar
 * Qualified Name:     com.meilele.staff.business.common.StringConver
 * JD-Core Version:    0.7.0.1
 */