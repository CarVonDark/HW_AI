import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class Sudoku {

	private static int boardSize = 0;
	private static int partitionSize = 0;

	public static void main(String[] args) {
		String filename = args[0];
		File inputFile = new File(filename);
		Scanner input = null;
		int[][] vals = null;
		ArrayList<Variable> vars = new ArrayList<Variable>();

		int temp = 0;
		int count = 0;

		try {
			input = new Scanner(inputFile);
			temp = input.nextInt();
			boardSize = temp;
			partitionSize = (int) Math.sqrt(boardSize);
			System.out.println("Boardsize: " + temp + "x" + temp);
			vals = new int[boardSize][boardSize];

			System.out.println("Input:");
			int i = 0;
			int j = 0;
			while (input.hasNext()) {
				temp = input.nextInt();
				count++;
				System.out.printf("%3d", temp);
				vals[i][j] = temp;
				if (temp == 0) {
					vars.add(new Variable(i, j, boxCalc(i, j)));
				}
				j++;
				if (j == boardSize) {
					j = 0;
					i++;
					System.out.println();
				}
				if (j == boardSize) {
					break;
				}
			}
			input.close();
		} catch (FileNotFoundException exception) {
			System.out.println("Input file not found: " + filename);
		}
		if (count != boardSize * boardSize)
			throw new RuntimeException("Incorrect number of inputs.");

		initiateDomains(vars, vals);
		solve(vars, vals);

		// Output
		if (!done(vars)) {
			System.out.println("No solution found.");
			return;
		}
		System.out.println("\nOutput\n");
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				System.out.printf("%3d", vals[i][j]);
			}
			System.out.println();
		}

	}

	public static void solve(ArrayList<Variable> vars, int[][] vals) {
		int index = 0;
		while(index < vars.size()) {
			Variable current = vars.get(index);
			current.value++;
			while(current.value <= boardSize) {
				if(!current.domain.contains(current.value)) {
					current.value++;
					continue;
				}
				if(checkSingle(current, vals)) {
					forwardChecking(current, vars);
					vals[current.row][current.column] = current.value;
					break;
				} else {
					current.value++;
				}
			}
			if(current.value > boardSize) {
				if(index >= 1) {
					current.value = 0;
					Variable prev = vars.get(index-1);
					forwardUnChecking(prev, vars);
					vals[prev.row][prev.column] = 0;
					index--;
					continue;
				} else {
					return;
				}
			}
			index++;
		}
	}
	

	public static boolean checkSingle(Variable current, int[][] vals) {
		int rowBox = (int) (current.row / partitionSize) * partitionSize;
		int columnBox = (int) (current.column / partitionSize) * partitionSize;
		for (int i = 0; i < boardSize; i++)
			if (vals[current.row][i] == current.value || vals[i][current.column] == current.value)
				return false;
		for (int i = 0; i < partitionSize; i++)
			for (int j = 0; j < partitionSize; j++)
				if (vals[rowBox + i][columnBox + j] == current.value)
					return false;
		return true;
	}
	
	public static boolean done(ArrayList<Variable> vars) {
		for(Variable v: vars)
			if(v.value == 0 || v.value > 9)
				return false;
		return true;
	}
	
	public static int boxCalc(int row, int col) {
		if(row < 3 && col < 3)
			return 0;
		else if(row < 3 && col <6)
			return 1;
		else if(row < 3 && col <9)
			return 2;
		else if(row < 6 && col <3)
			return 3;
		else if(row < 6 && col <6)
			return 4;
		else if(row < 6 && col <9)
			return 5;
		else if(row < 9 && col <3)
			return 6;
		else if(row < 9 && col <6)
			return 7;
		else 
			return 8;
	}
	
	public static void forwardChecking(Variable var, ArrayList<Variable> vars) {
		for(Variable v: vars)
			if(v.box == var.box || v.column == var.column || v.row == var.row)
				if(v.domain.contains(var.value))
					v.domain.remove(var.value);
	}
	
	public static void forwardUnChecking(Variable var, ArrayList<Variable> vars) {
		for(Variable v: vars)
			if(v.box == var.box || v.column == var.column || v.row == var.row)
					v.domain.add(var.value);
		var.domain.remove(var.value);
	}
	
	public static void initiateDomains(ArrayList<Variable> vars, int[][] vals) {
		for(Variable v: vars) {
			HashSet<Integer> initialDomain = new HashSet<Integer>();
			for(int i = 1; i <= boardSize; i++) {
				initialDomain.add(i);
			}
			for(int i = 0; i < boardSize; i++) 
				for(int j = 0; j < boardSize; j++)
					if(v.box == boxCalc(i, j) || v.column == j || v.row == i)
						if(initialDomain.contains(vals[i][j])) {
							//System.out.println(i +" " +  j + " "+ boxCalc(i, j) + " " + vals[i][j]);
							initialDomain.remove(vals[i][j]);
						}
			v.domain = initialDomain;
		}
	}
	
	public static void printMa(int[][] vals) {
		System.out.println();
		for(int i = 0; i < vals.length; i++) {
			for(int j = 0; j < vals.length; j++) {
				System.out.print(vals[i][j] + " ");
			}
			System.out.println();
		}
	}
	

}