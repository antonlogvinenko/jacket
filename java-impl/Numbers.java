public class Numbers {

    public static Double addD(Double a1, Double a2) {
	return a1 + a2;
    }

    public static Long addL(Long a1, Long a2) {
	return a1 + a2;
    }
    
    private static Number addN(Number a1, Number a2) {
	if (a1 instanceof Double || a2 instanceof Double) {
	    return addD(a1.doubleValue(), a2.doubleValue());
	}
	if (a1 instanceof Long || a2 instanceof Long) {
	    return addL(a1.longValue(), a2.longValue());
	}
	throw new RuntimeException("Exceptional cake");
    }

    public static Object add(Object a1, Object a2) {
	if (a1 instanceof Number && a2 instanceof Number) {
	    return addN((Number) a1, (Number) a2);
	}
	throw new RuntimeException("Exceptional cake");
    }


    private static Double mulD(Double a1, Double a2) {
	return a1 * a2;
    }

    private static Long mulL(Long a1, Long a2) {
	return a1 * a2;
    }

    private static Number mulN(Number a1, Number a2) {
	if (a1 instanceof Double || a2 instanceof Double) {
	    return mulD(a1.doubleValue(), a2.doubleValue());
	}
	if (a1 instanceof Long || a2 instanceof Long) {
	    return mulL(a1.longValue(), a2.longValue());
	}
	throw new RuntimeException("Exceptional cake");
    }

    public static Object mul(Object a1, Object a2) {
	if (a1 instanceof Number && a2 instanceof Number) {
	    return mulN((Number) a1, (Number) a2);
	}
	throw new RuntimeException("Exceptional cake");
    }



    private static Double subD(Double a1, Double a2) {
	return a1 - a2;
    }

    private static Long subL(Long a1, Long a2) {
	return a1 - a2;
    }

    private static Number subN(Number a1, Number a2) {
	if (a1 instanceof Double || a2 instanceof Double) {
	    return subD(a1.doubleValue(), a2.doubleValue());
	}
	if (a1 instanceof Long || a2 instanceof Long) {
	    return subL(a1.longValue(), a2.longValue());
	}
	throw new RuntimeException("Exceptional cake");
    }

    public static Object sub(Object a1, Object a2) {
	if (a1 instanceof Number && a2 instanceof Number) {
	    return subN((Number) a1, (Number) a2);
	}
	throw new RuntimeException("Exceptional cake");
    }


    private static Double divD(Double a1, Double a2) {
	return a1 / a2;
    }

    private static Long divL(Long a1, Long a2) {
	return a1 / a2;
    }

    private static Number divN(Number a1, Number a2) {
	if (a1 instanceof Double || a2 instanceof Double) {
	    return divD(a1.doubleValue(), a2.doubleValue());
	}
	if (a1 instanceof Long || a2 instanceof Long) {
	    return divL(a1.longValue(), a2.longValue());
	}
	throw new RuntimeException("Exceptional cake");
    }

    public static Object div(Object a1, Object a2) {
	if (a1 instanceof Number && a2 instanceof Number) {
	    return divN((Number) a1, (Number) a2);
	}
	throw new RuntimeException("Exceptional cake");
    }

}
