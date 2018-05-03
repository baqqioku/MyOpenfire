package org.jivesoftware.openfire.plugin;

public class MllOfUserStatus {
	private Long id;
	private String ConnectionType;
	private String username;
	private String resources;
	private String online;
	private String IpAddress;
	private Long startTime;
	private Long logoutTime;
	private Long createTime;
	private Long updateTime;
	private String todaytime;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getConnectionType() {
		return this.ConnectionType;
	}

	public void setConnectionType(String connectionType) {
		this.ConnectionType = connectionType;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getResources() {
		return this.resources;
	}

	public void setResources(String resources) {
		this.resources = resources;
	}

	public String getOnline() {
		return this.online;
	}

	public void setOnline(String online) {
		this.online = online;
	}

	public String getIpAddress() {
		return this.IpAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.IpAddress = ipAddress;
	}

	public Long getStartTime() {
		return this.startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getLogoutTime() {
		return this.logoutTime;
	}

	public void setLogoutTime(Long logoutTime) {
		this.logoutTime = logoutTime;
	}

	public Long getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public Long getUpdateTime() {
		return this.updateTime;
	}

	public void setUpdateTime(Long updateTime) {
		this.updateTime = updateTime;
	}

	public String getTodaytime() {
		return this.todaytime;
	}

	public void setTodaytime(String todaytime) {
		this.todaytime = todaytime;
	}
}
