public abstract class AClosure {

    final private int arity;
    protected Object[] arguments;
    
    public AClosure(int arity) {
	this.arity = arity;
	this.arguments = new Object[arity];
    }

    public Object _invoke(Object... args) {
	this.arguments = args;
	int argsLength = args.length;
	if (argsLength != arity) {
	    String message = "Wrong number of arguments: " +
		argsLength + ", instead of: " + arity;
	    throw new RuntimeException(message);
	    }
	return invoke();
    }

    abstract public Object invoke();
    
}
