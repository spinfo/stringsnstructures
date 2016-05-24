package modules.matrix;

public class TypeSumTupel implements Comparable<TypeSumTupel> {

	private String type;
	private Double sum;
	
	public TypeSumTupel(String type, Double sum) {
		super();
		this.type = type;
		this.sum = sum;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Double getSum() {
		return sum;
	}

	public void setSum(Double sum) {
		this.sum = sum;
	}

	@Override
	public int compareTo(TypeSumTupel o) {
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
