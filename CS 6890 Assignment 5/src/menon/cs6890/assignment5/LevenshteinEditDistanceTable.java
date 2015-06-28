package menon.cs6890.assignment5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores the Levenshtein Edit Distance table elements that can be accessed
 * through row and column coordinates
 */
public class LevenshteinEditDistanceTable {
	
	private ArrayList<LevenshteinEditDistanceTableElement> table;
	private String target;
	private int targetSize;
	private String source;
	private int sourceSize;
	private int insertionCost;
	private int deletionCost;
	private int substitutionCost;
	
	/**
	 * Constructor takes the target and source strings
	 * 
	 * @param target
	 * @param source
	 */
	public LevenshteinEditDistanceTable(String target, String source, int insertionCost, int deletionCost, int substitutionCost) {
				
		if (target == null || source == null) {
			throw new IllegalArgumentException("Null parameters not allowed.");
		}
	
		this.target = target;
		this.source = source;
		this.insertionCost = insertionCost;
		this.deletionCost = deletionCost;
		this.substitutionCost= substitutionCost;
		
		//Increase the size by 1 to allow for empty first element
		this.targetSize = this.target.length() + 1;
		this.sourceSize = this.source.length() + 1;
		
		this.table = new ArrayList<LevenshteinEditDistanceTableElement>(targetSize * sourceSize);
		for (int sourceStringIndex = 0; sourceStringIndex < this.sourceSize; ++sourceStringIndex) {
			for (int targetStringIndex = 0; targetStringIndex < this.targetSize; ++targetStringIndex) {
				this.table.add(new LevenshteinEditDistanceTableElement(null, null, null, 0, sourceStringIndex, targetStringIndex));
			}
		}
	}
	
	/**
	 * Convert the source string to the target string and update the LED table
	 */
	public void convertSourceToTarget() {
		
		LevenshteinEditDistanceTableElement levenshteinEditDistanceTableElement = null, currentLevenshteinEditDistanceTableElement = null;
		
		//Distance between characters in source string to the null character in the beginning of the target string
		//is the cost of deleting the characters.
		LevenshteinEditDistanceTableElement previousElement = null;
		for (int sourceStringIndex = 0; sourceStringIndex < this.sourceSize; ++sourceStringIndex) {
			levenshteinEditDistanceTableElement = getElement(0, sourceStringIndex);
			if (levenshteinEditDistanceTableElement != null) {
				levenshteinEditDistanceTableElement.setAlignmentCost(sourceStringIndex);
				levenshteinEditDistanceTableElement.setMinimumEditDistanceFromSameSource(previousElement);
				previousElement = levenshteinEditDistanceTableElement;
				setElement(0, sourceStringIndex, levenshteinEditDistanceTableElement);
			}
		}
		
		//Distance between the null character in source string and each element in the target string is the cost of inserting 
		//each target string character
		previousElement = null;
		for (int targetStringIndex = 0; targetStringIndex < this.targetSize; ++targetStringIndex) {
			levenshteinEditDistanceTableElement = getElement(targetStringIndex, 0);
			if (levenshteinEditDistanceTableElement != null) {
				levenshteinEditDistanceTableElement.setAlignmentCost(targetStringIndex);
				levenshteinEditDistanceTableElement.setMinimumEditDistanceFromSameTarget(previousElement);
				previousElement = levenshteinEditDistanceTableElement;
				setElement(targetStringIndex, 0, levenshteinEditDistanceTableElement);
			}
		}
		
		int distanceFromPreviousInSource = 0, distanceFromPreviousInTarget = 0, distanceFromPreviousInSourceAndTarget = 0;
		LevenshteinEditDistanceTableElement previousInSourceElement = null, previousInTargetElement = null, previousInSourceAndTargetElement = null;
		
		//Loop through the remaining elements in the table
		for (int sourceStringIndex = 1; sourceStringIndex < this.sourceSize; ++sourceStringIndex) {
			for (int targetStringIndex = 1; targetStringIndex < this.targetSize; ++targetStringIndex) {
				
				currentLevenshteinEditDistanceTableElement = getElement(targetStringIndex, sourceStringIndex);
				assert currentLevenshteinEditDistanceTableElement != null;
				
				//Get distance from previous element in source string
				previousInSourceElement = getNeighborAboveOnPreviousRow(targetStringIndex, sourceStringIndex);
				assert previousInSourceElement != null;
				distanceFromPreviousInSource = previousInSourceElement.getAlignmentCost() + this.deletionCost;
				
				//Get distance from previous element in target string 
				previousInTargetElement = getPreviousNeighborOnSameRow(targetStringIndex, sourceStringIndex);
				assert previousInTargetElement != null;
				distanceFromPreviousInTarget = previousInTargetElement.getAlignmentCost() + this.insertionCost;
				
				//Get distance from previous elements in source and target strings 
				previousInSourceAndTargetElement = getDiagonalNeighborOnPreviousRow(targetStringIndex, sourceStringIndex);
				assert previousInSourceAndTargetElement != null;
				if (this.source.charAt((currentLevenshteinEditDistanceTableElement.getSourceStringOffset() - 1)) == 
				    this.target.charAt((currentLevenshteinEditDistanceTableElement.getTargetStringOffset() - 1))) {
					//If same character in source and target, there is no cost
					distanceFromPreviousInSourceAndTarget = previousInSourceAndTargetElement.getAlignmentCost();
				} else {
					//Add the cost of substitution
					distanceFromPreviousInSourceAndTarget = previousInSourceAndTargetElement.getAlignmentCost() + this.substitutionCost;
				}
				
				currentLevenshteinEditDistanceTableElement.setAlignmentCost(Math.min(Math.min(distanceFromPreviousInTarget, distanceFromPreviousInSource), distanceFromPreviousInSourceAndTarget));
				currentLevenshteinEditDistanceTableElement.setMinimumEditDistanceFromSameSource(previousInSourceElement);
				currentLevenshteinEditDistanceTableElement.setMinimumEditDistanceFromSameTarget(previousInTargetElement);
				currentLevenshteinEditDistanceTableElement.setMinimumEditDistanceFromDiagonal(previousInSourceAndTargetElement);
				
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
	LevenshteinEditDistanceTableElement getElement(int column, int row) {
		
		//Check if valid values have been passed in
		if (!isValidCoordinate(column, row)) {
			return null;
		}
		
		int currentElementOffset = getTableOffset(column, row);
		
		LevenshteinEditDistanceTableElement element = this.table.get(currentElementOffset);

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
	private boolean setElement(int column, int row, LevenshteinEditDistanceTableElement element) {
		
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
	private LevenshteinEditDistanceTableElement getPreviousNeighborOnSameRow(int column, int row) {
		
		//Check if valid values have been passed in
		if (!isValidCoordinate(column, row)) {
			return null;
		}

		//Check if current point is on the border
		if (column == 0) {
			return null;
		}
		
		int currentElementOffset = getTableOffset(column, row);
		
		LevenshteinEditDistanceTableElement neighbor = this.table.get(currentElementOffset - 1);

		return neighbor;
		
	}
	
	/**
	 * Get the neighbor above on the previous row 
	 * 
	 * @param column
	 * @param row
	 * @return a table element
	 */
	private LevenshteinEditDistanceTableElement getNeighborAboveOnPreviousRow(int column, int row) {
		
		//Check if valid values have been passed in
		if (!isValidCoordinate(column, row)) {
			return null;
		}
		
		//Check if current point is on the border
		if (row == 0) {
			return null;
		}
		
		int currentElementOffset = getTableOffset(column, row);
		
		LevenshteinEditDistanceTableElement neighbor = this.table.get(currentElementOffset - this.targetSize);

		return neighbor;		

	}
	
	/**
	 * Get the diagonal neighbor or previous row
	 * 
	 * @param column
	 * @param row
	 * @return a table element
	 */
	private LevenshteinEditDistanceTableElement getDiagonalNeighborOnPreviousRow(int column, int row) {
		
		//Check if valid values have been passed in
		if (!isValidCoordinate(column, row)) {
			return null;
		}
		
		//Check if current point is on the border
		if (column == 0 || row == 0) {
			return null;
		}
		
		int currentElementOffset = getTableOffset(column, row);
		
		LevenshteinEditDistanceTableElement neighbor = table.get(currentElementOffset - this.targetSize - 1);
		
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
		
		if (column > this.targetSize - 1 || row > this.sourceSize - 1 || column < 0 || row < 0) {
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
		
		return row * this.targetSize + column;
		
	}
	
	/**
	 * @return a protected copy of the table
	 */
	public List<LevenshteinEditDistanceTableElement> getTable() {
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
		return this.sourceSize;
	}
	
	/**
	 * @return the size of the target string plus one (for the null element at the beginning of the string)
	 */
	public int getTargetSize() {
		return this.targetSize;
	}
	
	/**
	 * @return the substitution cost if the source string is to be replaced by the target string
	 */
	public int getFullStringSubstitutionCost() {
		
		int fullStringSubstitutionCost = 0;
		
		//Add the cost of deleting each character in source string
		fullStringSubstitutionCost += this.sourceSize - 1;
		
		//Add the cost of inserting each character from the target into the source string
		fullStringSubstitutionCost += this.targetSize - 1;
		
		return fullStringSubstitutionCost;

	}

}