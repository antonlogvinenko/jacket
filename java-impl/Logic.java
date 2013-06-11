public class Logic {

    public static Boolean toBoolean(Object obj) {
	if (obj instanceof Boolean) {
	    return (boolean) obj;
	}
	if (obj instanceof Number) {
	    return obj != 0;
	}
	if (obj instanceof String) {
	    return !((String) obj).isEmpty();
	}
	throw new RuntimeException("Who is Mr. Putin?");
    }

    public static Boolean and(Boolean b1, Boolean b2) {
	return b1 && b2;
    }

    public static Boolean or(Boolean b1, Boolean b2) {
	return b1 || b2;
    }

    public static Boolean xor(Boolean b1, Boolean b2) {
	return (b1 && !b2) || (!b1 && b2);
    }
    
    public static Boolean not(Boolean b) {
	return !b;
    }
}
