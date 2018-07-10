package se.grenby.appembler;

class AssemblyParameter {
    public enum Type {
        VALUE, REFERENCE, AUTO_WIRE
    }

    private final String name;
    private final Type type;
    private final Class<?> klass;
    private final Object object;

    public AssemblyParameter(String name, Object object) {
        this.name = name;
        this.type = Type.VALUE;
        this.klass = null;
        this.object = object;
    }

//    public AssemblyParameter(Object object) {
//        this(null, object);
//    }

    public AssemblyParameter(String name, Class<?> klass) {
        this.name = name;
        this.type = Type.REFERENCE;
        this.klass = klass;
        this.object = null;
    }

//    public AssemblyParameter(Class<?> klass) {
//        this(null, klass);
//    }

    public AssemblyParameter(String name) {
        this.name = name;
        this.type = Type.AUTO_WIRE;
        this.klass = null;
        this.object = null;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Class<?> getKlass() {
        return klass;
    }

    public Object getObject() {
        return object;
    }
}
