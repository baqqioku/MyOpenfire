package org.jivesoftware.openfire.plugin;

import java.net.URL;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.jivesoftware.openfire.XMPPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServiceUrlUtils {
	private static final Logger LOG = LoggerFactory.getLogger(ServiceUrlUtils.class);
	private static final String CONFIG_NAME = "serviceUrl.properties";
	private static final String WX_POST = "wx_post";
	private static final String WX_POST_DEFAULT = "http://www.mll3321.com/im/offlinePush";
	private static final String STAFFAPP = "staffapp_post";
	private static final String STAFFAPP_POST_DEFAULT = "http://www.mll3321.com/im/mengOfflinePush";
	//private static final String CHAT_DOMAIN = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
	private static final String CHAT_DOMAIN_NAME = "chatDomainName";
	private static final String SERVER_URL_DEFAULT = "http://chat.jlings.com";
	private static Configuration config;
	
	static {
		LOG.info("init chatProx ServiceUrlUtils Configuration!");
		try {
			LOG.info("chatProx ServiceUrlUtils ClassLoader : {}", ServiceUrlUtils.class.getClassLoader());

			URL url = ServiceUrlUtils.class.getClassLoader().getResource(CONFIG_NAME);
			if (url == null) {
				url = Thread.currentThread().getContextClassLoader().getResource(CONFIG_NAME);
			}
			if (url == null) {
				LOG.error("chatProx ServiceUrlUtils cannot load serviceUrl.properties!");
				config = null;
			} else {
				config = new PropertiesConfiguration(url);
			}
		} catch (ConfigurationException e) {
			LOG.error("chatProx ServiceUrlUtils cannot load serviceUrl.properties", e);
			config = null;
		}
	}

	private static final String getServiceUrlByName(String name, String defaultValue) {
		if (config == null) {
			LOG.warn("chatProx ServiceUrlUtils config is null!");
			return defaultValue;
		}
		return config.getString(name, defaultValue);
	}

	public static final String getWxPostUrl() {
		return getServiceUrlByName(WX_POST, WX_POST_DEFAULT);
	}

	public static final String getStaffAppPostUrl() {
		return getServiceUrlByName(STAFFAPP, STAFFAPP_POST_DEFAULT);
	}

	public static final String getChatDomain() {
		return getServiceUrlByName("chatDomainName", XMPPServer.getInstance().getServerInfo().getXMPPDomain());
	}
	
	
	public static final String getServerUrl() {
		return getServiceUrlByName("server_url", SERVER_URL_DEFAULT);
	}
		

}