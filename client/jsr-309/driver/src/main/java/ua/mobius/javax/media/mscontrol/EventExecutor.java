/*
 * 15/07/13 - Change notice:
 * This file has been modified by Mobius Software Ltd.
 * For more information please visit http://www.mobius.ua
 */
package ua.mobius.javax.media.mscontrol;

import javax.media.mscontrol.MediaEvent;
import javax.media.mscontrol.MediaEventListener;

/**
 * 
 * @author amit bhayani
 *
 */
public class EventExecutor implements Runnable {
	private MediaEventListener mediaEventListener = null;
	private MediaEvent mediaEvent = null;

	public EventExecutor(MediaEventListener mediaEventListener, MediaEvent mediaEvent) {
		this.mediaEventListener = mediaEventListener;
		this.mediaEvent = mediaEvent;
	}

	public void run() {
		this.mediaEventListener.onEvent(this.mediaEvent);
	}

}
