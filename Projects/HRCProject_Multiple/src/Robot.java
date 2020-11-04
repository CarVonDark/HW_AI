import java.util.HashMap;
import java.util.HashSet;
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
	private HashSet<Position> targets = new HashSet<Position>();
	private Stack<Action> steps;
	private boolean getRemoved = false;
	private int oldNumberRobot;

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
		this.oldNumberRobot = 4;
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
		if (oldNumberRobot != env.getRobots().size()) {
			steps.clear();
			bfs();
			oldNumberRobot = env.getRobots().size();
			if (steps.isEmpty()) {
				return Action.DO_NOTHING;
			}
		}
		if (steps.isEmpty()) {
			updateTargets();
			bfs();
			if (steps.isEmpty()) {
				return Action.DO_NOTHING;
			}
		}
		Action re = steps.peek();
		Position p = new Position(posRow, posCol);
		switch (re) {
		case MOVE_RIGHT:
			p.col++;
			break;
		case MOVE_LEFT:
			p.col--;
			break;
		case MOVE_DOWN:
			p.row++;
			break;
		case MOVE_UP:
			p.row--;
			break;
		default:
			break;
		}
		for(Robot r: env.getRobots()) {
			if(r!=this && p.row == r.posRow && p.col == r.posCol) {
				steps.clear();
				bfs();
				break;
			}
		}
		return steps.pop();

	}

	private void updateTargets() {
		targets.clear();
		for (Robot r : env.getRobots()) {
			if (r.headed != null)
				targets.add(r.headed);
		}
	}

	private void bfs() {
		Tile[][] tiles = env.getTiles();
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
		boolean firstStep = true;
		while (!queue.isEmpty()) {
			Position current = queue.poll();
			// System.out.println(current.row + " " + current.col);
			if (env.validPos(current.row, current.col + 1) && !(firstStep && !compareFirstStep(current.row, current.col + 1))) {
				Position next = new Position(current.row, current.col + 1);
				if (!hasVisited[next.row][next.col]) {
					hasVisited[next.row][next.col] = true;
					moves[current.row][current.col + 1] = Action.MOVE_RIGHT;
					if (tiles[next.row][next.col].getStatus() == TileStatus.DIRTY && !compareTargets(next)) {
						targetFound = true;
						headed = next;
						break;
					}
					queue.add(next);
				}
			}
			if (env.validPos(current.row, current.col - 1) && !(firstStep && !compareFirstStep(current.row, current.col - 1))) {
				Position next = new Position(current.row, current.col - 1);
				if (!hasVisited[next.row][next.col]) {
					hasVisited[next.row][next.col] = true;
					moves[current.row][current.col - 1] = Action.MOVE_LEFT;
					if (tiles[next.row][next.col].getStatus() == TileStatus.DIRTY && !compareTargets(next)) {
						targetFound = true;
						headed = next;
						break;
					}
					queue.add(next);
				}
			}
			if (env.validPos(current.row + 1, current.col) && !(firstStep && !compareFirstStep(current.row + 1, current.col))) {
				Position next = new Position(current.row + 1, current.col);
				if (!hasVisited[next.row][next.col]) {
					hasVisited[next.row][next.col] = true;
					moves[current.row + 1][current.col] = Action.MOVE_DOWN;
					if (tiles[next.row][next.col].getStatus() == TileStatus.DIRTY && !compareTargets(next)) {
						targetFound = true;
						headed = next;
						break;
					}
					queue.add(next);
				}
			}
			if (env.validPos(current.row - 1, current.col) && !(firstStep && !compareFirstStep(current.row - 1, current.col))) {
				Position next = new Position(current.row - 1, current.col);
				if (!hasVisited[next.row][next.col]) {
					hasVisited[next.row][next.col] = true;
					moves[current.row - 1][current.col] = Action.MOVE_UP;
					if (tiles[next.row][next.col].getStatus() == TileStatus.DIRTY && !compareTargets(next)) {
						targetFound = true;
						headed = next;
						break;
					}
					queue.add(next);
				}
			}
			if(firstStep) {
				firstStep = false;
			}
		}
		if (targetFound) {
			int row = headed.row;
			int col = headed.col;
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

	private boolean compareFirstStep(int row, int col) {
		for(Robot r : env.getRobots()) {
			if(r != this && r.posRow == row && r.posCol == col) {
				return false;
			}
		}
		return true;	
	}

	public boolean compareTargets(Position target) {
		if (targets.isEmpty()) {
			return false;
		}
		for (Position p : targets) {
			if (p.row == target.row && p.col == target.col) {
				return true;
			}
		}
		
		return false;
	}
}