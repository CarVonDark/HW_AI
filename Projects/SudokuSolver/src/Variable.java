import java.util.HashSet;

public class Variable {
	
	public int row;
	public int column;
	public int value;
	public HashSet<Integer> domainRow;
	public HashSet<Integer> domainCol;
	public HashSet<Integer> domainBox;
	
	
	public Variable(int row, int column) {
		this.row = row;
		this.column = column;
		this.value = 0;
		this.domainRow = new HashSet<Integer>();
		this.domainCol = new HashSet<Integer>();
		this.domainBox = new HashSet<Integer>();
	}
}
