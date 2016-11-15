package peersim.chord.messaging;

public class FinalMessage implements ChordMessage {

	private int hopCounter = 0;
	private String path;
	
	public FinalMessage(int hopCounter, String path) {
		this.hopCounter = hopCounter;
		this.path = path;
	}

	public int getHopCounter() {
		return hopCounter;
	}
	
	public String getPath(){
		return path;
	}
}
