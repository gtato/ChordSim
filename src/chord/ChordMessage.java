/**
 * 
 */
package chord;

import java.math.BigInteger;
import java.util.ArrayList;




public class ChordMessage {
	
	public static final int LOOK_UP=0;
	public static final int FINAL=1;
	public static final int SUCCESSOR=2;
	public static final int SUCCESSOR_FOUND=3;
	public static final int NOTIFY=4;
	
	private String label;
	private int type;
	private BigInteger sender;
	private Object content;
	private ArrayList<String> path = new ArrayList<String>();
	
	public ChordMessage(int type, Object content) {
		this.type = type;
		this.content = content;
	}

	
	public int getType(){
		return this.type;
	}
	
	public BigInteger getSender() {
		return sender;
	}
	
	public void setSender(BigInteger sender){
		this.sender = sender;
		addToPath(sender);
	}
	
	public Object getContent() {
		return content;
	}

	
	public void addToPath(BigInteger chordId){
		path.add(chordId.toString());
	}
	public ArrayList<String> getPath(){
		return path;
	}


	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}
	
	public boolean isType(int type){
		return this.type == type;
	}
	

}
