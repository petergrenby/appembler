package se.grenby.appembler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import se.grenby.appembler.exception.CyclicDependencyException;
import se.grenby.appembler.exception.NoAssemblyInstructionException;
import se.grenby.appembler.exception.NoMatchingConstructorException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AppemblerTest {

    @Test
    public void simpleTest()  {
        var aa = new Appembler();

        aa.instruction(new AssemblyInstruction.Builder(TopTestingClass.class)
                .val("ett", "testa saker")
                .build()
        );

        TopTestingClass topTestingClass = aa.assemble(TopTestingClass.class);

        assertNotNull(topTestingClass);
    }

    @Test
    public void treeTest()  {
	    var aa = new Appembler();

	    aa.instruction(new AssemblyInstruction.Builder(TopTestingClass.class)
                .val("ett", "testa saker")
                .ref("subTestingClass", SubTestingClass.class)
                .auto("subTestingClassWithInterface")
                .build()
        );
        aa.instruction(new AssemblyInstruction.Builder(SubTestingClass.class)
                .val("ett", "testa saker")
                .build()
        );
        aa.instruction(new AssemblyInstruction.Builder(SubTestingClassWithInterface.class)
                .val("ett", "testa saker")
                .build()
        );

	    TopTestingClass topTestingClass = aa.assemble(TopTestingClass.class);

	    assertNotNull(topTestingClass);
    }

    @Test
    public void incorrectParameterNameTest() {
        var aa = new Appembler();

        Executable addinstruction = () -> {
            aa.instruction(new AssemblyInstruction.Builder(TopTestingClass.class)
                    .val("wrong", "not going to work")
                    .build()
            );
        };

        assertThrows(NoMatchingConstructorException.class, addinstruction, "If parameter names are supplied they are required to be exist in a constructor at correct position.");
    }

    @Test
    public void incorrectValueTypeTest() {
        var aa = new Appembler();

        Executable addinstruction = () -> {
            aa.instruction(new AssemblyInstruction.Builder(TopTestingClass.class)
                    .val("ett", 42)
                    .build()
            );
        };

        assertThrows(NoMatchingConstructorException.class, addinstruction, "Parameter types are required to be the same for values.");
    }

    @Test
    public void incorrectReferenceTypeTest() {
        var aa = new Appembler();

        Executable addinstruction = () -> {
            aa.instruction(new AssemblyInstruction.Builder(TopTestingClass.class)
                    .val("ett", "testa saker")
                    .ref("subTestingClassWithInterface", SubTestingClassWithInterface.class)
                    .build()
            );
        };

        assertThrows(NoMatchingConstructorException.class, addinstruction, "Parameter types are required to be the same for values.");
    }


    @Test
    public void cyclicTest() {
        var aa = new Appembler();

        aa.instruction(new AssemblyInstruction.Builder(TopTestingClass.class)
                .val("ett", "testa saker")
                .ref("subTestingClass", SubTestingClass.class)
                .auto("subTestingClassWithInterface")
                .build()
        );
        aa.instruction(new AssemblyInstruction.Builder(SubTestingClass.class)
                .val("ett", "testa saker")
                .ref("topTestingClass", TopTestingClass.class)
                .build()
        );

        Executable assembler = () -> {
            TopTestingClass topTestingClass = aa.assemble(TopTestingClass.class);
        };

        assertThrows(CyclicDependencyException.class, assembler, "Cyclic dependencies should not be allowed.");
    }

    @Test
    public void inheritanceTest() {
        var aa = new Appembler();

        aa.instruction(new AssemblyInstruction.Builder(TopTestingClass.class)
                .val("ett", "testa saker")
                .ref("subTestingClass", SubTestingClass.class)
                .auto("testingInterface")
                .build()
        );
        aa.instruction(new AssemblyInstruction.Builder(SubTestingClass.class)
                .val("ett", "testa saker")
                .build()
        );
        aa.instruction(new AssemblyInstruction.Builder(SubTestingClassWithInterface.class)
                .val("ett", "testa saker")
                .build()
        );

        TopTestingClass topTestingClass = aa.assemble(TopTestingClass.class);

        assertNotNull(topTestingClass);
    }

    @Test
    public void noInstructionTest() {
        var aa = new Appembler();

        Executable assembler = () -> {
            TopTestingClass topTestingClass = aa.assemble(TopTestingClass.class);
        };

        assertThrows(NoAssemblyInstructionException.class, assembler, "Assembly instructions are required for assembly of a class.");
    }


    public static class TopTestingClass {

        public TopTestingClass() {
        }

        public TopTestingClass(String ett) {
            System.out.println(TopTestingClass.class.getName() + ": " + ett);
        }

        public TopTestingClass(String ett, SubTestingClass subTestingClass) {
            System.out.println(TopTestingClass.class.getName() + ": " + ett + ", " + subTestingClass.toString());
        }

        public TopTestingClass(String ett, SubTestingClass subTestingClass, SubTestingClassWithInterface subTestingClassWithInterface) {
            System.out.println(TopTestingClass.class.getName() + ": " + ett + ", " + subTestingClass.toString() + ", " + subTestingClassWithInterface.toString());
        }

        public TopTestingClass(String ett, SubTestingClass subTestingClass, TestingInterface testingInterface) {
            System.out.println(TopTestingClass.class.getName() + ": " + ett + ", " + subTestingClass.toString() + ", " + testingInterface.toString());
        }
    }

    public static class SubTestingClass {

        private final String ett;
        private final Integer tva;

        public SubTestingClass(String ett) {
            System.out.println(SubTestingClass.class.getName() + ": " + ett);
            this.ett = ett;
            this.tva = null;
        }

        public SubTestingClass(String ett, Integer tva) {
            System.out.println(SubTestingClass.class.getName() + ": " + ett + tva);
            this.ett = ett;
            this.tva = tva;
        }

        public SubTestingClass(String ett, TopTestingClass topTestingClass) {
            this.ett = ett;
            this.tva = null;
        }

        @Override
        public String toString() {
            return "SubTestingClass{" +
                    "ett='" + ett + '\'' +
                    ", tva=" + tva +
                    '}';
        }
    }

    public static class SubTestingClassWithInterface implements TestingInterface {

        private final String ett;
        private final Integer tva;

        public SubTestingClassWithInterface(String ett) {
            System.out.println(SubTestingClassWithInterface.class.getName() + ": " + ett);
            this.ett = ett;
            this.tva = null;
        }

        public SubTestingClassWithInterface(String ett, Integer tva) {
            System.out.println(SubTestingClassWithInterface.class.getName() + ": " + ett + tva);
            this.ett = ett;
            this.tva = tva;
        }

        @Override
        public String toString() {
            return "SubTestingClassWithInterface{" +
                    "ett='" + ett + '\'' +
                    ", tva=" + tva +
                    '}';
        }
    }

    public interface TestingInterface {
    }
}
