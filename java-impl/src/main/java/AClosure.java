import static java.lang.System.arraycopy;

public abstract class AClosure implements Cloneable {

	final private int arity;
	protected Object[] arguments;
	protected Object[] closed;

	public AClosure(int arity) {
		this.arity = arity;
		this.arguments = new Object[0];
	}

	public AClosure(Object[] closed, int arity) {
		this.closed = closed;
		this.arity = arity;
		this.arguments = new Object[0];
	}

	private void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}

	public Object _invoke(Object... args) throws CloneNotSupportedException {
		if (arguments.length + args.length > arity) {
			throw new RuntimeException("Too much arguments");
		}

		Object[] newArguments = new Object[arguments.length + args.length];
		arraycopy(arguments, 0, newArguments, 0, arguments.length);
		arraycopy(args, 0, newArguments, arguments.length, args.length);

		AClosure aclosure = (AClosure) this.clone();
		aclosure.setArguments(newArguments);
		if (newArguments.length == arity) {
			return aclosure.invoke();
		}
		return aclosure;
	}

	abstract protected Object invoke();

}
