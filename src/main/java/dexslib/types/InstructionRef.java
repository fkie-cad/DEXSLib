package dexslib.types;

import dexslib.util.SpecialisedInstruction;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;

/**
 * An Instruction-Reference (InstructionRef) is used as the object representation of the exact location of an instruction.
 * For instance like in SomeClass.smali on line 478. This instruction has to be in an method, therefore this InstructionRef consist of the following objects
 *
 * ClassDef for the class
 * Method.getImplementation() for the method
 * Instruction the instruction itself in form as as SpecialisedInstruction
 *
 */


public class InstructionRef {
    private SpecialisedInstruction specialisedInstruction;
    private ClassDef classOfInst;
    private MethodImplementation methodImplementation;
    private String fullMethodName; // <
    private int methodParameters;
    private Method method;


    /*public InstructionRef(ClassDef classOfInst,MethodImplementation methodImplementation, String fullMethodName, SpecialisedInstruction specialisedInstruction){
        this.classOfInst = classOfInst;
        this.methodImplementation = methodImplementation;
        this.fullMethodName = fullMethodName;
        this.specialisedInstruction = specialisedInstruction;


    }*/


    public InstructionRef(ClassDef classOfInst, Method method, SpecialisedInstruction specialisedInstruction) {
        this.classOfInst = classOfInst;
        this.methodImplementation = method.getImplementation();
        this.fullMethodName = method.toString();
        this.specialisedInstruction = specialisedInstruction;
        this.methodParameters = method.getParameters().size();
        this.method = method;
    }


    public int getMethodParameterCount(){
        return this.methodParameters;
    }

    public SpecialisedInstruction getSpecialisedInstruction() {
        return specialisedInstruction;
    }

    public void setSpecialisedInstruction(SpecialisedInstruction specialisedInstruction) {
        this.specialisedInstruction = specialisedInstruction;
    }

    public ClassDef getClassOfInst() {
        return classOfInst;
    }

    public void setClassOfInst(ClassDef classOfInst) {
        this.classOfInst = classOfInst;
    }

    public MethodImplementation getMethodImplementation() {
        return methodImplementation;
    }

    public void setMethodImplementation(MethodImplementation methodImplementation) {
        this.methodImplementation = methodImplementation;
    }

    public String getFullMethodName() {
        return fullMethodName;
    }

    public void setFullMethodName(String fullMethodName) {
        this.fullMethodName = fullMethodName;
    }


    @Override
    /**
     * @return the String representation of this object in the form  class>-><methodname>(parameters)ReturnValue|InstructionLine
     */
    public String toString() {
        return this.getFullMethodName()+"|"+this.getSpecialisedInstruction().toString();
    }


    public boolean isInstructionInvokingMethod(){
        return this.getSpecialisedInstruction().isMethodInvocation();
    }

    public String getMethodInvocation(){
        return this.getSpecialisedInstruction().getFullMethodName();
    }

    public Method getMethod(){
        return this.method;
    }
}
