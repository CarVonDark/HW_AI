import java.util.HashSet;

public class Variable {
	
	public int row;
	public int column;
	public int value;
	public int box;
	public HashSet<Integer> domain;
	
	public Variable(int row, int column, int box) {
		this.row = row;
		this.column = column;
		this.value = 0;
		this.box = box;
		this.domain = new HashSet<Integer>();
	}
	
	public Variable(int row, int column, int box, int value) {
		this.row = row;
		this.column = column;
		this.value = value;
		this.box = box;
		this.domain = new HashSet<Integer>();
	}

}
