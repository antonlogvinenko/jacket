public abstract class AClosure {

    final private int arity;
    protected Object[] arguments;
    private int argsLength = 0;
    
    public AClosure(int arity) {
	this.arity = arity;
	this.arguments = new Object[arity];
    }

    public Object _invoke(Object... args) {
	if (argsLength + args.length > arity) {
	    throw new RuntimeException("Too much arguments");
	}
	for (int i = 0; i < args.length; i++) {
	    this.arguments[i + argsLength] = args[i];
	}
	this.argsLength += args.length;
	if (argsLength == arity) {
	    return invoke();
	}
	return this;
    }

    abstract public Object invoke();
    
}
