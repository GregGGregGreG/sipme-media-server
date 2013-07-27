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
package ua.mobius.media.server.impl.rtp.sdp;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import ua.mobius.media.server.utils.Text;

/**
 * Session Descriptor.
 *
 * @author kulikov
 */
public class SessionDescription {
    protected final static Text AUDIO = new Text("audio");
    protected final static Text VIDEO = new Text("video");

    protected final static Text RTPMAP = new Text("a=rtpmap");
    protected final static Text FMTP = new Text("a=fmtp");

    private Text version;
    private OriginField origin = new OriginField();
    private Text session;
    private ConnectionField connection = new ConnectionField();
    private TimeField time = new TimeField();
    private ArrayList<MediaDescriptorField> mds = new ArrayList(2);

    private MediaDescriptorField audioDescriptor;
    private MediaDescriptorField videoDescriptor;

    private MediaDescriptorField md;

    /**
     * Reads descriptor from binary data
     * @param data the binary data
     * @throws ParseException
     */
    public void parse(byte[] data) throws ParseException {
        //clean previous data
        md = null;
        mds.clear();
        
        Text text = new Text();
        text.strain(data, 0, data.length);

        while (text.hasMoreLines()) {
            Text line = text.nextLine();
            if (line.length() == 0) continue;
            switch (line.charAt(0)) {
                case 'v':
                    Iterator<Text> it = line.split('=').iterator();
                    it.next();

                    version = it.next();
                    version.trim();
                    break;
                case 'o':
                    origin.strain(line);
                    break;
                case 's':
                    it = line.split('=').iterator();
                    it.next();
                    
                    session = it.next();
                    session.trim();
                    break;
                case 'c':
                    if (md == null) {
                        connection.strain(line);
                    } else {
                        md.setConnection(line);
                    }
                    break;
                case 't':
                    time.strain(line);
                    break;
                case 'm':
                    md = new MediaDescriptorField();
                    mds.add(md);
                    md.setDescriptor(line);

                    if (md.getMediaType().equals(AUDIO)) {
                        this.audioDescriptor = md;
                    } else {
                        this.videoDescriptor = md;
                    }
                    break;
                case 'a':
                	if (md != null) {
                		md.addAttribute(line);
                	}
                    break;
            }
        }
    }

    public void init(Text text) throws ParseException {
        //clean previous data
        md = null;
        mds.clear();
        
        while (text.hasMoreLines()) {
            Text line = text.nextLine();
            if (line.length() == 0) continue;
            switch (line.charAt(0)) {
                case 'v':
                    Iterator<Text> it = line.split('=').iterator();
                    it.next();

                    version = it.next();
                    version.trim();
                    break;
                case 'o':
                    origin.strain(line);
                    break;
                case 's':
                    it = line.split('=').iterator();
                    it.next();
                    
                    session = it.next();
                    session.trim();
                    break;
                case 'c':
                    if (md == null) {
                        connection.strain(line);
                    } else {
                        md.setConnection(line);
                    }
                    break;
                case 't':
                    time.strain(line);
                    break;
                case 'm':
                    md = new MediaDescriptorField();
                    mds.add(md);
                    md.setDescriptor(line);

                    if (md.getMediaType().equals(AUDIO)) {
                        this.audioDescriptor = md;
                    } else {
                        this.videoDescriptor = md;
                    }
                    break;
                case 'a':
                	if (md != null) {
                		md.addAttribute(line);
                	}
                    break;
            }
        }
    }
    
    /**
     * Gets version attribute.
     *
     * @return the value of version attribute
     */
    public Text getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = new Text(version);
    }
    
    /**
     * Gets the origin attribute
     *
     * @return origin attribute
     */
    public OriginField getOrigin() {
        return origin;
    }

    public void setOrigin(String name, String sessionID, String sessionVersion,
            String netType, String addressType, String address) {
            this.origin = new OriginField(name, sessionID, sessionVersion,
                    netType, addressType, address);
    }
    
    /**
     * Gets session identifier.
     *
     * @return session identifier value.
     */
    public String getSession() {
        return session.toString();
    }

    /**
     * Gets connection attribute.
     *
     * @return the connection attribute.
     */
    public ConnectionField getConnection() {
        return connection;
    }

    /**
     * Gets the time attribute.
     *
     * @return time attribute.
     */
    public TimeField getTime() {
        return time;
    }

    /**
     * Gets the media attributes.
     *
     * @return collection of media attributes.
     */
    public Collection<MediaDescriptorField> getMedia() {
        return mds;
    }

    /**
     * Gets the description of audio stream.
     *
     * @return the descriptor object.
     */
    public MediaDescriptorField getAudioDescriptor() {
        return this.audioDescriptor;
    }

    /**
     * Gets the description of video stream.
     *
     * @return the descriptor object.
     */
    public MediaDescriptorField getVideoDescriptor() {
        return this.videoDescriptor;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("v=%s\n", version.toString()));
        builder.append(origin.toString());
        return builder.toString();
    }

}
