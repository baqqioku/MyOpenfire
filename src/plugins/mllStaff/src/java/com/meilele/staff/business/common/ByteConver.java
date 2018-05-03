package com.meilele.staff.business.common;

public class ByteConver extends AbstractConver<Integer> {
	public ByteConver() {
	}

	public ByteConver(Integer defvalue) {
		super.setDefvalue(defvalue);
	}

	protected Integer converObject(Object obj) {
		Integer result = null;
		try {
			if (obj != null) {
				if ((obj instanceof Integer)) {
					result = (Integer) obj;
				} else {
					result = Integer.valueOf(Integer.parseInt(obj.toString()));
				}
			}
		} catch (Exception ex) {
		}
		return result;
	}

	protected Integer[] getArray(int length) {
		return new Integer[length];
	}
}

/* Location:           C:\Users\Administrator\Desktop\openfire\plugins\mllStaff\lib\mllStaff-lib.jar
 * Qualified Name:     com.meilele.staff.business.common.ByteConver
 * JD-Core Version:    0.7.0.1
 */