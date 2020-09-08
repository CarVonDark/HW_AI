import java.util.HashSet;

public class Variable {
	
	public int row;
	public int column;
	public int value;
	public HashSet<Integer> domain;
	
	public Variable(int row, int column) {
		this.row = row;
		this.column = column;
		this.value = 0;
		this.domain = new HashSet<Integer>();
	}
	
	public Variable(int row, int column, int value) {
		this.row = row;
		this.column = column;
		this.value = value;
		this.domain = new HashSet<Integer>();
	}

}
