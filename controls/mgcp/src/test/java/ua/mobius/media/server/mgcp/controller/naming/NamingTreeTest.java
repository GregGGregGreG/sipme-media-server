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
package ua.mobius.media.server.mgcp.controller.naming;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import ua.mobius.media.server.mgcp.controller.MgcpEndpoint;
import ua.mobius.media.server.mgcp.controller.MyTestEndpoint;
import ua.mobius.media.server.mgcp.controller.signal.MgcpPackage;
import ua.mobius.media.server.utils.Text;

/**
 *
 * @author kulikov
 */
public class NamingTreeTest {

    private NamingTree namingTree = new NamingTree();
    
    private MyTestEndpoint endpoint1 = new MyTestEndpoint("mobius/aap/1");
    private MyTestEndpoint endpoint2 = new MyTestEndpoint("mobius/aap/2");
    private MyTestEndpoint endpoint3 = new MyTestEndpoint("mobius/aap/3");
    
    private MgcpEndpoint ac1=new MgcpEndpoint(endpoint1, null, "localhost", 2727, new ArrayList());
    private MgcpEndpoint ac2=new MgcpEndpoint(endpoint2, null, "localhost", 2727, new ArrayList());
    private MgcpEndpoint ac3=new MgcpEndpoint(endpoint3, null, "localhost", 2727, new ArrayList());
   
    private MgcpEndpoint[] res = new MgcpEndpoint[10];
    
    public NamingTreeTest() {    	
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {    
        namingTree.register(ac1,null);
        namingTree.register(ac2,null);
        namingTree.register(ac3,null);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of register method, of class NamingTree.
     */
    @Test
    public void testFindAll() throws UnknownEndpointException {
        int n = namingTree.find(new Text("mobius/aap/*"), res);
        assertEquals(3, n);
    }

    @Test
    public void testFindAny() throws UnknownEndpointException {
        int n = namingTree.find(new Text("mobius/aap/$"), res);
        assertEquals(1, n);
    }

    @Test
    public void testFind() throws UnknownEndpointException {
        int n = namingTree.find(new Text("mobius/aap/1"), res);
        assertEquals(1, n);
    }
    
    @Test
    public void testFindDuration() throws UnknownEndpointException {
        long s = System.nanoTime();
        int n = namingTree.find(new Text("mobius/aap/3"), res);
        System.out.println("COUNT:" + n);
        assertEquals(1, n);
        long f = System.nanoTime();
        
        System.out.println("Duration=" + (f-s));
    }
    
}
