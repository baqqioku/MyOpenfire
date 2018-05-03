package org.jivesoftware.openfire.plugin;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OffMessageStoreTask implements Runnable{
	private static final Logger LOGGER = LoggerFactory.getLogger(OffMessageStoreTask.class);

	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		LOGGER.info(" check msg:{}",new Date());
		try{
			MesageOfflineHandler.getHandler().StoreOffMessage();
		} catch (Exception e) {
			LOGGER.info("Catch error:{}",e.getMessage());
		}
	}

}
