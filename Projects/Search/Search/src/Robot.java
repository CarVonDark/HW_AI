import java.util.ArrayList;
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
		if (index < pathLength) {
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
		LinkedList<Position> queue = new LinkedList<Position>();
		boolean[][] hasVisited = new boolean[env.getRows()][env.getCols()];
		Action[][] moves = new Action[env.getRows()][env.getCols()];
		for (int i = 0; i < hasVisited.length; i++) {
			for (int j = 0; j < hasVisited[0].length; j++) {
				hasVisited[i][j] = false;
				moves[i][j] = Action.DO_NOTHING;
			}
		}
		Position root = new Position(posRow, posCol);
		queue.add(root);
		hasVisited[root.row][root.col] = true;
		LinkedList<Position> targets = env.getTargets();
		while (!targets.isEmpty()) {
			Boolean currentTargetFound = false;
			Position currentTarget = null;
			while (!queue.isEmpty()) {
				Position current = queue.poll();
				//System.out.println(current.row + " " + current.col);
				if (env.validPos(current.row, current.col + 1)) {
					Position next = new Position(current.row, current.col + 1);
					if (!hasVisited[next.row][next.col]) {
						hasVisited[next.row][next.col] = true;
						moves[current.row][current.col + 1] = Action.MOVE_RIGHT;
						if (targets.contains(next)) {
							currentTarget = new Position(next.row, next.col);
							currentTargetFound = true;
							break;
						}
						this.openCount++;
						queue.add(next);
					}
				}
				if (env.validPos(current.row, current.col - 1)) {
					Position next = new Position(current.row, current.col - 1);
					if (!hasVisited[next.row][next.col]) {
						hasVisited[next.row][next.col] = true;
						moves[current.row][current.col - 1] = Action.MOVE_LEFT;
						if (targets.contains(next)) {
							currentTarget = new Position(next.row, next.col);
							currentTargetFound = true;
							break;
						}
						this.openCount++;
						queue.add(next);
					}
				}
				if (env.validPos(current.row + 1, current.col)) {
					Position next = new Position(current.row + 1, current.col);
					if (!hasVisited[next.row][next.col]) {
						hasVisited[next.row][next.col] = true;
						moves[current.row + 1][current.col] = Action.MOVE_DOWN;
						if (targets.contains(next)) {
							currentTarget = new Position(next.row, next.col);
							currentTargetFound = true;
							break;
						}
						this.openCount++;
						queue.add(next);
					}
				}
				if (env.validPos(current.row - 1, current.col)) {
					Position next = new Position(current.row - 1, current.col);
					if (!hasVisited[next.row][next.col]) {
						hasVisited[next.row][next.col] = true;
						moves[current.row - 1][current.col] = Action.MOVE_UP;
						if (targets.contains(next)) {
							currentTarget = new Position(next.row, next.col);
							currentTargetFound = true;
							break;
						}
						this.openCount++;
						queue.add(next);
					}
				}
			}
			if (currentTargetFound) {
				this.pathFound = true;
				LinkedList<Action> thisTurn = new LinkedList<Action>();
				targets.remove(currentTarget);
				int row = currentTarget.row;
				int col = currentTarget.col;
				while(!moves[row][col].equals(Action.DO_NOTHING)) {
					thisTurn.addFirst(moves[row][col]);
					this.pathLength++;
					if(moves[row][col].equals(Action.MOVE_RIGHT))
						col--;
					else if(moves[row][col].equals(Action.MOVE_LEFT))
						col++;
					else if(moves[row][col].equals(Action.MOVE_UP))
						row++;
					else 
						row--;
				}
				for (int i = 0; i < hasVisited.length; i++) {
					for (int j = 0; j < hasVisited[0].length; j++) {
						hasVisited[i][j] = false;
						moves[i][j] = Action.DO_NOTHING;
					}
				}
				this.path.addAll(thisTurn);
				queue.clear();
				queue.add(currentTarget);
				hasVisited[currentTarget.row][currentTarget.col] = true;
			} else {
				this.pathFound = false;
				break;
			}
		}
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