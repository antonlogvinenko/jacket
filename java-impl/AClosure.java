public abstract class AClosure implements Cloneable {

    final private int arity;
    protected Object[] arguments;
    
    public AClosure(int arity) {
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
	for (int i = 0; i < arguments.length; i++) {
	    newArguments[i] = arguments[i];
	}
	for (int i = 0; i < args.length; i++) {
	    newArguments[i + arguments.length] = args[i];
	}
	
	AClosure aclosure = (AClosure) this.clone();
	aclosure.setArguments(newArguments);
	if (newArguments.length == arity) {
	    return aclosure.invoke();
	}
	return aclosure;
    }

    abstract protected Object invoke();
    
}
