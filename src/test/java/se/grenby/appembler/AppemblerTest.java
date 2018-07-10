package se.grenby.appembler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AppemblerTest {

    @Test
    public void simpleTest() throws IllegalAccessException, InstantiationException, InvocationTargetException {
	    var aa = new Appembler();

	    aa.instruction(new AssemblyInstruction.Builder(Testing.class)
                .val("ett", "testa saker")
                .ref("testing2", Testing2.class)
                .auto("testing3")
                .build()
        );
        aa.instruction(new AssemblyInstruction.Builder(Testing2.class)
                .val("ett", "testa saker")
                .build()
        );
        aa.instruction(new AssemblyInstruction.Builder(Testing3.class)
                .val("ett", "testa saker")
                .build()
        );

	    Testing testing = aa.assemble(Testing.class);

	    assertNotNull(testing);
    }

    @Test
    public void incorrectParameterNameTest() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        var aa = new Appembler();

        Executable addinstruction = () -> {
            aa.instruction(new AssemblyInstruction.Builder(Testing.class)
                    .val("wrong", "not going to work")
                    .build()
            );
        };

        assertThrows(RuntimeException.class, addinstruction, "If parameter names are supplied they are required to be exist in a constructor at correct position.");
    }

    @Test
    public void incorrectValueTypeTest() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        var aa = new Appembler();

        Executable addinstruction = () -> {
            aa.instruction(new AssemblyInstruction.Builder(Testing.class)
                    .val("ett", 42)
                    .build()
            );
        };

        assertThrows(RuntimeException.class, addinstruction, "Parameter types are required to be the same for values.");
    }

    @Test
    public void incorrectReferenceTypeTest() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        var aa = new Appembler();

        Executable addinstruction = () -> {
            aa.instruction(new AssemblyInstruction.Builder(Testing.class)
                    .val("ett", "testa saker")
                    .ref("tva", Testing3.class)
                    .build()
            );
        };

        assertThrows(RuntimeException.class, addinstruction, "Parameter types are required to be the same for values.");
    }


    @Test
    public void cyclicTest() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        var aa = new Appembler();

        aa.instruction(new AssemblyInstruction.Builder(Testing.class)
                .val("ett", "testa saker")
                .ref("testing2", Testing2.class)
                .auto("testing3")
                .build()
        );
        aa.instruction(new AssemblyInstruction.Builder(Testing2.class)
                .val("ett", "testa saker")
                .ref("testing", Testing.class)
                .build()
        );

        Executable assembler = () -> {
            Testing testing = aa.assemble(Testing.class);
        };

        assertThrows(IllegalStateException.class, assembler, "Cyclic dependencies should not be allowed.");
    }

    @Test
    public void inheritanceTest() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        var aa = new Appembler();

        aa.instruction(new AssemblyInstruction.Builder(Testing.class)
                .val("ett", "testa saker")
                .ref("testing2", Testing2.class)
                .auto("testingInterface")
                .build()
        );
        aa.instruction(new AssemblyInstruction.Builder(Testing2.class)
                .val("ett", "testa saker")
                .build()
        );
        aa.instruction(new AssemblyInstruction.Builder(Testing3.class)
                .val("ett", "testa saker")
                .build()
        );

        Testing testing = aa.assemble(Testing.class);

        assertNotNull(testing);
    }

    @Test
    public void noInstructionTest() throws IllegalAccessException, InstantiationException, InvocationTargetException {
        var aa = new Appembler();

        Executable assembler = () -> {
            Testing testing = aa.assemble(Testing.class);
        };

        assertThrows(RuntimeException.class, assembler, "Assembly instructions are required for assembly of a class.");
    }


    public static class Testing {

        public Testing() {
        }

        public Testing(String ett) {
            System.out.println(Testing.class.getName() + ": " + ett);
        }

        public Testing(String ett, Testing2 testing2) {
            System.out.println(Testing.class.getName() + ": " + ett + ", " + testing2.toString());
        }

        public Testing(String ett, Testing2 testing2, Testing3 testing3) {
            System.out.println(Testing.class.getName() + ": " + ett + ", " + testing2.toString() + ", " + testing3.toString());
        }

        public Testing(String ett, Testing2 testing2, TestingInterface testingInterface) {
            System.out.println(Testing.class.getName() + ": " + ett + ", " + testing2.toString() + ", " + testingInterface.toString());
        }
    }

    public static class Testing2 {

        private final String ett;
        private final Integer tva;

        public Testing2(String ett) {
            System.out.println(Testing2.class.getName() + ": " + ett);
            this.ett = ett;
            this.tva = null;
        }

        public Testing2(String ett, Integer tva) {
            System.out.println(Testing2.class.getName() + ": " + ett + tva);
            this.ett = ett;
            this.tva = tva;
        }

        public Testing2(String ett, Testing testing) {
            this.ett = ett;
            this.tva = null;
        }

        @Override
        public String toString() {
            return "Testing2{" +
                    "ett='" + ett + '\'' +
                    ", tva=" + tva +
                    '}';
        }
    }

    public static class Testing3 implements TestingInterface {

        private final String ett;
        private final Integer tva;

        public Testing3(String ett) {
            System.out.println(Testing3.class.getName() + ": " + ett);
            this.ett = ett;
            this.tva = null;
        }

        public Testing3(String ett, Integer tva) {
            System.out.println(Testing3.class.getName() + ": " + ett + tva);
            this.ett = ett;
            this.tva = tva;
        }

        @Override
        public String toString() {
            return "Testing3{" +
                    "ett='" + ett + '\'' +
                    ", tva=" + tva +
                    '}';
        }
    }

    public interface TestingInterface {
    }
}
