package se.grenby.appembler;

import se.grenby.appembler.exception.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;

public class Appembler {

    private final ConstructionScope scope;

    private static class ConstructionInstruction {
        final Constructor<?> constructor;
        final ConstructionScope scope;
        final AssemblyParameter[] assemblyParameters;

        public ConstructionInstruction(Constructor<?> constructor, ConstructionScope scope, AssemblyParameter[] assemblyParameters) {
            this.constructor = constructor;
            this.scope = scope;
            this.assemblyParameters = assemblyParameters;
        }
    }

    private final Map<Class<?>, ConstructionInstruction> constructors = new HashMap<>();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final ThreadLocal<Map<Class<?>, Object>> threadLocalInstances = new ThreadLocal<>();

    public Appembler() {
        this.scope = ConstructionScope.SINGLETON;
    }

    public Appembler(ConstructionScope scope) {
        this.scope = scope;
    }

    public void instruction(AssemblyInstruction assemblyInstruction) {
        List<AssemblyParameter> assemblyParameters = assemblyInstruction.getAssemblyParameters();
        int size = assemblyInstruction.getAssemblyParameters().size();

        ArrayList<ConstructionInstruction> constructionInstructions = new ArrayList<>();
        Constructor<?>[] cs = assemblyInstruction.getKlass().getConstructors();
        for (Constructor<?> c : cs) {
            if (size == c.getParameterCount()) {
                Parameter[] constructorParameters = c.getParameters();
                int correctParameters = 0;
                for (int i = 0; i < size; i++) {
                    AssemblyParameter ap = assemblyParameters.get(i);
                    Parameter cp = constructorParameters[i];
                    if (isCorrectConstructorParameter(ap, cp)) {
                        correctParameters++;
                    }
                }
                if (correctParameters == size) {
                    constructionInstructions.add(new ConstructionInstruction(c,
                            assemblyInstruction.getScope() == null ? scope : assemblyInstruction.getScope(),
                            assemblyParameters.toArray(new AssemblyParameter[assemblyParameters.size()])));
                }
            }
        }

        if (constructionInstructions.size() == 0) {
            throw new NoMatchingConstructorException("No matching constructor found.");
        } else if (constructionInstructions.size() == 1) {
            constructors.put(assemblyInstruction.getKlass(), constructionInstructions.get(0));
        } else {
            throw new AmbiguousConstructorMatchingException("Multiple matching constructors found. Failed to match assembly instruction to a constructor.");
        }
    }

    private boolean isCorrectConstructorParameter(AssemblyParameter ap, Parameter cp) {
        boolean correctParameter = false;
        if (ap.getName() == null ||
                ap.getName() != null && ap.getName().equals(cp.getName())) {
            switch (ap.getType()) {
                case VALUE:
                    if (ap.getObject().getClass().equals(cp.getType())) {
                        correctParameter = true;
                    }
                    break;
                case REFERENCE:
                    if (cp.getType().isAssignableFrom​(ap.getKlass())) {
                        correctParameter = true;
                    }
                    break;
                case AUTO_REFERENCE:
                    correctParameter = true;
                    break;
                default:
                    throw new RuntimeException("Unsupported assembly parameter type found: " + ap.getType());
            }
        }

        return correctParameter;
    }

    public <T> T assemble(Class<T> klass) {
        return assemble(klass, new HashSet<>());
    }

    private <T> T assemble(Class<T> klass, Set<Class<?>> touched) {
        if (touched.contains(klass)) {
            throw new CyclicDependencyException("Cyclic dependencies are not allowed. Class " + klass.getName() + " was found at twice.");
        } else {
            touched.add(klass);
        }

        ConstructionInstruction instruction = retrieveConstructionInstruction(klass);
        if (instruction == null) {
            throw new NoAssemblyInstructionException("No assembly instructions have been supplied for class " + klass + ".");
        }

        switch (instruction.scope) {
            case PROTOTYPE: {
                return construct(instruction, touched);
            }
            case THREAD_LOCAL: {
                return constructOrRetrieve(klass, instruction, threadLocalInstances.get(), touched);
            }
            case SINGLETON: {
                return constructOrRetrieve(klass, instruction, instances, touched);
            }
            default: {
                throw new RuntimeException("ConstructionScope " + constructors.get(klass).scope + " not handled!");
            }
        }
    }

    private <T> ConstructionInstruction retrieveConstructionInstruction(Class<T> klass) {
        var instruction = constructors.get(klass);
        if (instruction == null) {
            for (var ent : constructors.entrySet()) {
                System.out.println(klass);
                System.out.println(ent.getKey());
                System.out.println(klass.isAssignableFrom​(ent.getKey()));
                if (klass.isAssignableFrom​(ent.getKey())) {
                    instruction = ent.getValue();
                }
            }
        }
        return instruction;
    }

    private <T> T constructOrRetrieve(Class<T> klass, ConstructionInstruction instruction, Map<Class<?>, Object> map, Set<Class<?>> touched)  {
        T instance;
        if (map.get(klass) == null) {
            instance = construct(instruction, touched);
            map.put(klass, instance);
        } else {
            instance = (T) map.get(klass);
        }
        return instance;
    }

    private <T> T construct(ConstructionInstruction instruction, Set<Class<?>> touched) {
        var list = new ArrayList<>();
        int i = 0;
        for (AssemblyParameter p : instruction.assemblyParameters) {
            if (p.getType() == AssemblyParameter.Type.VALUE) {
                list.add(p.getObject());
            } else if (p.getType() == AssemblyParameter.Type.REFERENCE) {
                list.add(assemble(p.getKlass(), touched));
            } else if (p.getType() == AssemblyParameter.Type.AUTO_REFERENCE) {
                Parameter rp = instruction.constructor.getParameters()[i];
                list.add(assemble(rp.getType(), touched));
            }
            i++;
        }

        System.out.println(list);
        try {
            return (T) instruction.constructor.newInstance(list.toArray());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ConstructionFailedException("Construction of class " + instruction.constructor.getDeclaringClass() + " failed during assembly of object. ", e);
        }
    }

}
