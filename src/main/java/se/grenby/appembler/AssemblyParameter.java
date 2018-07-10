package se.grenby.appembler;

class AssemblyParameter {

    public enum Type {
        VALUE, REFERENCE, AUTO_REFERENCE
    }

    private final String name;
    private final Type type;
    private final Class<?> klass;
    private final Object object;

    public static AssemblyParameter value(String name, Object object) {
        return new AssemblyParameter(name, Type.VALUE, null, object);
    }

    public static AssemblyParameter reference(String name, Class<?> klass) {
        return new AssemblyParameter(name, Type.REFERENCE, klass, null);
    }

    public static AssemblyParameter autoReference(String name) {
        return new AssemblyParameter(name, Type.AUTO_REFERENCE, null, null);
    }

    private AssemblyParameter(String name, Type type, Class<?> klass, Object object) {
        this.name = name;
        this.type = type;
        this.klass = klass;
        this.object = object;
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
