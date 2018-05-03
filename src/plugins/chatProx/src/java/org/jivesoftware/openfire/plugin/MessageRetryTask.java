package org.jivesoftware.openfire.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageRetryTask implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageRetryTask.class);

	public void run() {
		try {
			LOGGER.info("Retry send msg!");
			MessageBufferHandler.getHandler().retryOnce();
		} catch (Exception e) {
			LOGGER.info("Catch retry send msg error!");
		}
	}
}
