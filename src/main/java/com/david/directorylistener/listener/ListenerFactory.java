package com.david.directorylistener.listener;

public class ListenerFactory {

	public static <L extends IListener> L getListener(Class<L> listenerClass) {
		L listener = null;
		
		try {
			listener = listenerClass.newInstance();
		}catch(InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		listener.init();
		
		return listener;
	}
}
