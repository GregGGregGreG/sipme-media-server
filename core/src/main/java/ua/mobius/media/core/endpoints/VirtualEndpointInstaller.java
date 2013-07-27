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
package ua.mobius.media.core.endpoints;

import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicInteger;

import ua.mobius.media.core.Server;
import ua.mobius.media.core.naming.EndpointNameGenerator;
import ua.mobius.media.server.spi.Endpoint;
import ua.mobius.media.server.spi.EndpointInstaller;
import ua.mobius.media.server.spi.ResourceUnavailableException;
import ua.mobius.media.server.spi.dsp.DspFactory;

/**
 * Endpoint installer is used for automatic creation and instalation of endpoints.
 *
 * It uses three parameters: the name pattern, class name and configuration
 * @author yulian oifa
 */
public class VirtualEndpointInstaller implements EndpointInstaller {

    private String namePattern;    
    private String endpointClass;
    protected Integer initialSize;
    
    protected EndpointNameGenerator nameParser;
    protected Server server;

    protected AtomicInteger lastEndpointID=new AtomicInteger(1);
    
    /**
     * Creates new endpoint installer.
     */
    public VirtualEndpointInstaller() {
        nameParser = new EndpointNameGenerator();
    }

    /**
     * Creates relation with server instance.
     * 
     * @param server the server instance.
     */
    public void setServer(Server server) {
        this.server = server;
    }

    /**
     * Gets the pattern used for generating endpoint name.
     *
     * @return text pattern
     */
    public String getNamePattern() {
        return namePattern;
    }

    /**
     * Sets the pattern used for generating endpoint name.
     *
     * @param namePattern the pattern text.
     */
    public void setNamePattern(String namePattern) {
        this.namePattern = namePattern;
    }

    /**
     * Gets the name of the class implementing endpoint.
     * 
     * @return the fully qualified class name.
     */
    public String getEndpointClass() {
        return this.endpointClass;
    }

    /**
     * Sets the name of the class implementing endpoint.
     *
     * @param endpointClass the fully qualified class name.
     */
    public void setEndpointClass(String endpointClass) {
        this.endpointClass = endpointClass;
    }    
    
    /**
     * Gets the initial size of endpoints pool
     * 
     * @return initial size
     */
    public Integer getInitialSize() {
        return this.initialSize;
    }

    /**
     * Sets the initial size of endpoints pool
     *
     * @param initial size
     */
    public void setInitialSize(Integer initialSize) {
        this.initialSize = initialSize;
    }    
    
    /**
     * (Non Java-doc.)
     *
     * @throws ResourceUnavailableException
     */
    public void install() {
        ClassLoader loader = Server.class.getClassLoader();
        for(int i=0;i<initialSize;i++)
        	newEndpoint();                    
    }

    public void newEndpoint()
    {
    	ClassLoader loader = Server.class.getClassLoader();
        nameParser.setPattern(namePattern);
        try {
            Constructor constructor = loader.loadClass(this.endpointClass).getConstructor(String.class);
            Endpoint endpoint = (Endpoint) constructor.newInstance(namePattern + lastEndpointID.getAndIncrement());
            server.install(endpoint,this);
        } catch (Exception e) {
            server.logger.error("Couldn't instantiate endpoint", e);
        }                
    }
    
    public boolean canExpand() 
    {
    	return true;
    }
    
    public void uninstall() {
    }

}
