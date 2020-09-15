import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Represents an intelligent agent moving through a particular room. The robot
 * only has one sensor - the ability to get the status of any tile in the
 * environment through the command env.getTileStatus(row, col).
 * 
 * @author Adam Gaweda, Michael Wollowski
 */

public class Robot {
	private Environment env;
	private int posRow;
	private int posCol;
	private LinkedList<Action> path;
	private boolean pathFound;
	private long openCount;
	private int pathLength;
	private int index;

	/**
	 * Initializes a Robot on a specific tile in the environment.
	 */
//	public Robot (Environment env) { this(env, 0, 0); }

	public Robot(Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.path = new LinkedList<>();
		this.index = 0;
		this.pathFound = false;
		this.openCount = 0;
		this.pathLength = 0;
	}

	public boolean getPathFound() {
		return this.pathFound;
	}

	public long getOpenCount() {
		return this.openCount;
	}

	public int getPathLength() {
		return this.pathLength;
	}

	public void resetOpenCount() {
		this.openCount = 0;
	}

	public int getPosRow() {
		return posRow;
	}

	public int getPosCol() {
		return posCol;
	}

	public void incPosRow() {
		posRow++;
	}

	public void decPosRow() {
		posRow--;
	}

	public void incPosCol() {
		posCol++;
	}

	public void decPosCol() {
		posCol--;
	}

	/**
	 * Returns the next action to be taken by the robot. A support function that
	 * processes the path LinkedList that has been populates by the search
	 * functions.
	 */
	public Action getAction() {
		if(index < pathLength) {
			return path.get(index++);
		} else {
			return Action.DO_NOTHING;
		}
	}

	/**
	 * This method implements breadth-first search. It populates the LinkedList of
	 * the field this.path and sets this.pathFound to true, if a path has been
	 * found. IMPORTANT: This method increases the this.openCount field every time
	 * the algorithm adds a node to the open data structure, i.e. its queue.
	 */
	public void bfs() {
		Queue<Position> queue = new LinkedList<Position>();
		LinkedList<Position> hasVisited = new LinkedList<Position>();
		Position current = new Position(posRow, posCol);
		LinkedList<Position> targets = env.getTargets();
		queue.add(current);
		if (bfsHelper(queue, hasVisited, targets)) {
			this.pathFound = true;
		} else {
			this.pathFound = false;
		}
	}

	public boolean bfsHelper(Queue<Position> queue, LinkedList<Position> hasVisited, LinkedList<Position> targets) {
		if (queue.isEmpty()) {
			return false;
		}
		Position current = queue.remove();
		if (targets.contains(current)) {
			targets.remove(current);
		}
		if (targets.isEmpty()) {
			return true;
		}
		if (env.validPos(current.row, current.col + 1)) {
			hasVisited.add(current);
			Position next = new Position(current.row, current.col + 1);
			if (!hasVisited.contains(next)) {
				this.openCount++;
				this.pathLength++;
				this.path.add(Action.MOVE_RIGHT);
				queue.add(next);
			}
			return bfsHelper(queue, hasVisited, targets);
		}
		if (env.validPos(current.row + 1, current.col)) {
			hasVisited.add(current);
			Position next = new Position(current.row + 1, current.col);
			if (!hasVisited.contains(next)) {
				this.openCount++;
				this.pathLength++;
				this.path.add(Action.MOVE_DOWN);
				queue.add(next);
			}
			return bfsHelper(queue, hasVisited, targets);
		}
		if (env.validPos(current.row, current.col - 1)) {
			hasVisited.add(current);
			Position next = new Position(current.row, current.col - 1);
			if (!hasVisited.contains(next)) {
				this.openCount++;
				this.pathLength++;
				this.path.add(Action.MOVE_LEFT);
				queue.add(next);
			}
			return bfsHelper(queue, hasVisited, targets);
		}
		if (env.validPos(current.row - 1, current.col)) {
			hasVisited.add(current);
			Position next = new Position(current.row - 1, current.col);
			if (!hasVisited.contains(next)) {
				this.openCount++;
				this.pathLength++;
				this.path.add(Action.MOVE_UP);
				queue.add(next);
			}
			return bfsHelper(queue, hasVisited, targets);
		}
		return false;
	}

	/**
	 * This method implements greedy search. It populates the LinkedList of the
	 * field this.path and sets this.pathFound to true, if a path has been found.
	 * IMPORTANT: This method increases the this.openCount field every time the
	 * algorithm adds a node to the open data structure, i.e. its field that holds
	 * the node to be explored next.
	 */
	public void greedy() {

	}

	/**
	 * This method implements A* search. It populates the LinkedList of the field
	 * this.path and sets this.pathFound to true, if a path has been found.
	 * IMPORTANT: This method increases the this.openCount field every time the
	 * algorithm adds a node to the open data structure, i.e. its priorityQueue
	 */
	public void astar() {
	}

}