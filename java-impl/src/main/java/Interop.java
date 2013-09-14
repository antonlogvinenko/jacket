import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.lang.reflect.Method;

final public class Interop {

	private Interop() {
	}

	public static void main(String... args)
		throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException,
		InvocationTargetException, NoSuchFieldException {
		System.out.println(accessInstance(new Long(42), "doubleValue", new Object[]{}));
	}

	private final static Map<Class, Class> typingMap = new HashMap<Class, Class>() {{
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

	/**
	 * Accessing
	 */
	public static Object accessInstance(Object object, String objectThing, Object[] arguments)
		throws IllegalAccessException, InvocationTargetException, ClassNotFoundException {
		if (arguments.length > 0) {
			return invokeMethod(object, objectThing, arguments);
		}
		try {
			return getField(object, objectThing);
		} catch (NoSuchFieldException e) {
			return invokeMethod(object, objectThing, arguments);
		}
	}

	private static Object getField(Object object, String fieldName)
		throws NoSuchFieldException, IllegalAccessException {
		return object.getClass().getField(fieldName).get(object);
	}

	private static Object invokeMethod(Object object, String methodName, Object[] arguments)
		throws ClassNotFoundException, InvocationTargetException, IllegalAccessException {
		Method method = findMethod(object.getClass(), methodName, arguments);
		if (method == null) {
			throw new RuntimeException("No method '" + methodName + "' found");
		}
		Class<?>[] types = method.getParameterTypes();
		arguments = castArguments(arguments, types);
		return method.invoke(object, arguments);
	}

	/**
	 * Instantiating
	 */
	public static Object instantiate(String className, Object[] arguments)
		throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException,
		InstantiationException {
		Constructor ctor = findConstructor(className, arguments);
		if (ctor == null) {
			throw new RuntimeException("Constructor not found");
		}
		Class<?>[] types = ctor.getParameterTypes();
		arguments = castArguments(arguments, types);
		return ctor.newInstance(arguments);
	}

	/**
	 * Find utilities
	 */
	private static Constructor findConstructor(String className, Object[] arguments) throws ClassNotFoundException {
		Constructor[] ctors = Class.forName(className).getDeclaredConstructors();
		for (Constructor ctor : ctors) {
			Class<?>[] ctorTypes = ctor.getParameterTypes();
			if (signaturesMatch(arguments, ctorTypes)) {
				return ctor;
			}
		}

		return null;
	}

	static Method findMethod(Class<?> c, String methodName, Object[] arguments) throws ClassNotFoundException {
		Method[] ctors = c.getDeclaredMethods();
		for (Method method : ctors) {
			if (method.getName().equals(methodName)) {
				Class<?>[] ctorTypes = method.getParameterTypes();
				if (signaturesMatch(arguments, ctorTypes)) {
					return method;
				}
			}
		}

		return null;
	}

	/**
	 * Casting arguments on call
	 */
	static public Object[] castArguments(Object[] arguments, Class<?>[] types) {
		for (int i = 0; i < arguments.length; i++) {
			Object arg = arguments[i];
			Class<?> type = types[i];
			if ((arg instanceof Long && !type.equals(Long.class) && !type.equals(Integer.class)) ||
				(arg instanceof Double && !type.equals(Double.class) && !type.equals(double.class))) {
				arguments[i] = numberCast((Number) arg, type);
			}
		}
		return arguments;
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

	/**
	 * Matching signatures
	 */
	private static boolean signaturesMatch(Object[] arguments, Class<?>[] ctorTypes) {
		Class<?>[] argTypes = getTypes(arguments);

		if (ctorTypes.length != argTypes.length) {
			return false;
		}
		ctorTypes = convertParameterTypes(ctorTypes);
		for (int i = 0; i < argTypes.length; i++) {
			if (!ctorTypes[i].isAssignableFrom(argTypes[i])) {
				return false;
			}
		}
		return true;
	}

	private static Class<?>[] getTypes(Object[] arguments) {
		int argsLength = arguments.length;

		Class<?>[] argTypes = new Class<?>[argsLength];
		for (int i = 0; i < argsLength; i++) {
			argTypes[i] = arguments[i].getClass();
		}

		return convertParameterTypes(argTypes);
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
