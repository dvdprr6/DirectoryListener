package com.david.directorylistener.listener;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import com.david.directorylistener.utils.Constants;
import com.david.directorylistener.utils.SystemTime;

/**
 * REFERENCE: https://stackoverflow.com/questions/30284302/watch-directory-using-scheduled-executor-instead-of-hard-loop-in-java
 * REFERENCE: https://stackoverflow.com/questions/3369383/java-watching-a-directory-to-move-large-files
 * @author david
 *
 */
public class DirectoryListener implements IListener{
	private final Map<Path, Long> expirationTimes = new HashMap<Path, Long>();
	
	@SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }
	
	public void startDirectoryListener() throws IOException, InterruptedException{
		WatchService watchService = FileSystems.getDefault().newWatchService();
		Path path = Paths.get(Constants.DIRECTORY_TO_LISTENER);
		path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
		
		while(true) {
			WatchKey key = watchService.take();
			
			while(true) {
				long currentTime = SystemTime.getCurrentTime();
				
				if(key != null) {
					for(WatchEvent<?> event : key.pollEvents()) {
						WatchEvent<Path> ev = cast(event);
						if(ev.kind() == StandardWatchEventKinds.ENTRY_CREATE || ev.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
							storeLastModifiedTime(ev);
						}
					}
					key.reset();
					
				}
				
				handlExpiredWaitTimes(currentTime);
				
				if(expirationTimes.isEmpty()) break;
				
				long timeout = Collections.min(expirationTimes.values()) - currentTime;
				key = watchService.poll(timeout, TimeUnit.MILLISECONDS);
			}
		}
	}

	@Override
	public void init() {
		// TODO INITIALIZE SOMETHING
		
	}
	
	private void storeLastModifiedTime(WatchEvent<Path> event) {
		try {
			Path file = Paths.get(Constants.DIRECTORY_TO_LISTENER + "/" + event.context());
			FileTime lastModified = Files.getLastModifiedTime(file, LinkOption.NOFOLLOW_LINKS);
			long waitTime = Long.valueOf(Constants.FILE_READY_WAIT_TIME) * 1000;
			expirationTimes.put(event.context(), lastModified.toMillis() + waitTime);
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void handlExpiredWaitTimes(long currentTime) {
		Set<Entry<Path, Long>> copyExpirationTimes = new CopyOnWriteArraySet<Entry<Path, Long>>(expirationTimes.entrySet()); // This will prevent the Concurrent Modification Exception during an enhanced for-loop
		
		for(Entry<Path, Long> entry : copyExpirationTimes) {
			if(entry.getValue() <= currentTime) {
				String fileName = entry.getKey().toString();
				
				// TODO DO SOMETHING WITH THE FILE
			}
			
			copyExpirationTimes.remove(entry);
			expirationTimes.remove(entry.getKey());
		}
	}
}
