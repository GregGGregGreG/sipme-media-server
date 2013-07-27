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
package ua.mobius.media.server.impl;

import java.io.IOException;

import ua.mobius.media.MediaSink;
import ua.mobius.media.server.scheduler.Scheduler;
import ua.mobius.media.server.scheduler.Task;

import org.apache.log4j.Logger;
/**
 * The base implementation of the media sink.
 * 
 * <code>AbstractSource</code> and <code>AbstractSink</code> are implement 
 * general wiring construct. 
 * All media components have to extend one of these classes.
 * 
 * @author Oifa Yulian
 */
public abstract class AbstractSink extends BaseComponent implements MediaSink {

    //shows if component is started or not.
    protected volatile boolean started = false;
    
    //transmission statisctics
    protected volatile long rxPackets;
    protected volatile long rxBytes;    
    
    protected static final Logger logger = Logger.getLogger(AbstractSink.class);
    
    /**
     * Creates new instance of sink with specified name.
     * 
     * @param name the name of the sink to be created.
     */
    public AbstractSink(String name) {
        super(name);               
    }        

    /**
     * (Non Java-doc).
     * 
     * @see ua.mobius.media.MediaSink#isStarted().
     */
    public boolean isStarted() {
        return this.started;
    }

    /**
     * (Non Java-doc).
     * 
     * @see ua.mobius.media.MediaSink#start().
     */
    public void start() {
    	if (started) {
			return;
		}

		//change state flag
		started = true;
		
		this.rxBytes = 0;
		this.rxPackets = 0;

		//send notification to component's listener
		started();		    	
    }    
    
    /**
     * (Non Java-doc).
     * 
     * @see ua.mobius.media.MediaSink#stop().
     */
    public void stop() {
    	started = false;
		stopped();    	
    }

    public abstract void activate();
    
    public abstract void deactivate();
    
    /**
     * Sends failure notification.
     * 
     * @param eventID failure event identifier.
     * @param e the exception caused failure.
     */
    protected void failed(Exception e) {
    }

    /**
     * (Non Java-doc).
     * 
     * @see ua.mobius.media.MediaSink#getPacketsReceived().
     */
    public long getPacketsReceived() {
        return rxPackets;
    }

    /**
     * (Non Java-doc).
     * 
     * @see ua.mobius.media.MediaSink#getBytesReceived() 
     */
    public long getBytesReceived() {
        return rxBytes;
    }

    @Override
    public void reset() {
        this.rxPackets = 0;
        this.rxBytes = 0;        
    }

    /**
     * Sends notification that media processing has been started.
     */
    protected void started() {
    }

    /**
     * Sends notification that detection is terminated.
     * 
     */
    protected void stopped() {
    }    

    public String report() {
    	return "";
    }        
}
