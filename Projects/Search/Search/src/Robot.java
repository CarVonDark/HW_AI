import java.util.LinkedList;

/**
	Represents an intelligent agent moving through a particular room.	
	The robot only has one sensor - the ability to get the status of any  
	tile in the environment through the command env.getTileStatus(row, col).
	@author Adam Gaweda, Michael Wollowski
*/

public class Robot {
	private Environment env;
	private int posRow;
	private int posCol;
	private LinkedList<Action> path;
	private boolean pathFound;
	private long openCount;
	private int pathLength;
	
	/**
	    Initializes a Robot on a specific tile in the environment. 
	*/
//	public Robot (Environment env) { this(env, 0, 0); }
	
	public Robot (Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.path = new LinkedList<>();
		this.pathFound = false;
		this.openCount = 0;
		this.pathLength = 0;
	}
	
	public boolean getPathFound(){
		return this.pathFound;
	}
	
	public long getOpenCount(){
		return this.openCount;
	}
	
	public int getPathLength(){
		return this.pathLength;
	}
	
	public void resetOpenCount() {
		this.openCount = 0;
	}
	
	public int getPosRow() { return posRow; }
	public int getPosCol() { return posCol; }
	public void incPosRow() { posRow++; }
	public void decPosRow() { posRow--; }
	public void incPosCol() { posCol++; }
    public void decPosCol() { posCol--; }
	
	/**
	   Returns the next action to be taken by the robot. A support function 
	   that processes the path LinkedList that has been populates by the
	   search functions.
	*/
	public Action getAction () {
	    return Action.DO_NOTHING;
	}
	
	/** 
	 * This method implements breadth-first search. It populates the LinkedList 
	 * of the field this.path and sets this.pathFound to true, if a path has been 
	 * found. IMPORTANT: This method increases the this.openCount field every time
	 * the algorithm adds a node to the open data structure, i.e. its queue.
	 */
	public void bfs() {
	}
	
	/** 
	 * This method implements greedy search. It populates the LinkedList 
	 * of the field this.path and sets this.pathFound to true, if a path has been 
	 * found. IMPORTANT: This method increases the this.openCount field every time
	 * the algorithm adds a node to the open data structure, i.e. its field that holds
	 * the node to be exlored next.
	 */
	public void greedy() {
	}

	/** 
	 * This method implements A* search. It populates the LinkedList 
	 * of the field this.path and sets this.pathFound to true, if a path has been 
	 * found. IMPORTANT: This method increases the this.openCount field every time
	 * the algorithm adds a node to the open data structure, i.e. its priorityQueue
	 */
	public void astar() {
	}
	

}