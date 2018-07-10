package se.grenby.appembler;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;

public class Appembler {

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
                            assemblyInstruction.getScope(),
                            assemblyParameters.toArray(new AssemblyParameter[assemblyParameters.size()])));
                }
            }
        }

        if (constructionInstructions.size() == 0) {
            throw new RuntimeException("No matching constructor found.");
        } else if (constructionInstructions.size() == 1) {
            constructors.put(assemblyInstruction.getKlass(), constructionInstructions.get(0));
        } else {
            throw new RuntimeException("Multiple matching constructors found. Failed to match assembly instruction to a constructor.");
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
                case AUTO_WIRE:
                    correctParameter = true;
                    break;
                default:
                    throw new IllegalStateException("What is going on?!?");
            }
        }

        return correctParameter;
    }

    public <T> T assemble(Class<T> klass) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        return assemble(klass, new HashSet<>());
    }

    private <T> T assemble(Class<T> klass, Set<Class<?>> touched) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if (touched.contains(klass)) {
            throw new IllegalStateException("Cyclic dependencies are not allowed. Class " + klass.getName() + " was found at twice.");
        } else {
            touched.add(klass);
        }

        ConstructionInstruction instruction = retrieveConstructionInstruction(klass);
        if (instruction == null) {
            throw new RuntimeException("No assembly instructions have been supplied for class " + klass + ".");
        }

        switch (instruction.scope) {
            case PROTOTYPE: {
                return construct(klass, instruction, touched);
            }
            case THREAD_LOCAL: {
                return constructOrRetrieve(klass, instruction, threadLocalInstances.get(), touched);
            }
            case SINGLETON: {
                return constructOrRetrieve(klass, instruction, instances, touched);
            }
            default: {
                throw new IllegalArgumentException("ConstructionScope " + constructors.get(klass).scope + " not handled!");
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

    private <T> T constructOrRetrieve(Class<T> klass, ConstructionInstruction instruction, Map<Class<?>, Object> map, Set<Class<?>> touched) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        T instance;
        if (map.get(klass) == null) {
            instance = construct(klass, instruction, touched);
            map.put(klass, instance);
        } else {
            instance = (T) map.get(klass);
        }
        return instance;
    }

    private <T> T construct(Class<T> klass, ConstructionInstruction instruction, Set<Class<?>> touched) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        var list = new ArrayList<>();
        int i = 0;
        for (AssemblyParameter p : instruction.assemblyParameters) {
            if (p.getType() == AssemblyParameter.Type.VALUE) {
                list.add(p.getObject());
            } else if (p.getType() == AssemblyParameter.Type.REFERENCE) {
                list.add(assemble(p.getKlass(), touched));
            } else if (p.getType() == AssemblyParameter.Type.AUTO_WIRE) {
                java.lang.reflect.Parameter rp = instruction.constructor.getParameters()[i];
                Class<?> klass2 = rp.getType();
                list.add(assemble(klass2, touched));
            }
            i++;
        }

        System.out.println(list);
        return (T) instruction.constructor.newInstance(list.toArray());
    }

}
