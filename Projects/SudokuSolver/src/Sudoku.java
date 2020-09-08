import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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
					vars.add(new Variable(i, j));
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
		
		long startTime = System.currentTimeMillis();
		
		initiateDomains(vars, vals);
		solve(vars, vals);
		
		long endTime = System.currentTimeMillis();

		long duration = (endTime - startTime);  
		
		System.out.println("Duration: " + duration);

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
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter("E:/Rose/SchoolWork/Current/CSSE413_ArtificialIntelligence/"
					+ filename.substring(0, filename.length() - 4) + "Solution.txt"));
			writerMA(writer, done(vars), vals);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writerMA(BufferedWriter writer, boolean done, int[][] vals) throws IOException {
		if (done) {
			for (int i = 0; i < vals.length; i++) {
				for (int j = 0; j < vals.length; j++) {
					writer.write(vals[i][j] + " ");
				}
				writer.write('\n');
			}
			writer.close();
		} else {
			writer.write("-1");
			writer.close();
		}
	}

	public static void solve(ArrayList<Variable> vars, int[][] vals) {
		int index = 0;
		while (index < vars.size()) {
			Variable current = vars.get(index);
			current.value++;
			while (current.value <= boardSize) {
				if (current.domainRow.contains(current.value) || current.domainCol.contains(current.value) 
						|| current.domainBox.contains(current.value)) {
					current.value++;
				} else {
					break;
				}
			}
			boolean outOfBound = forwardChecking(current, vars);
			vals[current.row][current.column] = current.value;
			
			if (outOfBound || current.value > boardSize) {
				if (index >= 1) {
					current.value = 0;
					Variable prev = vars.get(index - 1);
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
		for (Variable v : vars)
			if (v.value == 0 || v.value > boardSize)
				return false;
		return true;
	}

	public static boolean forwardChecking(Variable var, ArrayList<Variable> vars) {
		for (Variable v : vars) {
			if ((v.row / partitionSize == var.row / partitionSize
					&& v.column / partitionSize == var.column / partitionSize))
				v.domainBox.add(var.value);
			else if (v.row == var.row)
				v.domainRow.add(var.value);
			else if (v.column == var.column)
				v.domainCol.add(var.value);
			if(v.domainRow.size() == boardSize || v.domainRow.size() == boardSize || v.domainRow.size() == boardSize) {
				return true;
			}
		}
		return false;
	}

	public static void forwardUnChecking(Variable var, ArrayList<Variable> vars) {
		for (Variable v : vars)
			if ((v.row / partitionSize == var.row / partitionSize
					&& v.column / partitionSize == var.column / partitionSize)) {
				if (v.domainBox.contains(var.value))
					v.domainBox.remove(var.value);
			} else if (v.row == var.row) {
				if (v.domainRow.contains(var.value))
					v.domainRow.remove(var.value);
			} else if (v.column == var.column) {
				if (v.domainCol.contains(var.value))
					v.domainCol.remove(var.value);
			}
	}

	public static void initiateDomains(ArrayList<Variable> vars, int[][] vals) {
		for (Variable v : vars) {
			HashSet<Integer> initialDomainRow = new HashSet<Integer>();
			HashSet<Integer> initialDomainCol = new HashSet<Integer>();
			HashSet<Integer> initialDomainBox = new HashSet<Integer>();

			for (int i = 0; i < boardSize; i++) {
				for (int j = 0; j < boardSize; j++) {
					if ((v.row / partitionSize == i / partitionSize && v.column / partitionSize == j / partitionSize))
						initialDomainBox.add(vals[i][j]);
					else if (v.row == i)
						initialDomainRow.add(vals[i][j]);
					else if (v.column == j)
						initialDomainCol.add(vals[i][j]);
				}
			}
			v.domainRow = initialDomainRow;
			v.domainCol = initialDomainCol;
			v.domainBox = initialDomainBox;
			
		}
	}

	public static void printMa(int[][] vals) {
		System.out.println();
		for (int i = 0; i < vals.length; i++) {
			for (int j = 0; j < vals.length; j++) {
				System.out.print(vals[i][j] + " ");
			}
			System.out.println();
		}
	}

}