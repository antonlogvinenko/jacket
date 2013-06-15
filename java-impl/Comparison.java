public class Comparison {

    public static Boolean less(Number num1, Number num2) {
	return num1.doubleValue() < num2.doubleValue();
    }

    public static Boolean greater(Number num1, Number num2) {
	return num1.doubleValue() > num2.doubleValue();
    }

    public static Boolean greaterOrEqual(Number num1, Number num2) {
	return num1.doubleValue() >= num2.doubleValue();
    }

    public static Boolean lessOrEqual(Number num1, Number num2) {
	return num1.doubleValue() <= num2.doubleValue();
    }
}
