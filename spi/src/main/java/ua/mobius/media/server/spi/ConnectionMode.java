/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/*
 * 15/07/13 - Change notice:
 * This file has been modified by Mobius Software Ltd.
 * For more information please visit http://www.mobius.ua
 */
package ua.mobius.media.server.spi;

import ua.mobius.media.server.utils.Text;



/**
 * This enum represent connection mode:
 * <ul>
 * <li>INACTIVE - This is default mode where there is not tx or rx of media</li>
 * <li>SEND_ONLY - only send</li>
 * <li>RECV_ONLY - only receive</li>
 * <li>SEND_RECV - send and receive</li>
 * <ul>
 * 
 * @author baranowb
 * @author amit bhayani
 * @kulikov
 */
public enum ConnectionMode {	
    
		INACTIVE, 
        SEND_ONLY,
        RECV_ONLY,
        SEND_RECV,
        CONFERENCE,
        NETWORK_LOOPBACK,
        LOOPBACK,
        CONTINUITY_TEST,
        NETWORK_CONTINUITY_TEST;

        public static ConnectionMode valueOf(Text v) {
            if (v.equals(inactive)) {
                return INACTIVE;
            } else if (v.equals(send_only)) {
                return SEND_ONLY;
            } else if (v.equals(recv_only)) {
                return RECV_ONLY;
            } else if (v.equals(send_recv)) {
                return SEND_RECV;
            } else if (v.equals(network_loopback)) {
                return NETWORK_LOOPBACK;
            } else if (v.equals(loopback)) {
                return LOOPBACK;
            } else if (v.equals(conference)) {
                return CONFERENCE;
            } else if (v.equals(continuity_test)) {
                return CONTINUITY_TEST;
            } else if (v.equals(network_continuity_test)) {
                return NETWORK_CONTINUITY_TEST;
            }
            
            return null;
        }
        
    private final static Text inactive = new Text("inactive");
    private final static Text send_only = new Text("sendonly");
    private final static Text recv_only = new Text("recvonly");
    private final static Text send_recv= new Text("sendrecv");
    private final static Text network_loopback = new Text("netwloop");
    private final static Text loopback = new Text("loopback");
    private final static Text conference  = new Text("confrnce");
    private final static Text continuity_test  = new Text("conttest");
    private final static Text network_continuity_test  = new Text("netwtest");
        
}
