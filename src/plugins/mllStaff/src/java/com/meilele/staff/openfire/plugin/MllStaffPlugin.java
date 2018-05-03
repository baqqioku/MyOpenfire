package com.meilele.staff.openfire.plugin;

import com.meilele.staff.openfire.iqHandler.IQQueryStaffHandler;
import java.io.File;
import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MllStaffPlugin implements Plugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(MllStaffPlugin.class);

	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		LOGGER.info("add iq query staff handler!");

		XMPPServer.getInstance().getIQRouter().addHandler(IQQueryStaffHandler.getInstance());
	}

	public void destroyPlugin() {
		LOGGER.info("remove iq query staff handler!");

		XMPPServer.getInstance().getIQRouter().removeHandler(IQQueryStaffHandler.getInstance());
	}
 }

