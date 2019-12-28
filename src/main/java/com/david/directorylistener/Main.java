package com.david.directorylistener;

import java.io.IOException;

import com.david.directorylistener.listener.DirectoryListener;
import com.david.directorylistener.listener.ListenerFactory;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException{
		ListenerFactory.getListener(DirectoryListener.class).startDirectoryListener();
	}
}
