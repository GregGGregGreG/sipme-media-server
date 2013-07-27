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

package ua.mobius.media.server.mgcp.controller;

import java.io.IOException;
import ua.mobius.media.server.spi.Connection;
import ua.mobius.media.server.spi.ConnectionMode;
import ua.mobius.media.server.spi.ConnectionFailureListener;
import ua.mobius.media.server.spi.MediaType;
import ua.mobius.media.server.spi.ModeNotSupportedException;
import ua.mobius.media.server.utils.Text;

import ua.mobius.media.server.mgcp.MgcpEvent;
import ua.mobius.media.server.mgcp.message.MgcpRequest;
import ua.mobius.media.server.mgcp.message.Parameter;

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * Represents the connection activity.
 * 
 * @author yulian oifa
 */
public class MgcpConnection implements ConnectionFailureListener {
	public static AtomicInteger connectionID=new AtomicInteger(1);
    
	public final static Text REASON_CODE = new Text("902 Loss of lower layer connectivity");
	protected Integer id;
    protected Text textualId;
    protected MgcpCall call;
    protected MgcpEndpoint mgcpEndpoint;
    protected Connection connection;
    private SocketAddress callAgent;
    private Text descriptor = new Text();    
    public MgcpConnection() 
    {
        id = connectionID.getAndIncrement();
        textualId=new Text(Integer.toHexString(id));
    }
    
    public int getID() {
        return id;
    }

    public Text getTextualID() {
        return textualId;
    }
    
    /**
     * Assigns call object to which this connection belongs.
     * 
     * @param call the call object.
     */
    protected void setCall(MgcpCall call) {
        this.call = call;
        call.connections.put(this.id,this);
    }
    
    public void setCallAgent(SocketAddress callAgent) {
    	this.callAgent=callAgent;
    }
    
    public void wrap(MgcpEndpoint mgcpEndpoint, MgcpCall call, Connection connection) {
    	this.mgcpEndpoint=mgcpEndpoint;
        this.call = call;
        this.connection = connection;
        this.connection.setConnectionFailureListener(this);        
        call.connections.put(this.id,this);
    }
    
    public void setMode(Text mode) throws ModeNotSupportedException {
    	connection.setMode(ConnectionMode.valueOf(mode));
    }

    public void setMode(ConnectionMode mode) throws ModeNotSupportedException {
        connection.setMode(mode);
    }
    
    public void setDtmfClamp(boolean dtmfClamp) {
        //connection.setDtmfClamp(dtmfClamp);
    }
    
    public Text getDescriptor() {
    	if(connection.getDescriptor()!=null)
    	{	
    		descriptor.strain(connection.getDescriptor().getBytes(), 0, connection.getDescriptor().length());
    		return descriptor;
    	}        
    	
    	return null;
    }

    public void setOtherParty(Text sdp) throws IOException {
        connection.setOtherParty(sdp);
    }
    
    public void setOtherParty(MgcpConnection other) throws IOException {
        this.connection.setOtherParty(other.connection);
    }
    
    /**
     * Terminates this activity and deletes connection.
     */
    public void release() {
        //notify call about this activity termination
    	call.exclude(this);    	       
    }
    
    public int getPacketsTransmitted() {
        return (int) connection.getPacketsTransmitted();
    }
    
    public int getPacketsReceived() {
        return (int) connection.getPacketsReceived();
    }
    
    public void onFailure() {
    	mgcpEndpoint.offer(this);
    	
    	MgcpEvent evt = (MgcpEvent) mgcpEndpoint.mgcpProvider.createEvent(MgcpEvent.REQUEST, callAgent);
		MgcpRequest msg = (MgcpRequest) evt.getMessage();        
		msg.setCommand(new Text("DLCX"));
		msg.setEndpoint(mgcpEndpoint.fullName);
		msg.setParameter(Parameter.CONNECTION_ID, textualId);
		msg.setTxID(MgcpEndpoint.txID.incrementAndGet());
		msg.setParameter(Parameter.REASON_CODE,this.REASON_CODE);
		mgcpEndpoint.send(evt, callAgent);		
    }
}
