package se.grenby.appembler;

import java.util.ArrayList;
import java.util.List;

public class AssemblyInstruction {

    private final Class<?> klass;
    private final ConstructionScope scope;
    private final List<AssemblyParameter> assemblyParameters;

    private AssemblyInstruction(Class<?> klass, ConstructionScope scope, List<AssemblyParameter> assemblyParameters) {
        this.klass = klass;
        this.scope = scope;
        this.assemblyParameters = assemblyParameters;
    }

    public static class Builder {
        private Class<?> klass;
        private ConstructionScope scope = ConstructionScope.SINGLETON;
        private List<AssemblyParameter> assemblyParameters = new ArrayList<AssemblyParameter>();

        public Builder(Class<?> klass) {
            this.klass = klass;
        }

        public Builder s(ConstructionScope scope) {
            this.scope = scope;
            return this;
        }

        public Builder val(String name, Object object) {
            AssemblyParameter ap = new AssemblyParameter(name, object);
            this.assemblyParameters.add(ap);
            return this;
        }

        public Builder ref(String name, Class<?> klass) {
            AssemblyParameter ap = new AssemblyParameter(name, klass);
            this.assemblyParameters.add(ap);
            return this;
        }

        public Builder auto(String name) {
            AssemblyParameter ap = new AssemblyParameter(name);
            this.assemblyParameters.add(ap);
            return this;
        }


        public AssemblyInstruction build() {
            return new AssemblyInstruction(klass, scope, assemblyParameters);
        }

    }

    public Class<?> getKlass() {
        return klass;
    }

    public ConstructionScope getScope() {
        return scope;
    }

    public List<AssemblyParameter> getAssemblyParameters() {
        return assemblyParameters;
    }
}
