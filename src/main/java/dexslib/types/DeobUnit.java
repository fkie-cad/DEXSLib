package dexslib.types;


import org.jf.dexlib2.iface.Method;

public class DeobUnit implements Comparable<DeobUnit> {



    private Method backwardSliceMethodDefinition;

    private String methodDescriptor;

    private String fullMethodName;



    private int encryptionTrashold =0;

    public void setMethodDescriptor(String methodDescriptor) {
        this.methodDescriptor = methodDescriptor;
    }


    public String getMethodDescriptor(){
        return this.methodDescriptor;
    }



    private int numberOfInvocations = 0;  // the number this deobfuscation routine was called


    public DeobUnit(Method method){
        this.methodDescriptor = method.toString();
        this.backwardSliceMethodDefinition = method;
        this.numberOfInvocations = 1;
        this.fullMethodName = method.toString();
    }


    public DeobUnit(Method method, int encryptionTrashold){
        this.methodDescriptor = method.toString();
        this.backwardSliceMethodDefinition = method;
        this.numberOfInvocations = 0;
        this.encryptionTrashold = encryptionTrashold;
        this.fullMethodName = method.toString();
    }


    public void setMethodDefinition(Method backwardSliceMethodDefinition){
        this.backwardSliceMethodDefinition = backwardSliceMethodDefinition;
    }

    public Method getMethodDefinition(){
        return this.backwardSliceMethodDefinition;
    }


    public int getNumberOfInvocations() {
        return numberOfInvocations;
    }

    public void setNumberOfInvocations(int numberOfInvocations) {
        this.numberOfInvocations = numberOfInvocations;
    }

    public void increaseNumOfInvocations() {
        this.numberOfInvocations++;
    }

    public int getEncryptionTrashold() {
        return encryptionTrashold;
    }

    public void setEncryptionTrashold(int encryptionTrashold) {
        this.encryptionTrashold = encryptionTrashold;
    }

    public String getFullMethodName(){
        return this.fullMethodName;
    }

    /*
    @Override
    public int hashCode()
    {
        return this.backwardSliceMethodDefinition.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        DeobUnit deobUnit = (DeobUnit) o;
        //return this.classDefinition.equals(((SmaliUnit) o).getClassDefinition());
        return this.backwardSliceMethodDefinition.equals(deobUnit.getMethodDefinition());
    }*/


    @Override
    public int compareTo(DeobUnit o) {

        /*if(o.getNumberOfInvocations() == this.getNumberOfInvocations()){
            return this.methodDescriptor.compareTo(o.getMethodDescriptor());
        }*/

        return o.getNumberOfInvocations() - this.getNumberOfInvocations();
    }
}
