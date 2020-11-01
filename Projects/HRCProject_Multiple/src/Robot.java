import java.util.LinkedList;
import java.util.Stack;

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
	private boolean toCleanOrNotToClean;
	private Position headed;
	private Stack<Action> steps;

	/**
	 * Initializes a Robot on a specific tile in the environment.
	 */

	public Robot(Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.headed = null;
		this.toCleanOrNotToClean = false;
		this.steps = new Stack<Action>();
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
//		if (toCleanOrNotToClean) {
//			toCleanOrNotToClean = false;
//			return Action.CLEAN;
//		}
//		toCleanOrNotToClean = true;
		if (steps.isEmpty()) {
			int closestDistance = Integer.MAX_VALUE;
			Tile[][] tiles = env.getTiles();
			for (int i = 0; i < tiles.length; i++) {
				for (int j = 0; j < tiles[0].length; j++) {
					if (env.getTileStatus(i, j) == TileStatus.DIRTY) {
						int distance = Math.abs(i - this.posRow) + Math.abs(j - this.posCol);
						if (distance < closestDistance) {
							headed = new Position(i, j);
							closestDistance = distance;
						}
					}
				}
			}
			bfs();
			if (steps.isEmpty()) {
				return Action.DO_NOTHING;
			}
			return steps.pop();
		} else {
			return steps.pop();
		}
	}

	private void bfs() {
		int row = this.headed.row;
		int col = this.headed.col;
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
		boolean targetFound = false;
		queue.add(root);
		hasVisited[root.row][root.col] = true;
		while (!queue.isEmpty()) {
			Position current = queue.poll();
			// System.out.println(current.row + " " + current.col);
			if (env.validPos(current.row, current.col + 1)) {
				Position next = new Position(current.row, current.col + 1);
				if (!hasVisited[next.row][next.col]) {
					hasVisited[next.row][next.col] = true;
					moves[current.row][current.col + 1] = Action.MOVE_RIGHT;
					if (next.row == row && next.col == col) {
						targetFound = true;
						break;
					}
					queue.add(next);
				}
			}
			if (env.validPos(current.row, current.col - 1)) {
				Position next = new Position(current.row, current.col - 1);
				if (!hasVisited[next.row][next.col]) {
					hasVisited[next.row][next.col] = true;
					moves[current.row][current.col - 1] = Action.MOVE_LEFT;
					if (next.row == row && next.col == col) {
						targetFound = true;
						break;
					}
					queue.add(next);
				}
			}
			if (env.validPos(current.row + 1, current.col)) {
				Position next = new Position(current.row + 1, current.col);
				if (!hasVisited[next.row][next.col]) {
					hasVisited[next.row][next.col] = true;
					moves[current.row + 1][current.col] = Action.MOVE_DOWN;
					if (next.row == row && next.col == col) {
						targetFound = true;
						break;
					}
					queue.add(next);
				}
			}
			if (env.validPos(current.row - 1, current.col)) {
				Position next = new Position(current.row - 1, current.col);
				if (!hasVisited[next.row][next.col]) {
					hasVisited[next.row][next.col] = true;
					moves[current.row - 1][current.col] = Action.MOVE_UP;
					if (next.row == row && next.col == col) {
						targetFound = true;
						break;
					}
					queue.add(next);
				}
			}
		}
		if (targetFound) {
			LinkedList<Action> thisTurn = new LinkedList<Action>();
			while (!moves[row][col].equals(Action.DO_NOTHING)) {
				thisTurn.addFirst(moves[row][col]);
				if (moves[row][col].equals(Action.MOVE_RIGHT))
					col--;
				else if (moves[row][col].equals(Action.MOVE_LEFT))
					col++;
				else if (moves[row][col].equals(Action.MOVE_UP))
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
			queue.clear();
			steps.add(Action.CLEAN);
			steps.addAll(thisTurn);
		} else {
			// Right now; Not possible
		}

	}

}