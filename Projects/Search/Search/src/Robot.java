import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

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
				// System.out.println(current.row + " " + current.col);
				if (env.validPos(current.row, current.col + 1)) {
					Position next = new Position(current.row, current.col + 1);
					if (!hasVisited[next.row][next.col]) {
						hasVisited[next.row][next.col] = true;
						moves[current.row][current.col + 1] = Action.MOVE_RIGHT;
						if (targetFind(next, targets)) {
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
						if (targetFind(next, targets)) {
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
						if (targetFind(next, targets)) {
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
						if (targetFind(next, targets)) {
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
				removeTargets(currentTarget, targets);
				int row = currentTarget.row;
				int col = currentTarget.col;
				while (!moves[row][col].equals(Action.DO_NOTHING)) {
					thisTurn.addFirst(moves[row][col]);
					this.pathLength++;
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
	/*
	 * It looks like in this assignment we do not need to do greedy
	 */
	public void greedy() {

	}

	public int getDistance(Position current, Position target) {
		return Math.abs(current.row - target.row) + Math.abs(current.col - target.col);
	}

	/**
	 * This method implements A* search. It populates the LinkedList of the field
	 * this.path and sets this.pathFound to true, if a path has been found.
	 * IMPORTANT: This method increases the this.openCount field every time the
	 * algorithm adds a node to the open data structure, i.e. its priorityQueue
	 */
	/*
	 * For astar 0-5 I assume there is only one target
	 */
	public void astar() {
		LinkedList<Position> targets = env.getTargets();
		Position target = targets.poll();
		PriorityQueue<PositionContainer> queue = new PriorityQueue<PositionContainer>(
				new Comparator<PositionContainer>() {
					@Override
					public int compare(PositionContainer p0, PositionContainer p1) {
						return (p0.distance - p1.distance) + getDistance(p0.p, target) - getDistance(p1.p, target);
					}
				});
		boolean[][] hasVisited = new boolean[env.getRows()][env.getCols()];
		Action[][] moves = new Action[env.getRows()][env.getCols()];
		for (int i = 0; i < hasVisited.length; i++) {
			for (int j = 0; j < hasVisited[0].length; j++) {
				hasVisited[i][j] = false;
				moves[i][j] = Action.DO_NOTHING;
			}
		}
		PositionContainer root = new PositionContainer(new Position(posRow, posCol), 0);
		queue.add(root);
		hasVisited[root.p.row][root.p.col] = true;
		while (!queue.isEmpty()) {
			PositionContainer currentContainer = queue.poll();
			Position current = currentContainer.p;
			int distance = currentContainer.distance;
			// System.out.println(current.row + " " + current.col);
			if (comparePosition(current, target)) {
				this.pathFound = true;
				break;
			}
			if (env.validPos(current.row, current.col + 1)) {
				Position next = new Position(current.row, current.col + 1);
				if (!hasVisited[next.row][next.col]) {
					hasVisited[next.row][next.col] = true;
					moves[current.row][current.col + 1] = Action.MOVE_RIGHT;
					this.openCount++;
					queue.add(new PositionContainer(next, distance + 1));
				}
			}
			if (env.validPos(current.row, current.col - 1)) {
				Position next = new Position(current.row, current.col - 1);
				if (!hasVisited[next.row][next.col]) {
					hasVisited[next.row][next.col] = true;
					moves[current.row][current.col - 1] = Action.MOVE_LEFT;
					this.openCount++;
					queue.add(new PositionContainer(next, distance + 1));
				}
			}
			if (env.validPos(current.row + 1, current.col)) {
				Position next = new Position(current.row + 1, current.col);
				if (!hasVisited[next.row][next.col]) {
					hasVisited[next.row][next.col] = true;
					moves[current.row + 1][current.col] = Action.MOVE_DOWN;
					this.openCount++;
					queue.add(new PositionContainer(next, distance + 1));
				}
			}
			if (env.validPos(current.row - 1, current.col)) {
				Position next = new Position(current.row - 1, current.col);
				if (!hasVisited[next.row][next.col]) {
					hasVisited[next.row][next.col] = true;
					moves[current.row - 1][current.col] = Action.MOVE_UP;
					this.openCount++;
					queue.add(new PositionContainer(next, distance + 1));
				}
			}

		}
		if (this.pathFound) {
			int row = target.row;
			int col = target.col;
			while (!moves[row][col].equals(Action.DO_NOTHING)) {
				this.path.addFirst(moves[row][col]);
				this.pathLength++;
				if (moves[row][col].equals(Action.MOVE_RIGHT))
					col--;
				else if (moves[row][col].equals(Action.MOVE_LEFT))
					col++;
				else if (moves[row][col].equals(Action.MOVE_UP))
					row++;
				else
					row--;
			}
		}
	}

	public void astar101112() {
		LinkedList<Position> targets = env.getTargets();
		PriorityQueue<PositionContainer> queue = new PriorityQueue<PositionContainer>(
				new Comparator<PositionContainer>() {
					@Override
					public int compare(PositionContainer p0, PositionContainer p1) {
						int min = Integer.MAX_VALUE;
						int re = -1;
						for (Position target : targets) {
							int p0Distance = p0.distance + getDistance(p0.p, target);
							int p1Distance = p1.distance + getDistance(p1.p, target);
							if (p0Distance < min) {
								min = p0Distance;
								re = -1;
							}
							if (p1Distance < min) {
								min = p1Distance;
								re = 1;
							}
						}
						return re;
					}
				});
		boolean[][] hasVisited = new boolean[env.getRows()][env.getCols()];
		Action[][] moves = new Action[env.getRows()][env.getCols()];
		for (int i = 0; i < hasVisited.length; i++) {
			for (int j = 0; j < hasVisited[0].length; j++) {
				hasVisited[i][j] = false;
				moves[i][j] = Action.DO_NOTHING;
			}
		}
		PositionContainer root = new PositionContainer(new Position(posRow, posCol), 0);
		queue.add(root);
		hasVisited[root.p.row][root.p.col] = true;
		while (!targets.isEmpty()) {
			Position currentTarget = null;
			while (!queue.isEmpty()) {
				PositionContainer currentContainer = queue.poll();
				Position current = currentContainer.p;
				int distance = currentContainer.distance;
				System.out.println(current.row + " " + current.col);
				if (targetFind(current, targets)) {
					removeTargets(current, targets);
					currentTarget = current;
					this.pathFound = true;
					break;
				}
				if (env.validPos(current.row + 1, current.col)) {
					Position next = new Position(current.row + 1, current.col);
					if (!hasVisited[next.row][next.col]) {
						hasVisited[next.row][next.col] = true;
						moves[current.row + 1][current.col] = Action.MOVE_DOWN;
						this.openCount++;
						queue.add(new PositionContainer(next, distance + 1));
					}
				}
				if (env.validPos(current.row, current.col + 1)) {
					Position next = new Position(current.row, current.col + 1);
					if (!hasVisited[next.row][next.col]) {
						hasVisited[next.row][next.col] = true;
						moves[current.row][current.col + 1] = Action.MOVE_RIGHT;
						this.openCount++;
						queue.add(new PositionContainer(next, distance + 1));
					}
				}
				if (env.validPos(current.row, current.col - 1)) {
					Position next = new Position(current.row, current.col - 1);
					if (!hasVisited[next.row][next.col]) {
						hasVisited[next.row][next.col] = true;
						moves[current.row][current.col - 1] = Action.MOVE_LEFT;
						this.openCount++;
						queue.add(new PositionContainer(next, distance + 1));
					}
				}
				
				if (env.validPos(current.row - 1, current.col)) {
					Position next = new Position(current.row - 1, current.col);
					if (!hasVisited[next.row][next.col]) {
						hasVisited[next.row][next.col] = true;
						moves[current.row - 1][current.col] = Action.MOVE_UP;
						this.openCount++;
						queue.add(new PositionContainer(next, distance + 1));
					}
				}

			}

			if (this.pathFound && currentTarget != null) {
				this.pathFound = true;
				LinkedList<Action> thisTurn = new LinkedList<Action>();
				int row = currentTarget.row;
				int col = currentTarget.col;
				while (!moves[row][col].equals(Action.DO_NOTHING)) {
					thisTurn.addFirst(moves[row][col]);
					this.pathLength++;
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
				this.path.addAll(thisTurn);
				queue.clear();
				queue.add(new PositionContainer(currentTarget, 0));
				hasVisited[currentTarget.row][currentTarget.col] = true;
			} else {
				this.pathFound = false;
				break;
			}
		}
	}

	public void astar141516() {
		LinkedList<Position> targets = env.getTargets();
		PriorityQueue<PositionContainer> queue = new PriorityQueue<PositionContainer>(
				new Comparator<PositionContainer>() {
					@Override
					public int compare(PositionContainer p0, PositionContainer p1) {
						int min = Integer.MAX_VALUE;
						int re = -1;
						for (Position target : targets) {
							int p0Distance = p0.distance + getDistance(p0.p, target);
							int p1Distance = p1.distance + getDistance(p1.p, target);
							if (p0Distance < min) {
								min = p0Distance;
								re = -1;
							}
							if (p1Distance < min) {
								min = p1Distance;
								re = 1;
							}
						}
						return re;
					}
				});
		boolean[][] hasVisited = new boolean[env.getRows()][env.getCols()];
		Action[][] moves = new Action[env.getRows()][env.getCols()];
		for (int i = 0; i < hasVisited.length; i++) {
			for (int j = 0; j < hasVisited[0].length; j++) {
				hasVisited[i][j] = false;
				moves[i][j] = Action.DO_NOTHING;
			}
		}
		PositionContainer root = new PositionContainer(new Position(posRow, posCol), 0);
		queue.add(root);
		hasVisited[root.p.row][root.p.col] = true;
		while (!targets.isEmpty()) {
			Position currentTarget = null;
			while (!queue.isEmpty()) {
				PositionContainer currentContainer = queue.poll();
				Position current = currentContainer.p;
				int distance = currentContainer.distance;
				System.out.println(current.row + " " + current.col);
				if (targetFind(current, targets)) {
					removeTargets(current, targets);
					currentTarget = current;
					this.pathFound = true;
					break;
				}
				if (env.validPos(current.row + 1, current.col)) {
					Position next = new Position(current.row + 1, current.col);
					if (!hasVisited[next.row][next.col]) {
						hasVisited[next.row][next.col] = true;
						moves[current.row + 1][current.col] = Action.MOVE_DOWN;
						this.openCount++;
						queue.add(new PositionContainer(next, distance + 1));
					}
				}
				if (env.validPos(current.row, current.col + 1)) {
					Position next = new Position(current.row, current.col + 1);
					if (!hasVisited[next.row][next.col]) {
						hasVisited[next.row][next.col] = true;
						moves[current.row][current.col + 1] = Action.MOVE_RIGHT;
						this.openCount++;
						queue.add(new PositionContainer(next, distance + 1));
					}
				}
				if (env.validPos(current.row, current.col - 1)) {
					Position next = new Position(current.row, current.col - 1);
					if (!hasVisited[next.row][next.col]) {
						hasVisited[next.row][next.col] = true;
						moves[current.row][current.col - 1] = Action.MOVE_LEFT;
						this.openCount++;
						queue.add(new PositionContainer(next, distance + 1));
					}
				}
				
				if (env.validPos(current.row - 1, current.col)) {
					Position next = new Position(current.row - 1, current.col);
					if (!hasVisited[next.row][next.col]) {
						hasVisited[next.row][next.col] = true;
						moves[current.row - 1][current.col] = Action.MOVE_UP;
						this.openCount++;
						queue.add(new PositionContainer(next, distance + 1));
					}
				}

			}

			if (this.pathFound && currentTarget != null) {
				this.pathFound = true;
				LinkedList<Action> thisTurn = new LinkedList<Action>();
				int row = currentTarget.row;
				int col = currentTarget.col;
				while (!moves[row][col].equals(Action.DO_NOTHING)) {
					thisTurn.addFirst(moves[row][col]);
					this.pathLength++;
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
				this.path.addAll(thisTurn);
				queue.clear();
				queue.add(new PositionContainer(currentTarget, 0));
				hasVisited[currentTarget.row][currentTarget.col] = true;
			} else {
				this.pathFound = false;
				break;
			}
		}
	}

	public boolean comparePosition(Position p1, Position p2) {
		return (p1.row == p2.row) && (p1.col == p2.col); 
	}
	
	public boolean targetFind(Position current, LinkedList<Position> targets) {
		for(Position p: targets) {
			if(comparePosition(current, p))
				return true;
		}
		return false;
	}
	
	public void removeTargets(Position current, LinkedList<Position> targets) {
		Position toRemove = null;
		for(Position p: targets) {
			if(comparePosition(current, p)) {
				toRemove = p;
			}
		}
		targets.remove(toRemove);
	}
}

class PositionContainer {
	public int distance;
	public Position p;

	public PositionContainer(Position p, int distance) {
		this.p = p;
		this.distance = distance;
	}
}
