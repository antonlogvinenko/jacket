public class Comparison {

    private static Boolean lessN(Number num1, Number num2) {
	return num1.doubleValue() < num2.doubleValue();
    }

    public static Boolean less(Object num1, Object num2) {
	if (num1 instanceof Number && num2 instanceof Number) {
	    return lessN((Number) num1, (Number) num2);
	}
	throw new RuntimeException("Exceptional cake");
    }


    
    private static Boolean greaterN(Number num1, Number num2) {
	return num1.doubleValue() > num2.doubleValue();
    }

    public static Boolean greater(Object num1, Object num2) {
	if (num1 instanceof Number && num2 instanceof Number) {
	    return greaterN((Number) num1, (Number) num2);
	}
	throw new RuntimeException("Exceptional cake");
    }


    
    private static Boolean greaterOrEqualN(Number num1, Number num2) {
	return num1.doubleValue() >= num2.doubleValue();
    }

    public static Boolean greaterOrEqual(Object num1, Object num2) {
	if (num1 instanceof Number && num2 instanceof Number) {
	    return greaterOrEqualN((Number) num1, (Number) num2);
	}
	throw new RuntimeException("Exceptional cake");
    }


    private static Boolean lessOrEqualN(Number num1, Number num2) {
	return num1.doubleValue() <= num2.doubleValue();
    }

    public static Boolean lessOrEqual(Object num1, Object num2) {
	if (num1 instanceof Number && num2 instanceof Number) {
	    return lessOrEqualN((Number) num1, (Number) num2);
	}
	throw new RuntimeException("Exceptional cake");
    }
}
