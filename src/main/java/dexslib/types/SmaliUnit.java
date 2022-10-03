package dexslib.types;

import dexslib.util.CoreUtils;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel Baier on 28.11.16.
 */
public class SmaliUnit implements Comparable<SmaliUnit> {
    private ClassDef classDefinition;
    private Method coreMethodDefinition, backwardSliceMethodDefinition;
    private List<Method> relevantMethods = new ArrayList<Method>();
    private Map<String,Method> allMethods = new HashMap<String,Method>();
    private int numberOfInst = 0;
    private String classDescriptor;
    private String Method2Analyze;


    public int getNumberOfInst() {
        return numberOfInst;
    }

    public void setNumberOfInst(int numberOfInst) {
        this.numberOfInst = numberOfInst;
    }


    public void setClassDescriptor(String classDescriptor) {
        this.classDescriptor = CoreUtils.removeArraySign(classDescriptor);
    }


    public String getClassDescriptor(){
        return this.classDescriptor;
    }

    public ClassDef getClassDefinition() {

        return classDefinition;
    }

    public void setClassDefinition(ClassDef classDefinition) {
        this.classDefinition = classDefinition;
    }

    public Method getCoreMethodDefinition() {
        return coreMethodDefinition;
    }

    public Method getBackwardSliceMethodDefinition() {
        return backwardSliceMethodDefinition;
    }

    public Method[] getRelevantMethodDefinitions() {
        Method[] methods = relevantMethods.toArray(new Method[relevantMethods.size()]);

        return methods;
    }

    public void setCoreMethodDefinition(Method methodDefinition) {
        this.coreMethodDefinition = methodDefinition;
    }

    public void setBackwardSliceMethodDefinition(Method methodDefinition) {
        this.backwardSliceMethodDefinition = methodDefinition;
    }

    public void addMethodDefinition(Method methodDefinition) {
        this.relevantMethods.add(methodDefinition);
    }

    /*
     this method is used to add all methods of a class, for some special purpose, e.g. if there is no default constructor
     */
    public void addAllMethodDefinition(String methodName, Method methodDefinition) {
        this.allMethods.put(methodName,methodDefinition); // ensures that we have only one method with one name
    }

    public Map<String,Method> getAllMethodDefinitions() {
        return allMethods;
    }


    /**
     *
     * @param method2Analyze this has to be String of method.toString()
     */
    public void setMethod2Analyze(String method2Analyze){
        this.Method2Analyze = method2Analyze;
    }

    public String getMethod2Analyze(){
        return this.Method2Analyze;
    }


    public void removeRelevantMethod(String methodCallRef){
        this.relevantMethods.remove(methodCallRef);
    }


    @Override
    public int hashCode()
    {
        return this.classDefinition.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        SmaliUnit sm = (SmaliUnit) o;
        //return this.classDefinition.equals(((SmaliUnit) o).getClassDefinition());
        return this.classDefinition.equals(sm.getClassDefinition());
    }


    @Override
    public int compareTo(SmaliUnit o) {

        if(o.getNumberOfInst() == this.getNumberOfInst()){
            return this.classDescriptor.compareTo(o.getClassDescriptor());
        }

        return o.getNumberOfInst() - this.getNumberOfInst();
    }
}
