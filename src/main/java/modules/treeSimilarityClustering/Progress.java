package modules.treeSimilarityClustering;

/**
 * Can be used to track the progress of a module processing data composed of individual elements.
 * @author Marcel Boeing
 *
 */
public class Progress {
	private long queued;
	private long processed;
	public Progress(long queued) {
		this(queued, 0l);
	}
	public Progress(long queued, long processed) {
		super();
		this.queued = queued;
		this.processed = processed;
	}
	public synchronized long getQueued(){
		return queued;
	}
	public synchronized long getProcessed(){
		return processed;
	}
	public synchronized void setQueued(long queued){
		this.queued = queued;
	}
	public synchronized void setProcessed(long processed){
		this.processed = processed;
	}
	public synchronized void countOne(){
		this.processed+=1;
		this.queued-=1;
	}
}
