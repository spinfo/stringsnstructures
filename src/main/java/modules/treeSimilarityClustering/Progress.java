package modules.treeSimilarityClustering;

public class Progress {
	private long verbleibend;
	private long verarbeitet;
	public Progress(long verbleibend) {
		this(verbleibend, 0l);
	}
	public Progress(long verbleibend, long verarbeitet) {
		super();
		this.verbleibend = verbleibend;
		this.verarbeitet = verarbeitet;
	}
	public synchronized long getVerbleibend(){
		return verbleibend;
	}
	public synchronized long getVerarbeitet(){
		return verarbeitet;
	}
	public synchronized void setVerbleibend(long verbleibend){
		this.verbleibend = verbleibend;
	}
	public synchronized void setVerarbeitet(long verarbeitet){
		this.verarbeitet = verarbeitet;
	}
}
