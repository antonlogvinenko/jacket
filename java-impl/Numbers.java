public class Numbers {

    public static Double add(Double a1, Double a2) {
	return a1 + a2;
    }

    public static Long add(Long a1, Long a2) {
	return a1 + a2;
    }
    
    public static Number add(Number a1, Number a2) {
	if (a1 instanceof Double || a2 instanceof Double) {
	    return add(a1.doubleValue(), a2.doubleValue());
	}
	if (a1 instanceof Long || a2 instanceof Long) {
	    return add(a1.longValue(), a2.longValue());
	}
	throw new RuntimeException("Exceptional cake");
    }


    public static Double mul(Double a1, Double a2) {
	return a1 * a2;
    }

    public static Long mul(Long a1, Long a2) {
	return a1 * a2;
    }

    public static Number mul(Number a1, Number a2) {
	if (a1 instanceof Double || a2 instanceof Double) {
	    return mul(a1.doubleValue(), a2.doubleValue());
	}
	if (a1 instanceof Long || a2 instanceof Long) {
	    return mul(a1.longValue(), a2.longValue());
	}
	throw new RuntimeException("Exceptional cake");
    }



    public static Double sub(Double a1, Double a2) {
	return a1 - a2;
    }

    public static Long sub(Long a1, Long a2) {
	return a1 - a2;
    }

    public static Number sub(Number a1, Number a2) {
	if (a1 instanceof Double || a2 instanceof Double) {
	    return sub(a1.doubleValue(), a2.doubleValue());
	}
	if (a1 instanceof Long || a2 instanceof Long) {
	    return sub(a1.longValue(), a2.longValue());
	}
	throw new RuntimeException("Exceptional cake");
    }


    public static Double div(Double a1, Double a2) {
	return a1 / a2;
    }

    public static Long div(Long a1, Long a2) {
	return a1 / a2;
    }

    public static Number div(Number a1, Number a2) {
	if (a1 instanceof Double || a2 instanceof Double) {
	    return div(a1.doubleValue(), a2.doubleValue());
	}
	if (a1 instanceof Long || a2 instanceof Long) {
	    return div(a1.longValue(), a2.longValue());
	}
	throw new RuntimeException("Exceptional cake");
    }

}
