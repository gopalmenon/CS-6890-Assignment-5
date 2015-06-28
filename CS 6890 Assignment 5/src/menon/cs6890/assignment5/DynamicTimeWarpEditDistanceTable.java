package menon.cs6890.assignment5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores the DTW table elements that can be accessed
 * through row and column coordinates
 */
public class DynamicTimeWarpEditDistanceTable {
	
	private ArrayList<TableElement> table;
	private String target;
	private String source;
	
	/**
	 * Constructor takes the target and source strings
	 * 
	 * @param target
	 * @param source
	 */
	public DynamicTimeWarpEditDistanceTable(String target, String source) {
				
		if (target == null || source == null) {
			throw new IllegalArgumentException("Null parameters not allowed.");
		}
	
		this.target = target;
		this.source = source;
		
		this.table = new ArrayList<TableElement>(this.target.length() * this.source.length());
		for (int sourceStringIndex = 0; sourceStringIndex < this.source.length(); ++sourceStringIndex) {
			for (int targetStringIndex = 0; targetStringIndex < this.target.length(); ++targetStringIndex) {
				this.table.add(new TableElement(null, null, null, 0, sourceStringIndex, targetStringIndex));
			}
		}
	}
	
	/**
	 * Convert the source string to the target string and update the DTW table
	 */
	public void convertSourceToTarget() {
		
		//Find the cost of warping the source to the first character in the target.
		TableElement currentTableElement = null, previousElement = null;
		for (int sourceStringIndex = 0; sourceStringIndex < this.source.length(); ++sourceStringIndex) {
			currentTableElement = getElement(0, sourceStringIndex);
			assert currentTableElement != null;
			if (this.source.charAt(sourceStringIndex) == this.target.charAt(0)) {
				if (previousElement == null) {
					currentTableElement.setAlignmentCost(0);
				} else {
					currentTableElement.setAlignmentCost(previousElement.getAlignmentCost());
				}
			} else {
				if (previousElement == null) {
					currentTableElement.setAlignmentCost(1);
				} else {
					currentTableElement.setAlignmentCost(previousElement.getAlignmentCost() + 1);
				}
			}
			currentTableElement.setMinimumDistanceFromSameSource(previousElement);
			previousElement = currentTableElement;
			setElement(0, sourceStringIndex, currentTableElement);
		}
		
		//Find the cost of warping the first character in the source to the target.
		previousElement = null;
		for (int targetStringIndex = 0; targetStringIndex < this.target.length(); ++targetStringIndex) {
			currentTableElement = getElement(targetStringIndex, 0);
			assert currentTableElement != null;
			if (this.target.charAt(targetStringIndex) == this.source.charAt(0)) {
				if (previousElement == null) {
					currentTableElement.setAlignmentCost(0);
				} else {
					currentTableElement.setAlignmentCost(previousElement.getAlignmentCost());
				}
			} else {
				if (previousElement == null) {
					currentTableElement.setAlignmentCost(1);
				} else {
					currentTableElement.setAlignmentCost(previousElement.getAlignmentCost() + 1);
				}
			}
			currentTableElement.setMinimumDistanceFromSameTarget(previousElement);
			previousElement = currentTableElement;
			setElement(targetStringIndex, 0, currentTableElement);
		}		
		
		int distanceFromPreviousInSource = 0, distanceFromPreviousInTarget = 0, distanceFromPreviousInSourceAndTarget = 0;
		TableElement previousInSourceElement = null, previousInTargetElement = null, previousInSourceAndTargetElement = null;
		
		//Loop through the elements in the table
		for (int sourceStringIndex = 1; sourceStringIndex < this.source.length(); ++sourceStringIndex) {
			for (int targetStringIndex = 1; targetStringIndex < this.target.length(); ++targetStringIndex) {
				
				currentTableElement = getElement(targetStringIndex, sourceStringIndex);
				assert currentTableElement != null;
				
				//Get distance from previous element in source string
				previousInSourceElement = getNeighborAboveOnPreviousRow(targetStringIndex, sourceStringIndex);
				assert previousInSourceElement != null;
				if (this.source.charAt(sourceStringIndex) == this.target.charAt(targetStringIndex)) {
					distanceFromPreviousInSource = previousInSourceElement.getAlignmentCost();
				} else {
					distanceFromPreviousInSource = previousInSourceElement.getAlignmentCost() + 1;
				}
				
				//Get distance from previous element in target string 
				previousInTargetElement = getPreviousNeighborOnSameRow(targetStringIndex, sourceStringIndex);
				assert previousInTargetElement != null;
				if (this.source.charAt(sourceStringIndex) == this.target.charAt(targetStringIndex)) {
					distanceFromPreviousInTarget = previousInTargetElement.getAlignmentCost();
				} else {
					distanceFromPreviousInTarget = previousInTargetElement.getAlignmentCost() + 1;
				}
				//Get distance from previous elements in source and target strings 
				previousInSourceAndTargetElement = getDiagonalNeighborOnPreviousRow(targetStringIndex, sourceStringIndex);
				assert previousInSourceAndTargetElement != null;
				if (this.source.charAt(sourceStringIndex) == this.target.charAt(targetStringIndex)) {
					distanceFromPreviousInSourceAndTarget = previousInSourceAndTargetElement.getAlignmentCost();
				} else {

					distanceFromPreviousInSourceAndTarget = previousInSourceAndTargetElement.getAlignmentCost() + 1;
				}
				
				currentTableElement.setAlignmentCost(Math.min(Math.min(distanceFromPreviousInTarget, distanceFromPreviousInSource), distanceFromPreviousInSourceAndTarget));
				currentTableElement.setMinimumDistanceFromSameSource(previousInSourceElement);
				currentTableElement.setMinimumDistanceFromSameTarget(previousInTargetElement);
				currentTableElement.setMinimumDistanceFromDiagonal(previousInSourceAndTargetElement);
				
			}
		}
		
	}
	
	/**
	 * Get element by column and row number
	 * 
	 * @param column
	 * @param row
	 * @return a table element
	 */
	TableElement getElement(int column, int row) {
		
		//Check if valid values have been passed in
		if (!isValidCoordinate(column, row)) {
			return null;
		}
		
		int currentElementOffset = getTableOffset(column, row);
		
		TableElement element = this.table.get(currentElementOffset);

		return element;
		
	}
	
	/**
	 * Put an element into the specified row and column
	 * 
	 * @param column
	 * @param row
	 * @param element
	 * @return true if the element is inserted
	 */
	private boolean setElement(int column, int row, TableElement element) {
		
		//Check if valid coordinate values have been passed in
		if (!isValidCoordinate(column, row)) {
			return false;
		}
		
		int currentElementOffset = getTableOffset(column, row);
		
		this.table.set(currentElementOffset, element);
		
		return true;
	}

	/**
	 * Get the previous neighbor on the same row
	 * 
	 * @param column
	 * @param row
	 * @return a table element
	 */
	private TableElement getPreviousNeighborOnSameRow(int column, int row) {
		
		//Check if valid values have been passed in
		if (!isValidCoordinate(column, row)) {
			return null;
		}

		//Check if current point is on the border
		if (column == 0) {
			return null;
		}
		
		int currentElementOffset = getTableOffset(column, row);
		
		TableElement neighbor = this.table.get(currentElementOffset - 1);

		return neighbor;
		
	}
	
	/**
	 * Get the neighbor above on the previous row 
	 * 
	 * @param column
	 * @param row
	 * @return a table element
	 */
	private TableElement getNeighborAboveOnPreviousRow(int column, int row) {
		
		//Check if valid values have been passed in
		if (!isValidCoordinate(column, row)) {
			return null;
		}
		
		//Check if current point is on the border
		if (row == 0) {
			return null;
		}
		
		int currentElementOffset = getTableOffset(column, row);
		
		TableElement neighbor = this.table.get(currentElementOffset - this.target.length());

		return neighbor;		

	}
	
	/**
	 * Get the diagonal neighbor or previous row
	 * 
	 * @param column
	 * @param row
	 * @return a table element
	 */
	private TableElement getDiagonalNeighborOnPreviousRow(int column, int row) {
		
		//Check if valid values have been passed in
		if (!isValidCoordinate(column, row)) {
			return null;
		}
		
		//Check if current point is on the border
		if (column == 0 || row == 0) {
			return null;
		}
		
		int currentElementOffset = getTableOffset(column, row);
		
		TableElement neighbor = table.get(currentElementOffset - this.target.length() - 1);
		
		return neighbor;
		
	}
	
	/**
	 * Check if the coordinates passed in are valid
	 * 
	 * @param column
	 * @param row
	 * @return true if coordinates are valid, else return false
	 */
	private boolean isValidCoordinate(int column, int row) {
		
		if (column > this.target.length() - 1 || row > this.source.length() - 1 || column < 0 || row < 0) {
			return false;
		} else {
			return true;
		}
				
	}
	
	/**
	 * Return table offset in array list given x and y coordinates
	 * 
	 * @param column
	 * @param row
	 * @return table offset
	 */
	private int getTableOffset(int column, int row) {
		
		return row * this.target.length() + column;
		
	}
	
	/**
	 * @return a protected copy of the table
	 */
	public List<TableElement> getTable() {
		return Collections.unmodifiableList(this.table);
	}
	
	/**
	 * @return the source string
	 */
	public String getSource() {
		return this.source;
	}
	
	/**
	 * @return the target string
	 */
	public String getTarget() {
		return this.target;
	}
	
	/**
	 * @return the size of the source string plus one (for the null element at the beginning of the string)
	 */
	public int getSourceSize() {
		return this.source.length();
	}
	
	/**
	 * @return the size of the target string plus one (for the null element at the beginning of the string)
	 */
	public int getTargetSize() {
		return this.target.length();
	}
	
	/**
	 * @return the substitution cost if the source string is to be replaced by the target string
	 */
	public int getFullStringSubstitutionCost() {
		
		int fullStringSubstitutionCost = 0;
		
		//Add the cost of deleting each character in source string
		fullStringSubstitutionCost += this.source.length();
		
		//Add the cost of inserting each character from the target into the source string
		fullStringSubstitutionCost += this.target.length();
		
		return fullStringSubstitutionCost;

	}

}