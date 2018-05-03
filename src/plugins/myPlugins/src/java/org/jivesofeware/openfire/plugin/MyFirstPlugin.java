package org.jivesofeware.openfire.plugin;

import java.io.File;
import java.util.Collection;
import java.util.TimerTask;

import org.jivesoftware.openfire.RoutingTable;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.spi.RoutingTableImpl;
import org.jivesoftware.util.JiveConstants;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.TaskEngine;

public class MyFirstPlugin implements Plugin{
	
	
	private static SessionManager sessionManager ;

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		// TODO Auto-generated method stub
		sessionManager = SessionManager.getInstance();
		/*TaskEngine.getInstance().scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				//Collection<ClientSession> sessionList = routhingTable.getClientsRoutes(false);
				//System.out.println("在线人数："+sessionList.size());
				int count = sessionManager.getUserSessionsCount(false);
				
				System.out.println("在线人数:"+count);
			}
			
		}, JiveConstants.SECOND * 3, JiveConstants.SECOND * 3);*/
	}

	@Override
	public void destroyPlugin() {
		// TODO Auto-generated method stub
		sessionManager.destroy();
	}

}
