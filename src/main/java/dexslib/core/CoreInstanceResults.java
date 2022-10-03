package dexslib.core;

import dexslib.SmaliLayer;
import dexslib.core.slicing.MethodParameters;
import dexslib.types.*;

import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.instruction.Instruction;

import java.util.*;

/**
 * Created by Daniel Baier on 04.01.17.
 */
public class CoreInstanceResults {

    private SmaliUnit currentCoreClassSmaliUnit;
    private int deobfuscationType = -1;

    private boolean hasOnlyCoreRef = false;

    private LinkedHashSet<String> deobfuscationRoutinesList = new LinkedHashSet<String>();
    private LinkedHashSet<String> classNamesOfRelevantClasses = new LinkedHashSet<String>();

    private Map<String, Field> fieldInvocationsOfCurrentCoreMap = new HashMap<String, Field>();
    private List<CoreClassRef> sortedListDescOfUsageOfRelevantMethods = new ArrayList<CoreClassRef>();



    public Map<String, InitCallRef> getCoreInstanceInitCallsMap() {
        return coreInstanceInitCallsMap;
    }

    public void setCoreInstanceInitCallsMap(Map<String, InitCallRef> coreInstanceInitCallsMap) {
        this.coreInstanceInitCallsMap = coreInstanceInitCallsMap;
    }

    // is used to safe the deobfuscation method with their corresponding obfuscated value
    private Map<InstructionRef, MethodParameters> obfuscatedStringsForDeobMethodMap = new HashMap<InstructionRef,MethodParameters>();


    private Map<String,Iterable<? extends Instruction>> coreInstanceNativeCallsMap = new HashMap<String,Iterable<? extends Instruction>>();

    // in order to remove calls to constructors of init methods, we safe them here
    private Map<String,InitCallRef> coreInstanceInitCallsMap = new HashMap<String,InitCallRef>();;

    // holds the native calls of the clinit of the current CoreInstance
    private List<String> nativeMethodNamesOfCurrentCoreInstance = new ArrayList<String>();

    // holds the references method calls of the current clinit of our current CoreInstance
    private Map<String, CoreClassRef> relevantMethodReferencesOfCoreMap = new HashMap<String, CoreClassRef>();
    private Map<String, ClassRef> relevantMethodClassesReferences = new HashMap<String, ClassRef>();
    private LinkedHashSet<ClassDef> relevantClassDefsSet = new LinkedHashSet<ClassDef>();

    public CoreInstanceResults(SmaliUnit currentCoreClassSmaliUnit, int deobfuscationType, LinkedHashSet<String> deobfuscationRoutinesList, LinkedHashSet<String> classNamesOfRelevantClasses, Map<String, Field> fieldInvocationsOfCurrentCoreMap, List<CoreClassRef> sortedListDescOfUsageOfRelevantMethods, Map<String, CoreClassRef> relevantMethodReferencesOfCoreMap, Map<String, ClassRef> relevantMethodClassesReferences, LinkedHashSet<ClassDef> relevantClassDefsSet,Map<String,Iterable<? extends Instruction>> coreInstanceNativeCallsMap, Map<String,InitCallRef> passedCoreInstanceInitCallsMap) {
        this.setCurrentCoreClassSmaliUnit(currentCoreClassSmaliUnit);
        this.setDeobfuscationType(deobfuscationType);
        this.setDeobfuscationRoutinesList(deobfuscationRoutinesList);
        this.setClassNamesOfRelevantClasses(classNamesOfRelevantClasses);
        this.setFieldInvocationsOfCurrentCoreMap(fieldInvocationsOfCurrentCoreMap); // istOfDeobfuscationFields
        this.setSortedListDescOfUsageOfRelevantMethods(sortedListDescOfUsageOfRelevantMethods);
        this.setRelevantMethodReferencesOfCoreMap(relevantMethodReferencesOfCoreMap);
        this.setRelevantMethodClassesReferences(relevantMethodClassesReferences);
        this.setRelevantClassDefsSet(relevantClassDefsSet);



        if(relevantClassDefsSet.contains(null)){
           // System.out.println("sowas darf nicht sein"); test case....
        }
        this.setCoreInstanceNativeCallsMap(coreInstanceNativeCallsMap);
        this.setInitCallRef(passedCoreInstanceInitCallsMap);
        //passedCoreInstanceInitCallsMap
    }


    /**
     *  The constructor when we have no clinit method as core class
     * @param deobfuscationType
     * @param obfuscatedStringsForDeobMethodMap
     */
    public CoreInstanceResults(int deobfuscationType, Map<InstructionRef, MethodParameters> obfuscatedStringsForDeobMethodMap){
        this.setDeobfuscationType(SmaliLayer.DEOBFUSCATION_TYPE_METHOD_BS); // we only setting this, if we are using this kind of deob method
        this.obfuscatedStringsForDeobMethodMap = obfuscatedStringsForDeobMethodMap;
    }


    public Map<InstructionRef, MethodParameters> getObfuscatedStringsForDeobMethodMap() {
        return obfuscatedStringsForDeobMethodMap;
    }

    public void setObfuscatedStringsForDeobMethodMap(Map<InstructionRef, MethodParameters> obfuscatedStringsForDeobMethodMap) {
        this.setDeobfuscationType(SmaliLayer.DEOBFUSCATION_TYPE_METHOD_BS); // we only setting this, if we are using this kind of deob method
        this.obfuscatedStringsForDeobMethodMap = obfuscatedStringsForDeobMethodMap;
    }




    public LinkedHashSet<String> getDeobfuscationRoutinesList() {
        return deobfuscationRoutinesList;
    }

    public void setDeobfuscationRoutinesList(LinkedHashSet<String> deobfuscationRoutinesList) {
        if(deobfuscationRoutinesList != null ){
            if(!deobfuscationRoutinesList.isEmpty()){
                this.deobfuscationRoutinesList = deobfuscationRoutinesList;
            }

        }
    }

    public LinkedHashSet<String> getClassNamesOfRelevantClasses() {
        return classNamesOfRelevantClasses;
    }

    public void setClassNamesOfRelevantClasses(LinkedHashSet<String> classNamesOfRelevantClasses) {
        if(classNamesOfRelevantClasses != null){
            if(!classNamesOfRelevantClasses.isEmpty()){
                this.classNamesOfRelevantClasses = classNamesOfRelevantClasses;
            }
        }

    }

    public SmaliUnit getCurrentCoreClassSmaliUnit() {
        return currentCoreClassSmaliUnit;
    }

    public void setCurrentCoreClassSmaliUnit(SmaliUnit currentCoreClassSmaliUnit) {
        this.currentCoreClassSmaliUnit = currentCoreClassSmaliUnit;
    }

    public Map<String, Field> getFieldInvocationsOfCurrentCoreMap() {
        return fieldInvocationsOfCurrentCoreMap;
    }

    public void setFieldInvocationsOfCurrentCoreMap(Map<String, Field> fieldInvocationsOfCurrentCoreMap) {
        if(fieldInvocationsOfCurrentCoreMap != null){
            if(!fieldInvocationsOfCurrentCoreMap.isEmpty()){
                this.fieldInvocationsOfCurrentCoreMap = fieldInvocationsOfCurrentCoreMap;
            }
        }

    }

    public List<CoreClassRef> getSortedListDescOfUsageOfRelevantMethods() {
        return sortedListDescOfUsageOfRelevantMethods;
    }

    public void setSortedListDescOfUsageOfRelevantMethods(List<CoreClassRef> sortedListDescOfUsageOfRelevantMethods) {
        if(sortedListDescOfUsageOfRelevantMethods != null){
            if(!sortedListDescOfUsageOfRelevantMethods.isEmpty()){
                this.sortedListDescOfUsageOfRelevantMethods = sortedListDescOfUsageOfRelevantMethods;
            }

        }


    }

    public int getDeobfuscationType() {
        return deobfuscationType;
    }

    public void setDeobfuscationType(int deobfuscationType) {
        this.deobfuscationType = deobfuscationType;
    }

    public Map<String, CoreClassRef> getRelevantMethodReferencesOfCoreMap() {
        return relevantMethodReferencesOfCoreMap;
    }

    public void setRelevantMethodReferencesOfCoreMap(Map<String, CoreClassRef> relevantMethodReferencesOfCoreMap) {
        if(relevantMethodReferencesOfCoreMap != null){
            if(!relevantMethodReferencesOfCoreMap.isEmpty()){
                this.relevantMethodReferencesOfCoreMap = relevantMethodReferencesOfCoreMap;
            }
        }

    }

    public Map<String, ClassRef> getRelevantMethodClassesReferences() {
        return relevantMethodClassesReferences;
    }

    public void setRelevantMethodClassesReferences(Map<String, ClassRef> relevantMethodClassesReferences) {
        if(relevantMethodClassesReferences != null){
            if(!relevantMethodClassesReferences.isEmpty()){
                this.relevantMethodClassesReferences = relevantMethodClassesReferences;
            }
        }

    }

    public LinkedHashSet<ClassDef> getRelevantClassDefsSet() {
        return relevantClassDefsSet;
    }

    public void setRelevantClassDefsSet(LinkedHashSet<ClassDef> relevantClassDefsSet) {
        if(relevantClassDefsSet != null){
            if(!relevantClassDefsSet.isEmpty()){
                this.relevantClassDefsSet = relevantClassDefsSet;
            }
        }

    }


    public boolean isHasOnlyCoreRef() {
        return hasOnlyCoreRef;
    }

    public void setHasOnlyCoreRef(boolean hasOnlyCoreRef) {
        this.hasOnlyCoreRef = hasOnlyCoreRef;
    }


    public Map<String,Iterable<? extends Instruction>> getCoreInstanceNativeCallsMap() {
        return coreInstanceNativeCallsMap;
    }

    public void setCoreInstanceNativeCallsMap(Map<String,Iterable<? extends Instruction>> coreInstanceNativeCallsMap) {
        this.coreInstanceNativeCallsMap = coreInstanceNativeCallsMap;
    }

    public boolean hasNativeCall(){
        return this.coreInstanceNativeCallsMap.isEmpty() == false;
    }

    public List<String> getNativeMethodNamesOfCurrentCoreInstance() {
        return nativeMethodNamesOfCurrentCoreInstance;
    }

    public void setNativeMethodNamesOfCurrentCoreInstance(List<String> nativeMethodNamesOfCurrentCoreInstance) {
        if(nativeMethodNamesOfCurrentCoreInstance != null){
            if(!nativeMethodNamesOfCurrentCoreInstance.isEmpty()){
                this.nativeMethodNamesOfCurrentCoreInstance = nativeMethodNamesOfCurrentCoreInstance;
            }
        }

    }

    public Map<String,InitCallRef>  getInitCallRef() {
        return this.coreInstanceInitCallsMap;
    }

    public void setInitCallRef(Map<String,InitCallRef>  initCallRefMap) {
        if (initCallRefMap != null) {
            if (!initCallRefMap.isEmpty()) {
                this.coreInstanceInitCallsMap = initCallRefMap;
            }
        }
    }

    public boolean hasInitCalls(){
        return this.coreInstanceInitCallsMap.isEmpty() == false;
    }


    }
