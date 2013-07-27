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
package ua.mobius.media.server.impl.resource.mediaplayer.mpeg;

import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * 
 * @author amit bhayani
 *
 */
public class RTPImmediateConstructor extends RTPConstructor {

	public static final int TYPE = 1;

	private int count;
	private byte[] data;

	public RTPImmediateConstructor() {
		super(TYPE);
	}

	@Override
	public int load(RandomAccessFile raAccFile) throws IOException {
		// 1 is for Type + 1 is for count
		int bytesRead = 2;

		count = raAccFile.read();
		data = new byte[count];
		for (int i = 0; i < count; i++) {
			data[i] = raAccFile.readByte();
		}		
 
		bytesRead += count;

		if (bytesRead < 16) {
			// Each Constructor needs to be 16bytes.
			raAccFile.skipBytes(16 - bytesRead);

		}
		return 16;
	}

	public int getCount() {
		return count;
	}

	public byte[] getData() {
		return data;
	}

}
