import java.util.HashSet;

public class Variable {
	
	public int row;
	public int column;
	public int box;
	public int value;
	public HashSet<Integer> domainRow = new HashSet<Integer>();
	public HashSet<Integer> domainCol = new HashSet<Integer>();
	public HashSet<Integer> domainBox = new HashSet<Integer>();
	
	public Variable(int row, int column) {
		this.row = row;
		this.column = column;
		this.value = 0;
	}
}
