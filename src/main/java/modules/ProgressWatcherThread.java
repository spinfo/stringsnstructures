package modules;

import java.time.Instant;

/**
 * Use to have a module's progress be monitored by a separate thread.
 * @author Marcel Boeing
 *
 */
public class ProgressWatcherThread extends Thread {
	
	private Instant lastInstant;
	private long lastProcessedAmount = 0l;

	private ProgressWatcher progressWatcher;
	private Module module;
	private long intervalMs;
	
	/**
	 * Returns a watcher thread instance that will check the module's progress every 30 seconds.
	 * @param progressWatcher ProgressWatcher instance
	 * @param module Module instance
	 */
	public ProgressWatcherThread(ProgressWatcher progressWatcher, Module module) {
		this(progressWatcher,module,30000l);
	}
	
	/**
	 * Returns a watcher thread instance that will check the module's progress periodically.
	 * @param progressWatcher ProgressWatcher instance
	 * @param module Module instance
	 * @param intervalMs Interval in which to check the module's progress
	 */
	public ProgressWatcherThread(ProgressWatcher progressWatcher, Module module, long intervalMs) {
		super();
		this.progressWatcher = progressWatcher;
		this.module = module;
		this.intervalMs = intervalMs;
	}

	@Override
	public void run() {
		try {
			while (progressWatcher.getQueued()>0){
				
				// Hibernate
				Thread.sleep(intervalMs);
				
				// Get current time
				Instant now = Instant.now();
				
				// Check if past data is available
				if (lastInstant == null){
					// Initialise and continue loop
					lastInstant = now;
					lastProcessedAmount = progressWatcher.getProcessed();
					continue;
				} else {
					// Calculate progress and set module status
					long queued = progressWatcher.getQueued();
					long processed = progressWatcher.getProcessed();
					long processedDelta = processed - lastProcessedAmount;
					long millisecondDelta = now.toEpochMilli() - lastInstant.toEpochMilli();
					long perMinute = processedDelta * (60000 / millisecondDelta);
					if (perMinute >0){
						long minsRemaining = queued / perMinute;
						module.setStatusDetail("Elements in queue: " + queued + " @ " + perMinute
							+ " per minute (" + minsRemaining + " minutes remaining)");
					} else {
						module.setStatusDetail("Elements in queue: " + queued + " @ " + perMinute
								+ " per minute (∞)");
					}
					lastInstant = now;
					lastProcessedAmount = processed;
				}
			}
			// Remove module status
			module.setStatusDetail(null);
			
		} catch (InterruptedException e) {
			e.printStackTrace();
			module.setStatusDetail(null);
		}
	}
	
}
