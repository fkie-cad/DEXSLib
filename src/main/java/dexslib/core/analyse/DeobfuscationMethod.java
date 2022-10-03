package dexslib.core.analyse;

import org.jf.dexlib2.iface.Method;

import java.util.ArrayList;

public class DeobfuscationMethod  implements Comparable<DeobfuscationMethod>{


    private String fullMethodName;
    private Method deobMethod;
    private ArrayList<String> obfuscatedStrings = new ArrayList<String>();
    private int numOfInvocations;




    public DeobfuscationMethod(String fullMethodName, Method deobMethod){
        this.fullMethodName = fullMethodName;
        this.deobMethod = deobMethod;
        this.numOfInvocations = 1;

    }

    public DeobfuscationMethod(String fullMethodName, int numOfInvocations){
        this.fullMethodName = fullMethodName;
        this.deobMethod = null;
        this.numOfInvocations = numOfInvocations;

    }

    public void increaseNumOfInvocations(){
        this.numOfInvocations = this.numOfInvocations + 1;
    }

    public int getNumOfInvocations(){
        return this.numOfInvocations;
    }


    public String getFullMethodName() {
        return fullMethodName;
    }

    public void setFullMethodName(String fullMethodName) {
        this.fullMethodName = fullMethodName;
    }

    public Method getDeobMethod() {
        return deobMethod;
    }

    public void setDeobMethod(Method deobMethod) {
        this.deobMethod = deobMethod;
    }

    public ArrayList<String> getObfuscatedStrings() {
        return obfuscatedStrings;
    }

    public void setObfuscatedStrings(ArrayList<String> obfuscatedStrings) {
        this.obfuscatedStrings = obfuscatedStrings;
    }

    public void addObfuscatedString2DeobMethod(String obfuscatedString){
        this.obfuscatedStrings.add(obfuscatedString);
    }

    /*
    public void setNumOfInvocations(int numOfInvocations){
        this.numOfInvocations = this.numOfInvocations
    }*/


    @Override
    public int hashCode()
    {
        return this.fullMethodName.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        DeobfuscationMethod dm = (DeobfuscationMethod) o;

        return this.fullMethodName.equals(dm.getFullMethodName());
    }


    @Override
    public int compareTo(DeobfuscationMethod o) {

        if(o.getNumOfInvocations() == this.getNumOfInvocations()){
            return this.fullMethodName.compareTo(o.getFullMethodName());
        }

        return o.getNumOfInvocations() - this.getNumOfInvocations();
    }

}
