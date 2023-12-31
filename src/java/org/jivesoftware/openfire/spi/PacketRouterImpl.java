/**
 * $RCSfile$
 * $Revision: 943 $
 * $Date: 2005-02-04 01:53:20 -0300 (Fri, 04 Feb 2005) $
 *
 * Copyright (C) 2005-2008 Jive Software. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.openfire.spi;

import java.util.Date;

import org.dom4j.Element;
import org.jivesoftware.openfire.*;
import org.jivesoftware.openfire.container.BasicModule;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketExtension;
import org.xmpp.packet.Presence;

/**
 * An uber router that can handle any packet type.<p>
 *
 * The interface is provided primarily as a convenience for services
 * that must route all packet types (e.g. s2s routing, e2e encryption, etc).
 *
 * @author Iain Shigeoka
 */
public class PacketRouterImpl extends BasicModule implements PacketRouter {

    private IQRouter iqRouter;
    private PresenceRouter presenceRouter;
    private MessageRouter messageRouter;

    /**
     * Constructs a packet router.
     */
    public PacketRouterImpl() {
        super("XMPP Packet Router");
    }

    /**
     * Routes the given packet based on packet recipient and sender. The
     * router defers actual routing decisions to other classes.
     * <h2>Warning</h2>
     * Be careful to enforce concurrency DbC of concurrent by synchronizing
     * any accesses to class resources.
     *
     * @param packet The packet to route
     */
    @Override
    public void route(Packet packet) {
        if (packet instanceof Message) {
            route((Message)packet);
        }
        else if (packet instanceof Presence) {
            route((Presence)packet);
        }
        else if (packet instanceof IQ) {
            route((IQ)packet);
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void route(IQ packet) {
        iqRouter.route(packet);
    }

    @Override
    public void route(Message packet) {
    	
    	/*  87 */     if (packet != null)
    	/*     */     {
    	/*  90 */       PacketExtension packetExtension = new PacketExtension("properties", "http://www.jivesoftware.com/xmlns/xmpp/properties");
    	/*  91 */       Element root = packetExtension.getElement();
    	/*  92 */       Element propertyElement = root.addElement("property");
    	/*  93 */       Element nameElement = propertyElement.addElement("name");
    	/*  94 */       Element valueElement = propertyElement.addElement("value");
    	/*  95 */       nameElement.setText("datetime");
    	/*  96 */       valueElement.addAttribute("type", "string");
    	/*  97 */       valueElement.setText(String.valueOf(new Date().getTime()));
    	/*  98 */       packet.addExtension(packetExtension);
    	/*     */     }
        messageRouter.route(packet);
    }

    @Override
    public void route(Presence packet) {
        presenceRouter.route(packet);
    }

    @Override
	public void initialize(XMPPServer server) {
        super.initialize(server);
        iqRouter = server.getIQRouter();
        messageRouter = server.getMessageRouter();
        presenceRouter = server.getPresenceRouter();
    }
}