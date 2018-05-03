package com.meilele.staff.business.common;

public class BooleanConver extends AbstractConver<Boolean> {
	public BooleanConver() {
	}

	public BooleanConver(Boolean defvalue) {
		super.setDefvalue(defvalue);
	}

	protected Boolean converObject(Object obj) {
		Boolean result = null;
		try {
			if (obj != null) {
				if ((obj instanceof Boolean)) {
					result = (Boolean) obj;
				} else {
					result = Boolean.valueOf(Boolean.parseBoolean(obj.toString()));
				}
			}
		} catch (Exception ex) {
		}
		return result;
	}

	protected Boolean[] getArray(int length) {
		return new Boolean[length];
	}
}

/* Location:           C:\Users\Administrator\Desktop\openfire\plugins\mllStaff\lib\mllStaff-lib.jar
 * Qualified Name:     com.meilele.staff.business.common.BooleanConver
 * JD-Core Version:    0.7.0.1
 */