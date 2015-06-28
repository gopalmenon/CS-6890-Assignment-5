package menon.cs6890.assignment5;

public class TableElement {
	
	private TableElement minimumDistanceFromSameSource;
	private TableElement minimumDistanceFromSameTarget;
	private TableElement minimumDistanceFromDiagonal;
	private int alignmentCost;
	private int sourceStringOffset;
	private int targetStringOffset;
	
	/**
	 * Constructor
	 * 
	 * @param minimumDistanceFromSameSource
	 * @param minimumDistanceFromSameTarget
	 * @param minimumDistanceFromDiagonal
	 * @param alignmentCost
	 * @param sourceStringOffset
	 * @param targetStringOffset
	 */
	public TableElement(TableElement minimumDistanceFromSameSource,
			            TableElement minimumDistanceFromSameTarget,
			            TableElement minimumDistanceFromDiagonal,
			            int alignmentCost,
			            int sourceStringOffset,
			            int targetStringOffset) {
		
		if (alignmentCost < 0 || sourceStringOffset < 0 || targetStringOffset < 0) {
			throw new IllegalArgumentException("Negative values not allowed");
		}
		
		this.minimumDistanceFromSameSource = minimumDistanceFromSameSource;
		this.minimumDistanceFromSameTarget = minimumDistanceFromSameTarget;
		this.minimumDistanceFromDiagonal = minimumDistanceFromDiagonal;
		this.alignmentCost = alignmentCost;
		this.sourceStringOffset = sourceStringOffset;
		this.targetStringOffset = targetStringOffset;
	}
	
	
	/**
	 * @return the back trace element - the element with the least cost
	 */
	public TableElement getBackTraceElement() {
		
		TableElement returnValue = null;
		
		TableElement backTraceElement1 = this.minimumDistanceFromSameSource != null ? this.minimumDistanceFromSameSource : null;
		TableElement backTraceElement2 = this.minimumDistanceFromSameTarget != null ? this.minimumDistanceFromSameTarget : null;
		TableElement backTraceElement3 = this.minimumDistanceFromDiagonal != null ? this.minimumDistanceFromDiagonal : null;
		
		if (backTraceElement1 != null) {
			returnValue = backTraceElement1;
		}
		
		if (backTraceElement2 != null) {
			if (returnValue != null) {
				if (backTraceElement2.getAlignmentCost() < returnValue.getAlignmentCost()) {
					returnValue = backTraceElement2;
				}
			} else {
				returnValue = backTraceElement2;
			}
		}
		
		if (backTraceElement3 != null) {
			if (returnValue != null) {
				if (backTraceElement3.getAlignmentCost() < returnValue.getAlignmentCost()) {
					returnValue = backTraceElement3;
				}
			} else {
				returnValue = backTraceElement3;
			}
		}
		
		return returnValue;
				
	}
	

	public TableElement getMinimumDistanceFromSameSource() {
		return minimumDistanceFromSameSource;
	}


	public void setMinimumDistanceFromSameSource(
			TableElement minimumDistanceFromSameSource) {
		this.minimumDistanceFromSameSource = minimumDistanceFromSameSource;
	}


	public TableElement getMinimumDistanceFromSameTarget() {
		return minimumDistanceFromSameTarget;
	}


	public void setMinimumDistanceFromSameTarget(
			TableElement minimumDistanceFromSameTarget) {
		this.minimumDistanceFromSameTarget = minimumDistanceFromSameTarget;
	}


	public TableElement getMinimumDistanceFromDiagonal() {
		return minimumDistanceFromDiagonal;
	}


	public void setMinimumDistanceFromDiagonal(
			TableElement minimumDistanceFromDiagonal) {
		this.minimumDistanceFromDiagonal = minimumDistanceFromDiagonal;
	}
	
	public int getAlignmentCost() {
		return alignmentCost;
	}
	public void setAlignmentCost(int alignmentCost) {
		this.alignmentCost = alignmentCost;
	}
	public int getSourceStringOffset() {
		return sourceStringOffset;
	}
	public void setSourceStringOffset(int sourceStringOffset) {
		this.sourceStringOffset = sourceStringOffset;
	}
	public int getTargetStringOffset() {
		return targetStringOffset;
	}
	public void setTargetStringOffset(int targetStringOffset) {
		this.targetStringOffset = targetStringOffset;
	}
	
}
