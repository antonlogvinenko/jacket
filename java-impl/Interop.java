import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

final public class Interop {

    private Interop() {}

    static Map<Class, Class> typingMap = new HashMap<Class, Class>() {{
	    put(int.class, Long.class);
	    put(Integer.class, Long.class);
	    put(byte.class, Long.class);
	    put(Byte.class, Long.class);
	    put(short.class, Long.class);
	    put(Short.class, Long.class);
	    put(long.class, Long.class);
	    put(float.class, Double.class);
	    put(Float.class, Double.class);
	    put(double.class, Double.class);
	    put(boolean.class, Boolean.class);
	}};

    public static Object instantiate(String className, Object[] arguments)
	throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException,
	       InstantiationException {
	Constructor ctor = find(className, arguments);
	if (ctor == null) {
	    return null;
	}
	Class<?>[] types = ctor.getParameterTypes();
	castArguments(arguments, types);
	return ctor.newInstance(arguments);
    }

    static public void castArguments(Object[] arguments, Class<?>[] types) {
	for (int i = 0; i < arguments.length; i++) {
	    Object arg = arguments[i];
	    Class<?> type = types[i];
	    if ((arg instanceof Long && !type.equals(Long.class) && !type.equals(Integer.class)) ||
		(arg instanceof Double && !type.equals(Double.class) && !type.equals(double.class))) {
		arguments[i] = numberCast((Number) arg, type);
	    }
	}
    }

    static public Object numberCast(Number arg, Class<?> type) {
	if (type.equals(float.class) || type.equals(Float.class)) {
	    return arg.floatValue();
	}
	if (type.equals(int.class) || type.equals(Integer.class)) {
	    return arg.intValue();
	}
	if (type.equals(byte.class) || type.equals(Byte.class)) {
	    return arg.byteValue();
	}
	if (type.equals(short.class) || type.equals(Short.class)) {
	    return arg.shortValue();
	}
	return arg;
    }

    static Constructor find(String className, Object[] arguments) throws ClassNotFoundException {
	int argsLength = arguments.length;

	Class<?>[] argTypes = new Class<?>[argsLength];
	for (int i = 0; i < argsLength; i++) {
	    argTypes[i] = arguments[i].getClass();
	}
	argTypes = convertParameterTypes(argTypes);

	Constructor[] ctors = Class.forName(className).getDeclaredConstructors();
	for (Constructor ctor : ctors) {
	    Class<?>[] ctorTypes = ctor.getParameterTypes();
	    if (ctorTypes.length == argsLength) {
		ctorTypes = convertParameterTypes(ctor.getParameterTypes());
		if (match(ctorTypes, argTypes)) {
		    return ctor;
		}
	    }
	}

	return null;
    }

    static boolean match(Class<?>[] types, Class<?>[] argTypes) {
	if (types.length != argTypes.length) {
	    return false;
	}
	for (int i = 0; i < types.length; i++) {
	    if (!types[i].isAssignableFrom(argTypes[i])) {
		return false;
	    }
	}
	return true;
    }

    private static Class<?>[] convertParameterTypes(Class<?>[] types) {
	for (int i = 0; i < types.length; i++) {
	    Class<?> type = types[i];
	    if (typingMap.containsKey(type)) {
		types[i] = typingMap.get(type);
	    }
	}
	return types;
    }
}
