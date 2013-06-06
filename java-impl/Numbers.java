public class Numbers {

    public static Double add(Double a1, Double a2) {
	return a1 + a2;
    }

    public static Float add(Float a1, Float a2) {
	return a1 + a2;
    }

    public static Long add(Long a1, Long a2) {
	return a1 + a2;
    }

    public static Integer add(Integer a1, Integer a2) {
	return a1 + a2;
    }

    public static Integer add(Short a1, Short a2) {
	return a1 + a2;
    }

    public static Integer add(Byte a1, Byte a2) {
	return a1 + a2;
    }
    
    public static Object add0(Number a1, Number a2) {
	if (a1 instanceof Double || a2 instanceof Double) {
	    return add(a1.doubleValue(), a2.doubleValue());
	}
	if (a1 instanceof Float || a2 instanceof Float) {
	    return add(a1.floatValue(), a2.floatValue());
	}
	if (a1 instanceof Long || a2 instanceof Long) {
	    return add(a1.longValue(), a2.longValue());
	}
	if (a1 instanceof Integer || a2 instanceof Integer) {
	    return add(a1.intValue(), a2.intValue());
	}
	if (a1 instanceof Short || a2 instanceof Short) {
	    return add(a1.shortValue(), a2.shortValue());
	}
	if (a1 instanceof Byte || a2 instanceof Byte) {
	    return add(a1.byteValue(), a2.byteValue());
	}
	throw new RuntimeException("Exceptional cake");
    }

    public static String add(Number a1, Number a2) {
	return add0(a1, a2).toString();
    }

}
