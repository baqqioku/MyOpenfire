package com.meilele.staff.business.common;

import java.util.HashMap;
import java.util.Map;

public enum Action {
	ACTION_ADD("add"), ACTION_BATCHADD("addbatch"), ACTION_UPDATE("update"), ACTION_DELETE("delete"), ACTION_QUERY(
			"query"), ERROR_ACTION("error_action");

	private static final Map<String, Action> ACTION_MAP;
	private String action;

	static {
		ACTION_MAP = new HashMap<String, Action>();
		for (Action action : values()) {
			ACTION_MAP.put(action.getAction(), action);
		}
	}

	private Action(String action) {
		this.action = action;
	}

	public String getAction() {
		return this.action;
	}

	public String toString() {
		return this.action;
	}

	public static Action fromAction(String action) {
		Action actionEnum = (Action) ACTION_MAP.get(action);
		if (actionEnum == null) {
			actionEnum = ERROR_ACTION;
		}
		return actionEnum;
	}

}
