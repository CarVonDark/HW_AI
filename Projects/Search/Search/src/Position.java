/** 
 * DO NOT MODIFY.
 * @author Michael Wollowski
 */
public class Position {
		public int row;  // Make private and add setters and getters MIW
		public int col;  // Make private and add setters and getters MIW
		
		Position(int row, int col){
			this.row = row;
			this.col = col;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj.getClass().equals(this.getClass())) {
				Position p = (Position) obj;
				return (p.row == this.row) && (p.col == this.col);
			} else {
				return false;
			}
		}
	}