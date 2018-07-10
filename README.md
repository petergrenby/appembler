# Project Appembler
The name **Appembler** is a word game of Application Assembler.
**Appembler** is an opinionated *Inversion of Control* or *Dependency Injector* "framework".
That said - it not really a framework, it is more like a set of helper classes to assemble your application.

## Opinionated
The idea with **Appembler** is to NOT be yet another framework that tries to solves all need, and becomes both big and complex. Instead it is based on the idea to opinionated, very opinionated, so that it can become simple and easy to use. So there are few things which I have taken a stand on when building it:

* All component dependecies should injected at time of component creation. That is - only constructor injection is supported.
 * Simpler assembling of component which means that the framework is also simpler.
 * Cyclic dependecies are not possible by design (very nice property).
 * Components, from framework point of view, can always be immutable (also very nice).
 * No side effect created but not initialized components.
* Components should not know about the framework.
 * There are no annotations that needs to be put into the component classes.
* All assemling instructions are in the code.
 * There are no XML files or anything else for setting up how the application will be assembled. 
* Assembling is done on property names and property types.
 * This makes less error prone to set up instruction on how to construct components for assembly. Constructor parameters are refered to by their name and not just their position in argument list.
 * This also means that it could never ever support less than Java 8 as that is when parameter names was introduced.

## Getting started
**Appembler** is built around the application assembler that one needs to create to get started. This simply done by create an instance of with *new*:

```
var appembler = new Appembler();
```

The next step is to tell **Appembler** on what components it should know about. That is - who they are, how they are constructed and what they need to be assembled.

```
appembler.instruction(new AssemblyInstruction.Builder(MyClass.class)
                .val("myargument", "a value of some kind")
                .build()
        );
```

This will tell **Appembler** that it can create *MyClass* with a constructor that takes one argument. The argument is names *myargument* and takes a *String*.

To construct and assemble this very simple application one would call assembly method and tell it with is *root* class to be assembled.

```
MyClass myObject = appembler.assemble(MyClass.class);
```     
A complete code example on how to do this are among the test in the **Appembler** code base.

## Getting your hand dirty
You would not get far with the example in "Getting started" section. There are more things that are needed for this to be useful. So lets look at some more assembly instructions. There three types of instructions:

1. val - Value, that should be passed into a constructor.
2. ref - Reference, that is a reference to another component that also needs to be constructed and assembled before it can be injected into the constructor.
3. auto - Auto reference, that allows **Appembler** to find the that should be constructed and assembled before it can be injected into the constructor.

Here is an example that uses them all:

```
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
```

The last thing that could be useful to know is that **Appembler** be default creates *singletons*. However, this can be override on each assembly instruction be setting *scope* to:

* SINGLETON - One object will be create for that **Appembler** instance.
* THREAD_LOCAL - One object will be create the thread that assembles it, for that **Appembler** instance.
* PROTOTYPE - One object will be created everytime the component is assembled.

One can also override the default scope, or construction scope, of **Appembler** instance like this.

```
var appembler = new Appembler(ConstructionScope.THREAD_LOCAL);
```