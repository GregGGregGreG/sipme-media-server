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
package ua.mobius.media.server.mgcp.pkg.au;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.Iterator;
import org.apache.log4j.Logger;
import ua.mobius.media.ComponentType;
import ua.mobius.media.server.mgcp.controller.signal.Event;
import ua.mobius.media.server.mgcp.controller.signal.NotifyImmediately;
import ua.mobius.media.server.mgcp.controller.signal.Signal;
import ua.mobius.media.server.spi.Connection;
import ua.mobius.media.server.spi.Endpoint;
import ua.mobius.media.server.spi.MediaType;
import ua.mobius.media.server.spi.ResourceUnavailableException;
import ua.mobius.media.server.spi.player.Player;
import ua.mobius.media.server.spi.player.PlayerEvent;
import ua.mobius.media.server.spi.player.PlayerListener;
import ua.mobius.media.server.spi.listener.TooManyListenersException;
import ua.mobius.media.server.utils.Text;

import java.util.concurrent.Semaphore;

/**
 * Implements play announcement signal.
 * 
 * @author yulian oifa
 */
public class Play extends Signal implements PlayerListener {
    
    private Event oc = new Event(new Text("oc"));
    private Event of = new Event(new Text("of"));
    
    private Player player;
    private volatile Options options;
    
    private int repeatCount;
    private int segCount;
    
    
    private long delay;
    private String uri;
    
    private Iterator<Text> segments;
    private final static Logger logger = Logger.getLogger(Play.class);
    
    private Semaphore terminateSemaphore=new Semaphore(1);
    
    public Play(String name) {
        super(name);
        oc.add(new NotifyImmediately("N"));
        of.add(new NotifyImmediately("N"));                
    }
    
    @Override
    public void execute() {
    	//get access to player
        player = this.getPlayer();

        //check result
        if (player == null) {
            of.fire(this, new Text("Endpoint has no player"));
            complete();
            return;
        }
        
        //register announcement handler
        try {
            player.addListener(this);
        } catch (TooManyListenersException e) {
            logger.error("OPERATION FAILURE", e);
        } 
        
        //get options of the request
        options = Options.allocate(getTrigger().getParams());        
                
        //set initial delay
        delay = 0;
        
        //get announcement segments
        segments = options.getSegments().iterator();
        repeatCount = options.getRepeatCount();
        segCount=0;
        
        uri = segments.next().toString();
        
        //start announcement
        startAnnouncementPhase();        
    }

    private void startAnnouncementPhase() {
        logger.info(String.format("(%s) Start announcement (segment=%d)", getEndpoint().getLocalName(), segCount));
        
        try {
            player.setURL(uri);
        } catch (MalformedURLException e) {
        	logger.info("Received URL in invalid format , firing of");
            of.fire(this, new Text("rc=301"));
            complete();            
            return;
        } catch (ResourceUnavailableException e) {
        	logger.info("Received URL can not be found , firing of");
            of.fire(this, new Text("rc=312"));
            complete();
            return;
        }
        
        //set max duration if present
        if (options.getDuration() != -1) {
            player.setDuration(options.getDuration());
        }

        //set initial offset
        if (options.getOffset() > 0) {
            player.setMediaTime(options.getOffset());
        }

        //initial delay
        player.setInitialDelay(delay);

        //starting
        player.activate();
    }    
    
    @Override
    public boolean doAccept(Text event) {
        if (!oc.isActive() && oc.matches(event)) {
            return true;
        }

        if (!of.isActive() && of.matches(event)) {
            return true;
        }
        
        return false;
    }

    @Override
    public void cancel() {
    	terminate();                
    }

    private Player getPlayer() {
    	Endpoint endpoint = getEndpoint();
        return (Player) endpoint.getResource(MediaType.AUDIO, ComponentType.PLAYER);
    }
        
    @Override
    public void reset() {
        super.reset();
        terminate();
        
        oc.reset();
        of.reset();
        
    }
    
    private void terminate() 
    {    	
    	try
    	{
    		terminateSemaphore.acquire();
    	}
    	catch(InterruptedException e)
    	{
    		
    	}
    	
    	if (player != null) 
    	{    		
    		player.removeListener(this);
    		player.deactivate();
    		player=null;
        }    	    	
    	
    	if(options!=null)
    	{
    		Options.recycle(options);    	
    		options=null;
    	}
    	
    	terminateSemaphore.release();    	    	
    }
    
    private void repeat(long delay) {
        this.delay = delay;
        startAnnouncementPhase();
    }
    
    private void next(long delay) {
        uri = segments.next().toString();
        segCount++;
        
        this.delay = delay;
        startAnnouncementPhase();
    }

    public void process(PlayerEvent event) {
        switch (event.getID()) {
            case PlayerEvent.STOP :
                logger.info(String.format("(%s) Announcement (segment=%d) has completed", getEndpoint().getLocalName(), segCount));
                if(repeatCount==-1)
                {
                	repeat(options.getInterval());
            		return;            		                	
                }
                else
                {
                	repeatCount--;
                    
                	if (repeatCount > 0) {
                		repeat(options.getInterval());
                		return;
                	}
                }
                
                if (segments.hasNext()) {
                	repeatCount = options.getRepeatCount();
                    next(options.getInterval());
                    return;
                }
                
                terminate();
                oc.fire(this, new Text("rc=100"));
                this.complete();
                
                break;
            case PlayerEvent.FAILED :
            	terminate();
                of.fire(this, null);
                this.complete();
        }
    }        
}
