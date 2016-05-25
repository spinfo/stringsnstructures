package modules.matrix;

public class ColumnSumTupel implements Comparable<ColumnSumTupel> {

	private String columnName;
	private Double sum;
	
	public ColumnSumTupel(String columnName, Double sum) {
		super();
		this.columnName = columnName;
		this.sum = sum;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public Double getSum() {
		return sum;
	}

	public void setSum(Double sum) {
		this.sum = sum;
	}

	@Override
	public int compareTo(ColumnSumTupel o) {
		int result;
		Double dist = o.getSum()-this.sum;
		if (dist < 1 && dist > 0)
			result = 1;
		else if (dist < 0 && dist > -1)
			result = -1;
		else result = dist.intValue();
		return result;
	}

}
