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
package ua.mobius.media.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.apache.log4j.Logger;

import ua.mobius.media.core.endpoints.BaseEndpointImpl;
import ua.mobius.media.core.endpoints.VirtualEndpointInstaller;
import ua.mobius.media.core.naming.NamingService;
import ua.mobius.media.server.io.network.UdpManager;
import ua.mobius.media.server.scheduler.Clock;
import ua.mobius.media.server.scheduler.Scheduler;
import ua.mobius.media.server.scheduler.Task;
import ua.mobius.media.server.spi.Endpoint;
import ua.mobius.media.server.spi.EndpointInstaller;
import ua.mobius.media.server.spi.MediaServer;
import ua.mobius.media.server.spi.ResourceUnavailableException;
import ua.mobius.media.server.spi.ServerManager;

/**
 *
 * @author Oifa Yulian
 */
public class Server implements MediaServer {

    //timing clock
    private Clock clock;

    //job scheduler
    private Scheduler scheduler;

    //resources pool
    private ResourcesPool resourcesPool;
    
    //udp manager
    private UdpManager udpManager;
    
    private NamingService namingService;
    
    //endpoint installers
    private ArrayList<EndpointInstaller> installers = new ArrayList();
    
    //endpoints
    private HashMap<String, Endpoint> endpoints = new HashMap();
    
    //managers
    private ArrayList<ServerManager> managers = new ArrayList();
    
    private HeartBeat heartbeat;
    private int heartbeatTime=0;
    private volatile long ttl;
    
    public  Logger logger = Logger.getLogger(Server.class);
    
    public Server() {
        namingService = new NamingService();
    }

   
    /**
     * Assigns clock instance.
     *
     * @param clock
     */
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
    
    public void setUdpManager(UdpManager udpManager) {
        this.udpManager=udpManager;
    }
    
    public void setResourcesPool(ResourcesPool resourcesPool) {
        this.resourcesPool=resourcesPool;
    }
    
    /**
     * Assigns the heartbeat time in minutes
     *
     * @param minutes
     */
    public void setHeartBeatTime(int heartbeatTime) {
        this.heartbeatTime = heartbeatTime;
    }    

    /**
     * Installs endpoints defined by specified installer.
     *
     * @param installer the endpoints installer
     */
    public void addInstaller(EndpointInstaller installer) {
    	((VirtualEndpointInstaller)installer).setServer(this);
    	installers.add(installer);
        installer.install();        
    }

    /**
     * Uninstalls endpoint defined by specified endpoint installer.
     *
     * @param installer the endpoints installer.
     */
    public void removeInstaller(EndpointInstaller installer) {
        installers.remove(installer);
        installer.uninstall();
    }

    /**
     * Installs the specified endpoint.
     *
     * @param endpoint the endpoint to installed.
     */
    public void install(Endpoint endpoint,EndpointInstaller installer) {
        //check endpoint first
        if (endpoint == null) {
            logger.error("Unknown endpoint");
            return;
        }

        //The endpoint implementation must extend BaseEndpointImpl class
        BaseEndpointImpl baseEndpoint = null;
        try {
            baseEndpoint = (BaseEndpointImpl) endpoint;
        } catch (ClassCastException e) {
            logger.error("Unsupported endpoint implementation " + endpoint.getLocalName());
            return;
        }


        //assign scheduler to the endpoint
        baseEndpoint.setScheduler(scheduler);
        baseEndpoint.setResourcesPool(resourcesPool);

        logger.info("Installing " + endpoint.getLocalName());

        //starting endpoint
        try {
            endpoint.start();
        } catch (Exception e) {
            logger.error("Couldn't start endpoint " + endpoint.getLocalName(), e);
            return;
        }

        //register endpoint with naming service
        try {
            namingService.register(endpoint);
        } catch (Exception e) {
            endpoint.stop();
            logger.error("Could not register endpoint " + endpoint.getLocalName(), e);
        }
        
        //register endpoint localy
        endpoints.put(endpoint.getLocalName(), endpoint);
        
        //send notification to manager
        for (ServerManager manager : managers) {
            manager.onStarted(endpoint,installer);
        }
    }


    /**
     * Uninstalls the endpoint.
     *
     * @param name the local name of the endpoint to be uninstalled
     */
    public void uninstalls(String name) {
        //unregister localy
        Endpoint endpoint = endpoints.remove(name);

        //send notification to manager
        for (ServerManager manager : managers) {
            manager.onStopped(endpoint);
        }
        
        try {
            //TODO: lookup irrespective of endpoint usage
            endpoint = namingService.lookup(name, true);
            if (endpoint != null) {
                endpoint.stop();
                namingService.unregister(endpoint);
            }
        } catch (Exception e) {
        	logger.error(e);
        }
        
    }

    /**
     * Starts the server.
     *
     * @throws Exception
     */
    public void start() throws Exception {
        //check clock
        if (clock == null) {
            logger.error("Timing clock is not defined");
            return;
        }

        if(heartbeatTime>0)
        {
        	heartbeat=new HeartBeat();
        	heartbeat.restart();
        }
    }

    /**
     * Stops the server.
     *
     */
    public void stop() {
        logger.info("Stopping UDP Manager");
        udpManager.stop();

        if(heartbeat!=null)
        	heartbeat.cancel();
        
        logger.info("Stopping scheduler");
        scheduler.stop();
        logger.info("Stopped media server instance ");                
    }

    public Endpoint lookup(String name, boolean bussy) throws ResourceUnavailableException {
        return null;//return namingService.lookup(name, bussy);
    }
    
    public Endpoint[] lookupall(String endpointName) throws ResourceUnavailableException {
    	return null;//return namingService.lookupall(endpointName);
    }

    public int getEndpointCount() {
        return 0;//return namingService.getEndpointCount();
    }
    
    public Collection<Endpoint> getEndpoints() {
        return endpoints.values();
    }

    /**
     * (Non Java-doc.)
     * 
     * @see ua.mobius.media.server.spi.MediaServer#addManager(ua.mobius.media.server.spi.ServerManager) 
     */
    public void addManager(ServerManager manager) {
        managers.add(manager);
    }

    /**
     * (Non Java-doc.)
     * 
     * @see ua.mobius.media.server.spi.MediaServer#removeManager(ua.mobius.media.server.spi.ServerManager) 
     */
    public void removeManager(ServerManager manager) {
        managers.remove(manager);
    }
    
    private class HeartBeat extends Task {

        public HeartBeat() {
            super();
        }        

        public int getQueueNumber()
        {
        	return scheduler.HEARTBEAT_QUEUE;
        }   
        
        public void restart()
        {
        	ttl=heartbeatTime*600;
        	scheduler.submitHeatbeat(this);
        }
        
        @Override
        public long perform() {
        	ttl--;
            if (ttl == 0) {
            	logger.info("Global hearbeat is still alive");
            	restart();
            } else {
                scheduler.submitHeatbeat(this);
            }            
            return 0;
        }
    }
}
