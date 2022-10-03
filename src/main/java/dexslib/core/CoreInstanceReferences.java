package dexslib.core;

import dexslib.ClassInitializerFilter;
import dexslib.SmaliLayer;
import dexslib.types.ClassRef;
import dexslib.types.CoreClassRef;
import dexslib.types.InitCallRef;
import dexslib.types.SmaliUnit;
import dexslib.util.CoreUtils;
import dexslib.util.SmaliLayerUtils;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.ReferenceType;
import org.jf.dexlib2.builder.instruction.BuilderInstruction10x;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;

import java.util.*;

/**
 * Created by Daniel Baier on 03.01.17.
 */
public class CoreInstanceReferences {

    /*
      class variables
     */

    private List<String> relevantFieldReferencesOfCoreList = new ArrayList<String>(); // only contains those which are writing/putting something
    private List<String> relevantFieldReferencesOfCoreListReading = new ArrayList<String>();


    private boolean isStaticBackwardSlice = false;

    private LinkedHashSet<String> relevantReferenceOfCoreSet = new LinkedHashSet<String>();

    private Map<String, ClassRef> relevantMethodClassesReferences = new HashMap<String, ClassRef>();
    private LinkedHashSet<String> relevantClassesSet = new LinkedHashSet<String>();
    private LinkedHashSet<ClassDef> relevantClassDefsSet = new LinkedHashSet<ClassDef>();

    // holds the references method calls of the current clinit of our current CoreInstance
    private Map<String,CoreClassRef> relevantMethodReferencesOfCoreMap = new HashMap<String,CoreClassRef>();

    // holds the native calls of the clinit of the current CoreInstance
    private List<String> nativeMethodNamesOfCurrentCoreInstance = new ArrayList<String>();

    // this hashmap contains all clinit instructions which have some native calls, for now this native calls will be removed (CoreClassOriginalName)
    private Map<String,Iterable<? extends Instruction>> coreInstanceNativeCallsMap = new HashMap<String,Iterable<? extends Instruction>>();

    private Map<String,InitCallRef> coreInstanceInitCallsMap = new HashMap<String,InitCallRef>();

    private SmaliUnit currentCoreClassIntance;



    public CoreInstanceReferences(SmaliUnit currentCoreClassIntance, boolean doBackwardSliceOfMethod){

        for(Method method: currentCoreClassIntance.getClassDefinition().getMethods()){
            if(AccessFlags.formatAccessFlagsForMethod(method.getAccessFlags()).contains("native")){
                this.nativeMethodNamesOfCurrentCoreInstance.add(SmaliLayerUtils.getCallingMethodName(method.toString()));
            }

        }

        this.isStaticBackwardSlice =  doBackwardSliceOfMethod;
        createCurrentCoreClassRefs(currentCoreClassIntance,doBackwardSliceOfMethod);
    }





    protected void createReferenceStructures(String classDescriptor){
        this.getRelevantMethodsAndClasses();
        getRelevantClassesOfCurrentCore(classDescriptor);


        if(relevantMethodClassesReferences.isEmpty()){
            this.getRelevantMethodsAndClasses();
        }

        if(relevantClassesSet.isEmpty()){
            this.getRelevantClassesOfCurrentCore(classDescriptor);
        }
    }


    private Map<String, List<String>> getRelevantMethodsListOfClass(Map<String,ClassRef> relevantMethodReferences){
        Map<String, List<String>> tmpMap = new HashMap<String, List<String>>();
        try {

            if (relevantMethodReferences == null) {

                for (Map.Entry<String, CoreClassRef> e : relevantMethodReferencesOfCoreMap.entrySet()) {

                    /*System.out.println("Phase 1: "+e.getValue());
                    System.out.println("Phase 2: "+e.getValue().getCoreClass());
                    System.out.println("Phase 3: "+e.getValue().getCoreClass().getClassDefinition());
                    System.out.println("Phase 4: "+e.getValue().getCoreClass().getClassDefinition().getType());
                    System.out.println("Phase 5: "+e.getKey());*/

                    // here no class is allowed to be add when it is from core itself, because we would than endup into an endless loop
                    if (!e.getValue().getCoreClass().getClassDefinition().getType().equals(SmaliLayerUtils.getCallingClassName(CoreUtils.removeArraySign(e.getKey())))) {
                        List<String> l = tmpMap.get(SmaliLayerUtils.getCallingClassName(e.getKey()));
                        if (l == null)
                            tmpMap.put(SmaliLayerUtils.getCallingClassName(e.getKey()), l = new ArrayList<String>());
                        l.add(SmaliLayerUtils.getCallingMethodName(e.getKey()));
                    }
                }

            } else {


                for (Map.Entry<String, ClassRef> e : relevantMethodReferences.entrySet()) {
                    List<String> l = tmpMap.get(SmaliLayerUtils.getCallingClassName(e.getKey()));
                    if (l == null)
                        tmpMap.put(SmaliLayerUtils.getCallingClassName(e.getKey()), l = new ArrayList<String>());
                    l.add(SmaliLayerUtils.getCallingMethodName(e.getKey()));
                }
            }


            return tmpMap;
        }catch (NullPointerException e){
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.err.println(e.getCause().toString());
            System.exit(0);
            return null;
        }

    }



    private Map<String,ClassRef> getImplementedInDex(Map<String,ClassRef> relevantReferencesOfClassMap){
        String[] callingRefArray = (String[]) relevantReferencesOfClassMap.keySet().toArray(new String[relevantReferencesOfClassMap.size()]);
        for(String tmpCallingRef : callingRefArray){
            //System.out.println("140: tmpCallingRef"+tmpCallingRef);
            if(CoreUtils.isImplemented(tmpCallingRef)){
                continue;
            }else{
                relevantReferencesOfClassMap.remove(tmpCallingRef);
            }
        }

        return relevantReferencesOfClassMap;

    }



    private boolean addSmaliUnitRef(String callingRef,Opcode instructionOpcode,boolean isAbstract, boolean isSuper){
        String methodCallRef,className = "";

        if(instructionOpcode == null){
            // this means we have a try/catch statement
            // because we can't get the instruction of this statement directly we just insert a NOP
            BuilderInstruction10x nop = new BuilderInstruction10x(Opcode.NOP);
            instructionOpcode = nop.getOpcode();

            methodCallRef = callingRef+"-><init>()V";
            className = callingRef;
        }else{
            // this is done if we have a static instance, which has to be add
            methodCallRef = SmaliLayerUtils.getClassInstanceName(callingRef)+"-><init>()V";
            className = CoreUtils.removeArraySign(SmaliLayerUtils.getClassInstanceName(callingRef));
        }

        SmaliUnit tmpSmaliUnit = this.getSmaliUnit(className, Arrays.asList(new String[] {methodCallRef}),isSuper);
        if(tmpSmaliUnit.getRelevantMethodDefinitions().length < 1){
            System.out.println("Zeile 172");
            if(tmpSmaliUnit.getAllMethodDefinitions().keySet().contains("<init>")){
                Method tmpMethod = tmpSmaliUnit.getAllMethodDefinitions().get("<init>");
                System.out.println("Zeile 175");
                tmpSmaliUnit.addMethodDefinition(tmpMethod);
                methodCallRef = tmpMethod.toString();
            }else{
                for(Method tmpMethod : tmpSmaliUnit.getAllMethodDefinitions().values()){
                    if(AccessFlags.formatAccessFlagsForMethod(tmpMethod.getAccessFlags()).contains("abstract")){
                        isAbstract = true;
                        tmpSmaliUnit.addMethodDefinition(tmpMethod);
                        methodCallRef = tmpMethod.toString();
                        CoreClassRef coreClassRef = new CoreClassRef(methodCallRef,instructionOpcode,tmpSmaliUnit);
                        //if(isSuper == false)
                            relevantMethodReferencesOfCoreMap.put(methodCallRef,coreClassRef);
                    }
                }
            }

        }
        if(isAbstract == false){
            CoreClassRef coreClassRef = new CoreClassRef(methodCallRef,instructionOpcode,tmpSmaliUnit);
            //if(isSuper == false)
                relevantMethodReferencesOfCoreMap.put(methodCallRef,coreClassRef);
        }

        relevantClassDefsSet.add(tmpSmaliUnit.getClassDefinition());
        //continue;

        return isAbstract;
    }


    /**
     *
     * @param classSmaliUnit this SmaliUnit has to ensure, that we have already a list of relevant MethodDefinitions
     * @return
     */

    public  Map<String,ClassRef> startBackwardSliceOfMethod(SmaliUnit classSmaliUnit,boolean initialRun){
        Map<String,ClassRef> relevantReferencesOfClassMap = new HashMap<String,ClassRef>();
        boolean isCore = false;


        if(this.isStaticBackwardSlice && initialRun){
            System.out.println("Name of Method: "+classSmaliUnit.getBackwardSliceMethodDefinition().getName());
            //isCore = true;
            return getClassRefsOfMethod(classSmaliUnit,classSmaliUnit.getBackwardSliceMethodDefinition().getImplementation().getInstructions(),isCore,false,classSmaliUnit.getBackwardSliceMethodDefinition().getImplementation());
            // return getClassRefsOfMethod(classSmaliUnit,classSmaliUnit.getCoreMethodDefinition().getImplementation().getInstructions(),isCore,false);
        }else {

        /*
        return getClassRefsOfMethod(classSmaliUnit,classSmaliUnit.getCoreMethodDefinition().getImplementation().getInstructions(),isCore,false,classSmaliUnit.getCoreMethodDefinition().getImplementation());



         */

            for (Method tmpRelevantMethod : classSmaliUnit.getRelevantMethodDefinitions()) {
                MethodImplementation tmpMethodImplementation = tmpRelevantMethod.getImplementation();
                if (tmpMethodImplementation == null) {
                    System.out.println("couldn't solve reference of " + tmpRelevantMethod.toString());
                    continue;
                }
                Map<String, ClassRef> refClassOfMethod = getClassRefsOfMethod(classSmaliUnit, tmpMethodImplementation.getInstructions(), isCore, false, tmpMethodImplementation);
                // Map<String,ClassRef> refClassOfMethod = getClassRefsOfMethod(classSmaliUnit,tmpMethodImplementation.getInstructions(),isCore,false);

                relevantReferencesOfClassMap.putAll(refClassOfMethod);
            }

        }


        return relevantReferencesOfClassMap;

    }


    private Map<String,ClassRef>  getClassRefsOfMethod(SmaliUnit classSmaliUnit, Iterable<? extends Instruction> instructions, boolean isCoreClass, boolean update,MethodImplementation tmpMethodImpl) {
    //private Map<String,ClassRef>  getClassRefsOfMethod(SmaliUnit classSmaliUnit, Iterable<? extends Instruction> instructions, boolean isCoreClass, boolean update) {

        List<String> toAnalyzeUsageOfCoreFields = new ArrayList<String>();
        List<String> toAnalyzedExceptions = new ArrayList<String>();
        if (isCoreClass) {


            // maybe we have an instance of an object which is used inside the core, therefore we have to add it as a relevant class
            for (Field tmpField : classSmaliUnit.getClassDefinition().getStaticFields()) {

                if(tmpField.toString().contains("Ls/s/i;")){
                    System.out.println("phase 1");
                }

                if (CoreUtils.isFieldTypeImplemented(tmpField.toString())) {
                    toAnalyzeUsageOfCoreFields.add(tmpField.toString());
                    if(tmpField.toString().contains("Ls/s/i;")){
                        System.out.println("phase 2");
                    }
                }
            }

            /*
             for now this feature is comment out, because this need further investigation to handle this with a working template
             but this is out of scope current this thesis

            long size = classSmaliUnit.getClassDefinition().getStaticFields().spliterator().getExactSizeIfKnown();
            //  this means there is no static field, but maybe there is some of the super class
            if(size < 1){
                if (CoreUtils.isImplemented(classSmaliUnit.getClassDefinition().getSuperclass())){
                    addSmaliUnitRef(classSmaliUnit.getClassDefinition().getSuperclass(), null, false,true);
                    //toAnalyzeUsageOfCoreFields.add(classSmaliUnit.getClassDefinition().getSuperclass());
                }
            }*/
        }


        // we also have to add all the self defined exceptions which are used inside a method
            for (TryBlock tmpTryBlock : tmpMethodImpl.getTryBlocks()) {

                for (Object exceptionSkeleton : tmpTryBlock.getExceptionHandlers()) {
                    ExceptionHandler tmpExceptionHandler = (ExceptionHandler) exceptionSkeleton;
                    if (CoreUtils.isImplemented(tmpExceptionHandler.getExceptionType())) {
                        //toAnalyzedExceptions.add(tmpExceptionHandler.getExceptionType());
                        addSmaliUnitRef(tmpExceptionHandler.getExceptionType(), null, false,false);
                    }

                }
            }




        Map<String,ClassRef> relevantReferencesOfClassMap = new HashMap<String,ClassRef>();
        InitCallRef initCallRef2Remove = new InitCallRef(instructions,null,null);


        for (Instruction instruction : instructions) {
            boolean hasDefaultConstructor = true;
            boolean isAbstract = false;
            Opcode instructionOpcode = instruction.getOpcode();

            if(instructionOpcode.setsResult() || instructionOpcode.referenceType == ReferenceType.METHOD || instructionOpcode.referenceType == ReferenceType.FIELD || instructionOpcode.equals(Opcode.NEW_INSTANCE) || instructionOpcode.referenceType == ReferenceType.TYPE ){


                String callingRef = ((ReferenceInstruction) instruction).getReference().toString();
                //System.out.println("321:Finder : "+callingRef);

                if(CoreUtils.isBasicType(callingRef)){
                    continue;
                }
                String callingClass = CoreUtils.removeArraySign(SmaliLayerUtils.getCallingClassName(callingRef));

                ClassRef someClassRef = null;

                if(!CoreUtils.isImplemented(callingRef)){
                    if(instructionOpcode.referenceType == ReferenceType.FIELD){
                        if(!CoreUtils.isFieldTypeImplemented(callingRef)){
                            continue;
                        }
                    }else{
                        continue;
                    }




                }


                if(isCoreClass || this.isStaticBackwardSlice){

                    if(CoreUtils.isNativeCall(callingRef,this.nativeMethodNamesOfCurrentCoreInstance)){


                        this.coreInstanceNativeCallsMap.put(classSmaliUnit.getClassDefinition().getType(),instructions);
                    }
                    //System.out.println("352:Finder : "+callingRef);
                    if(toAnalyzeUsageOfCoreFields.contains(callingRef) || toAnalyzedExceptions.contains(callingRef)){
                        //System.out.println("354:Finder : "+callingRef);
                        //isAbstract = addSmaliUnitRef(callingRef,instructionOpcode,isAbstract);


                        // this is done if we have a static instance, which has to be add
                        String methodCallRef = SmaliLayerUtils.getClassInstanceName(callingRef)+"-><init>()V";
                        String className = CoreUtils.removeArraySign(SmaliLayerUtils.getClassInstanceName(callingRef));



                       SmaliUnit tmpSmaliUnit = this.getSmaliUnit(className, Arrays.asList(new String[] {methodCallRef}),false);
                       if(tmpSmaliUnit.getRelevantMethodDefinitions().length < 1){
                            if(tmpSmaliUnit.getAllMethodDefinitions().keySet().contains("<init>")){
                                Method tmpMethod = tmpSmaliUnit.getAllMethodDefinitions().get("<init>");
                                tmpSmaliUnit.addMethodDefinition(tmpMethod);
                                methodCallRef = tmpMethod.toString();
                            }else{
                                for(Method tmpMethod : tmpSmaliUnit.getAllMethodDefinitions().values()){
                                    if(AccessFlags.formatAccessFlagsForMethod(tmpMethod.getAccessFlags()).contains("abstract")){
                                        isAbstract = true;
                                        tmpSmaliUnit.addMethodDefinition(tmpMethod);
                                        methodCallRef = tmpMethod.toString();
                                        CoreClassRef coreClassRef = new CoreClassRef(methodCallRef,instructionOpcode,tmpSmaliUnit);
                                        relevantMethodReferencesOfCoreMap.put(methodCallRef,coreClassRef);
                                    }
                                }
                            }

                        }
                        if(isAbstract == false){
                            CoreClassRef coreClassRef = new CoreClassRef(methodCallRef,instructionOpcode,tmpSmaliUnit);
                            relevantMethodReferencesOfCoreMap.put(methodCallRef,coreClassRef);
                        }

                        relevantClassDefsSet.add(tmpSmaliUnit.getClassDefinition());
                        //continue;
                    }
                }


                if(instructionOpcode.referenceType == ReferenceType.FIELD &&CoreUtils.isFieldTypeImplemented(callingRef)){
                    SmaliLayer.globalRelevantFieldList.add(callingRef);
                    String methodCallRef = SmaliLayerUtils.getClassInstanceName(callingRef)+"-><init>()V";
                    String className = CoreUtils.removeArraySign(SmaliLayerUtils.getClassInstanceName(callingRef));

                    SmaliUnit tmpSmaliUnit = this.getSmaliUnit(className, Arrays.asList(new String[] {methodCallRef}),false);
                    if(tmpSmaliUnit.getRelevantMethodDefinitions().length < 1){

                        if(tmpSmaliUnit.getAllMethodDefinitions().keySet().contains("<init>")){
                            hasDefaultConstructor = false;
                            tmpSmaliUnit.removeRelevantMethod(methodCallRef);
                            Method tmpMethod = tmpSmaliUnit.getAllMethodDefinitions().get("<init>");
                            tmpSmaliUnit.addMethodDefinition(tmpMethod);
                            methodCallRef = tmpMethod.toString();

                        }else{
                            for(Method tmpMethod : tmpSmaliUnit.getAllMethodDefinitions().values()){
                                if(AccessFlags.formatAccessFlagsForMethod(tmpMethod.getAccessFlags()).contains("abstract")){
                                    isAbstract = true;
                                    hasDefaultConstructor = false;
                                    tmpSmaliUnit.removeRelevantMethod(methodCallRef);
                                    tmpSmaliUnit.addMethodDefinition(tmpMethod);
                                    methodCallRef = tmpMethod.toString();
                                    CoreClassRef coreClassRef = new CoreClassRef(methodCallRef,instructionOpcode,tmpSmaliUnit);
                                    relevantMethodReferencesOfCoreMap.put(methodCallRef,coreClassRef);
                                }
                            }


                        }

                    }
                    if(isAbstract == false){

                        CoreClassRef coreClassRef = new CoreClassRef(methodCallRef,instructionOpcode,tmpSmaliUnit);
                        relevantMethodReferencesOfCoreMap.put(methodCallRef,coreClassRef);
                    }

                    relevantClassDefsSet.add(tmpSmaliUnit.getClassDefinition());

                }

                // a field of the core don't have to be inserted, because it's already part of the core
                if(instructionOpcode.referenceType == ReferenceType.FIELD  && callingClass.equals(classSmaliUnit.getClassDefinition().getType())) {
                    continue;
                }else{

                    if(!callingRef.contains("->") && hasDefaultConstructor){
                        callingRef = callingRef+"-><init>()V";
                    }


                    if(relevantReferencesOfClassMap.size() != 0 && relevantReferencesOfClassMap.get(callingRef) != null){
                        someClassRef = (ClassRef) relevantReferencesOfClassMap.get(callingRef);
                        someClassRef.increaseNumOfUsage();
                    }else{
                        // we have a new class ref
                        someClassRef = new ClassRef(callingRef,instructionOpcode);
                    }

                    relevantReferencesOfClassMap.put(callingRef,someClassRef);



                    if(update == false){
                        if(CoreUtils.isNativeCall(callingRef,this.nativeMethodNamesOfCurrentCoreInstance)){
                            //System.out.println("possible native call2: "+callingRef);
                            this.coreInstanceNativeCallsMap.put(classSmaliUnit.getClassDescriptor(),instructions);
                        }
                    }



                }
            }

        }


        return getImplementedInDex(relevantReferencesOfClassMap);

    }




       /*
     maybe the clinit stuff should be only cosidered in the creation of the new dex file....
     */

    /**
     *
     * @param classSmaliUnit
     * @param isCore
     * @return
     */

    private Map<String,ClassRef>  getClassRefs(SmaliUnit classSmaliUnit,boolean isCore){
        Map<String,ClassRef> relevantReferencesOfClassMap = new HashMap<String,ClassRef>();

        if(isCore){
            return getClassRefsOfMethod(classSmaliUnit,classSmaliUnit.getCoreMethodDefinition().getImplementation().getInstructions(),isCore,false,classSmaliUnit.getCoreMethodDefinition().getImplementation());
           // return getClassRefsOfMethod(classSmaliUnit,classSmaliUnit.getCoreMethodDefinition().getImplementation().getInstructions(),isCore,false);
        }else {

            for(Method tmpRelevantMethod : classSmaliUnit.getRelevantMethodDefinitions()){
                MethodImplementation tmpMethodImplementation = tmpRelevantMethod.getImplementation();
                if (tmpMethodImplementation == null){
                    System.out.println("couldn't solve reference of "+tmpRelevantMethod.toString());
                    continue;
                }
                Map<String,ClassRef> refClassOfMethod = getClassRefsOfMethod(classSmaliUnit,tmpMethodImplementation.getInstructions(),isCore,false,tmpMethodImplementation);
               // Map<String,ClassRef> refClassOfMethod = getClassRefsOfMethod(classSmaliUnit,tmpMethodImplementation.getInstructions(),isCore,false);
                relevantReferencesOfClassMap.putAll(refClassOfMethod);
            }
        }

        return relevantReferencesOfClassMap;

    }


    public Map<String,CoreClassRef> getUpdatedReferencesOfClass(){
        //Map<String,ClassRef> relevantReferencesOfClassMap = new HashMap<String,ClassRef>();
        //this.getRelevantMethodsAndClasses()
        if(relevantMethodClassesReferences.isEmpty()){
            this.getRelevantMethodsAndClasses();
        }

        return this.relevantMethodReferencesOfCoreMap;
        /*
        for(Method tmpRelevantMethod : methodArray){
            Map<String,ClassRef> refClassOfMethod = getClassRefsOfMethod(classSmaliUnit,tmpRelevantMethod.getImplementation().getInstructions(),false,true);
            relevantReferencesOfClassMap.putAll(refClassOfMethod);
        }

        return relevantReferencesOfClassMap;*/
    }



    private void createCurrentCoreClassRefs(SmaliUnit coreClass, boolean isBackwardSlice){

        Map<String,ClassRef> relevantReferencesOfClassMap;
        if(isBackwardSlice){
            // here is the coreClass the class, where we have the method for which we are doing the bacward slice
            relevantReferencesOfClassMap = startBackwardSliceOfMethod(coreClass,true);
        }else{
            relevantReferencesOfClassMap = getClassRefs(coreClass,true);
        }

        for(Map.Entry<String, ClassRef> refOfCore : relevantReferencesOfClassMap.entrySet()){
            String callingRef = refOfCore.getKey();

            Opcode instructionOpcode =  refOfCore.getValue().getInstructionOpcode();


            if(instructionOpcode.referenceType ==  ReferenceType.FIELD){

                if(instructionOpcode.setsRegister() == false){
                    relevantFieldReferencesOfCoreList.add(callingRef);
                }else{
                    relevantFieldReferencesOfCoreListReading.add(callingRef);
                }

                relevantReferenceOfCoreSet.add(callingRef);
            }else {

                CoreClassRef coreClassRef = new CoreClassRef(callingRef,instructionOpcode,coreClass);

                relevantMethodReferencesOfCoreMap.put(callingRef,coreClassRef);
                relevantReferenceOfCoreSet.add(callingRef);

            }



        }

    }



    private SmaliUnit getSmaliUnit(String className,List<String> methods,boolean isSuper){
        if(methods.isEmpty() || methods == null){
            return null;
        }


        SmaliUnit sm = new SmaliUnit();
        className = CoreUtils.removeArraySign(className);
        for(ClassDef classDef :  ClassInitializerFilter.getListOfFilteredClinitClasses()){


            if(classDef.getType().equals(className)) {
                //System.out.println("classDef.getType()2:"+classDef.getType());
                sm.setClassDefinition(classDef);
                /*long testforEmptyMethod = classDef.getMethods().spliterator().getExactSizeIfKnown();
                if (testforEmptyMethod < 1){
                    relevantClassDefsSet.add(classDef);
                    //relevantClassesSet.add(classDef.getType());
                    continue;
                }*/

                for (Method tmpMethod : classDef.getMethods()) {

                   //System.out.println("Methods to analyze further:: "+tmpMethod.toString()+ "     Slice Status: "+this.isStaticBackwardSlice+ "   Method-Name: "+tmpMethod.getName());
                    //System.out.println("AAAA: "+SmaliLayerUtils.getCallingMethodName(tmpMethod.toString()));
                    //System.out.println("AAAA: "+SmaliLayerUtils.getCallingMethodName(tmpMethod.toString()));
                    if(methods.contains(SmaliLayerUtils.getCallingMethodName(tmpMethod.toString())) || tmpMethod.getName().equals("<clinit>") || this.isStaticBackwardSlice){
                        //System.out.println("Methods to analyze further2:: "+tmpMethod.toString()+ "     Slice Status: "+this.isStaticBackwardSlice+ "   Method-Name: "+tmpMethod.getName());
                        if(tmpMethod.getName().equals("<init>") &&  AccessFlags.formatAccessFlagsForMethod(tmpMethod.getAccessFlags()).contains("private")){
                            continue;
                        }else if(tmpMethod.getName().equals("<init>") &&  this.isStaticBackwardSlice){
                        //System.out.println("Methods we can see: "+tmpMethod.toString());
                            sm.addMethodDefinition(tmpMethod);
                            relevantClassDefsSet.add(classDef);
                        }
                        else if(isSuper){
                            sm.addMethodDefinition(tmpMethod);
                            relevantClassDefsSet.add(classDef);
                        }else{
                            sm.addMethodDefinition(tmpMethod);
                            relevantClassDefsSet.add(classDef);
                        }

                    }


                    if(relevantMethodReferencesOfCoreMap.keySet().contains(tmpMethod.toString()) ){
                        sm.addMethodDefinition(tmpMethod);
                        relevantClassDefsSet.add(classDef);
                    }

                    sm.addAllMethodDefinition(tmpMethod.getName(),tmpMethod);


                }
            }
        }


        return sm;
    }


    /**
     *
     * @return a Map<String, ClassRef> which contains the relevant Methods and Classes of the current CoreInstance
     */
    private Map<String, ClassRef> getRelevantMethodsAndClasses(){
        Map<String, ClassRef> relevantMethodReferences = new HashMap<String, ClassRef>();
        Map<String, ClassRef> initialRelevantMethodReferences = new HashMap<String, ClassRef>();
        Map<String, List<String>> tmpMap = null;

        boolean first = true;
        boolean run = true;
        boolean analyze =false;
        while(run){
            if(first){
                first = false;
                tmpMap = getRelevantMethodsListOfClass(null);
                SmaliUnit sm = null;
                for (Map.Entry<String, List<String>> e : tmpMap.entrySet()) {
                    sm = getSmaliUnit(e.getKey(), e.getValue(),false);

                    //initialRelevantMethodReferences = getClassRefs(sm, false);
                    if(this.isStaticBackwardSlice){
                        // here is the coreClass the class, where we have the method for which we are doing the bacward slice
                        initialRelevantMethodReferences = startBackwardSliceOfMethod(sm,false);
                    }else{
                        initialRelevantMethodReferences = getClassRefs(sm,false);
                    }
                    relevantMethodReferences.putAll(initialRelevantMethodReferences);
                }


            }else{
                //System.out.println("der else part in Zeile 659");
                tmpMap = getRelevantMethodsListOfClass(initialRelevantMethodReferences);
                if(tmpMap.isEmpty() || tmpMap == null){

                    run = false;
                    break;
                }

                SmaliUnit sm = null;

                for (Map.Entry<String, List<String>> e : tmpMap.entrySet()) {
                    sm = getSmaliUnit(e.getKey(), e.getValue(),false);
                    /*
                    if(SmaliLayer.remainingCoresSet.contains(sm.getClassDefinition().getType())){

                    }*/
                    //initialRelevantMethodReferences = getClassRefs(sm, false);
                    if(this.isStaticBackwardSlice){
                        // here is the coreClass the class, where we have the method for which we are doing the bacward slice
                        initialRelevantMethodReferences = startBackwardSliceOfMethod(sm,false);
                    }else{
                        initialRelevantMethodReferences = getClassRefs(sm,false);
                    }

                    if(initialRelevantMethodReferences == null || initialRelevantMethodReferences.isEmpty()){
                        run = false;
                        break;
                    }


                    /*
                     * there should be no reason, that we gain the same classRef - which is the key, from <calling-class>-><calling-method> -
                     * twice
                     */
                    if(relevantMethodReferences.containsKey(initialRelevantMethodReferences.keySet().toArray()[0])){
                        //System.out.println("error (1): "+initialRelevantMethodReferences.keySet().toArray()[0]); --> this should be fixed with an better patch
                        run = false;
                        break;
                    }

                    relevantMethodReferences.putAll(initialRelevantMethodReferences);
                }



            }

            if(initialRelevantMethodReferences == null || initialRelevantMethodReferences.isEmpty()){
                run = false;
                break;
            }



        }




        relevantMethodReferences.putAll(relevantMethodReferencesOfCoreMap);

        /*
         derzeit verbugt...
         */
        for(String callRef : relevantMethodReferencesOfCoreMap.keySet()){
            String methodCallRef = SmaliLayerUtils.getClassInstanceName(callRef);
            //System.out.println("MEthodcall_Ref:" +methodCallRef);

            String className = SmaliLayerUtils.getClassInstanceName(callRef);
           // System.out.println("className_REF:" +className);
           // System.out.println("CALLREF:" +callRef);


            getSmaliUnit(className, Arrays.asList(new String[] {methodCallRef}),false);

        }


        relevantMethodClassesReferences.putAll(relevantMethodReferences);
        return relevantMethodReferences;
    }


    private LinkedHashSet<String> getRelevantClassesOfCurrentCore(String coreClassDescriptor){
        LinkedHashSet<String> relevantClassesSetMethod = new LinkedHashSet<String>();
        for (Map.Entry<String,ClassRef> e :relevantMethodClassesReferences.entrySet()){

            String callRefClasses = CoreUtils.removeArraySign(SmaliLayerUtils.getCallingClassName(e.getKey()));
            if(!callRefClasses.equals(coreClassDescriptor))
                relevantClassesSetMethod.add(callRefClasses);

        }

        relevantClassesSet.addAll(relevantClassesSetMethod);
        return relevantClassesSetMethod;
    }


    /*
     füer die variablen variante will ich immer wissen, welche von relevanz sind:
      - diese werden in der clinit beschrieben und wenn diese private sind, so werden diese in der
        der klasse lokal gelesen
        - sind diese publich so müssen diese von anderen gelesen werden

     bei der methoden-variante interessieren mich eigentlich nur die felder welche den typ haben, den die methode als parameter auch hat
     */

    private List<String> getRelevantFieldsOfCore(){

        if(relevantFieldReferencesOfCoreList.isEmpty()){
            return null;
        }

        /*//List<Field> tmpStaticField = coreClass.getClassDefinition().getStaticFields();
        for(Method m : coreClass.getClassDefinition().getMethods()){
          if(m.getName().equals("<clinit>"))  {

              break;
          }
        }*/
        return relevantFieldReferencesOfCoreList;
    }





    /*
     public/protected accessors
     */

    protected Map<String,CoreClassRef> getCurrentCoreClassRefs(SmaliUnit coreClass){
        if(relevantMethodReferencesOfCoreMap.isEmpty()){
            createCurrentCoreClassRefs(coreClass,this.isStaticBackwardSlice);
        }
        return relevantMethodReferencesOfCoreMap;
    }



    protected LinkedHashSet<String> getRelevantClassesSet(String classDescriptor){

        if(relevantClassesSet.isEmpty()){
            this.getRelevantClassesOfCurrentCore(classDescriptor);

        }

        //return getRelevantClassesOfCurrentCore(classDescriptor);
        return relevantClassesSet;
    }

    protected Map<String, ClassRef> getRelevantMethodClassesReferences(){
        return relevantMethodClassesReferences;
    }

    protected LinkedHashSet<ClassDef> getRelevantClassDefsSet(){
        return relevantClassDefsSet;
    }

    protected List<String> getReadingFieldReferencesOfCoreIntance(){
        return relevantFieldReferencesOfCoreListReading;
    }


    protected Map<String,Iterable<? extends Instruction>> getCurrentCoreInstanceNativeCallsMap(){
        return this.coreInstanceNativeCallsMap;
    }

    protected List<String> getNativeMethodNamesOfCurrentCoreInstance(){
        return this.nativeMethodNamesOfCurrentCoreInstance;
    }


    protected Map<String,InitCallRef>  getInitCallRefsMap() {
        return this.coreInstanceInitCallsMap;
    }
}

