package dexslib.core;


import dexslib.SmaliLayer;
import dexslib.core.slicing.BackwardSliceOnRegisterLevel;
import dexslib.core.slicing.MethodParameters;
import dexslib.types.CoreClassRef;
import dexslib.types.InstructionRef;
import dexslib.types.SmaliUnit;
import dexslib.util.CoreUtils;
import dexslib.util.SmaliLayerUtils;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.ReferenceType;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;

import java.util.*;

/**
 * Created by Daniel Baier on 03.01.17.
 */
public class CoreInstance {

    private String deobfuscationRoutine = "NONE";
    private int deobfuscationType = -1;
    private boolean isCoreInstanceCreated = false;



    private Map<String,CoreClassRef> relevantMethodReferencesOfCoreMap;



     /* instance variables */
     private CoreInstanceReferences coreInstanceReferences;
    private DexFile dexFile;
    private SmaliUnit currentCoreClassInstance;
    private CoreInstanceType coreInstanceType;
    private String classDescriptor;
    private boolean doOnlyStaticMethodSlice;

    private String tmpDeobfuscationRoutineName;







    public CoreInstance(DexFile dxFile,SmaliUnit currentCoreClassIntance, boolean doOnlyStaticMethodSlice){




        classDescriptor = currentCoreClassIntance.getClassDefinition().getType();
        this.dexFile = dxFile;
        this.currentCoreClassInstance = currentCoreClassIntance;
        this.doOnlyStaticMethodSlice =  doOnlyStaticMethodSlice;

        //System.out.println("ClassDescriptor: "+currentCoreClassInstance.getClassDescriptor());

        // when doing this instance we create a ClassRef for the currentCoreInstance, if we are doing a static backwardslice
        // the currentCoreInstance is the class where the method is implemented
        //this.coreInstanceReferences = new CoreInstanceReferences(currentCoreClassIntance,doOnlyStaticMethodSlice);
        this.coreInstanceReferences = new CoreInstanceReferences(currentCoreClassIntance,false);

        relevantMethodReferencesOfCoreMap = this.coreInstanceReferences.getCurrentCoreClassRefs(currentCoreClassIntance);

/*
        if(doOnlyStaticMethodSlice){ /
            System.out.println("relevantMethodReferencesOfCoreMap: ");
        for(String s : relevantMethodReferencesOfCoreMap.keySet()){
            System.out.println("key: "+s);
        }
        //System.exit(1);
        // }*/



        //this.coreInstanceReferences.;
        this.coreInstanceType = new CoreInstanceType(); // here we check for the types

        this.createCoreInstance();



        // findemichhier --> hie die isDeobMethode aufrufen.
        // DEOBFUSCATION_TYPE_METHOD_BS = 0x11;

        // hier brauche ich eine prüfung --> wenn bestimmte Listen etc. leer sind, dann soll hier geprüft werden ob es eine deobmethode gibt
        // und dann soll die HashMap<String,InstructionRef> befüllt werden.
        this.coreInstanceReferences.createReferenceStructures(classDescriptor);


        //if(doOnlyStaticMethodSlice){
/*
            System.out.println("getRelevantMethodClassesReferences(): ");
            for(String s : this.coreInstanceReferences.getRelevantMethodClassesReferences().keySet()){
                System.out.println("key: "+s);
            }


            System.out.println("getRelevantClassesSet(classDescriptor): ");
            for(String s : this.coreInstanceReferences.getRelevantClassesSet(classDescriptor)){
                System.out.println("key: "+s);
            }*/

            //System.exit(1);
        //}

    }


    /*

    /*
     * removes the method references which are not implemented in the dex file
     *

    private void removeNotImplementedInDex(){
        String[] callingRefArray = (String[]) relevantMethodReferencesOfCoreMap.keySet().toArray(new String[relevantMethodReferencesOfCoreMap.size()]);
        for(String tmpCallingRef : callingRefArray){
            if(CoreUtils.isImplemented(tmpCallingRef)){
                continue;
            }else{
                relevantMethodReferencesOfCoreMap.remove(tmpCallingRef);
                relevantReferenceOfCoreSet.remove(tmpCallingRef);
            }
        }

    }*/















    /*
     * thos only works for the clinit version because we expect here that the obfuscated strings stored inside some static field
     */
    private String getMostUsedTypeOfStaticFieldFromCore(SmaliUnit coreClass){

        Map<String, Integer> map = new HashMap<>();

        for (Field tmpStaticField : coreClass.getClassDefinition().getStaticFields()) {
            String fieldType = tmpStaticField.getType();
            Integer val = map.get(fieldType);
            map.put(fieldType, val == null ? 1 : val + 1);
            // furthermore we test here, if this
        }

        Map.Entry<String, Integer> max = null;

        for (Map.Entry<String, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue())
                max = e;
        }

        return (String) max.getKey();
    }










    /*
    * return a list of the used Methods orderd with the most used methods on position 0
    */
    private List<CoreClassRef> getSortedListDescOfUsageOfRelevantMethods(){
        List<CoreClassRef> listOfSortedMethodRefs = new ArrayList<CoreClassRef>(relevantMethodReferencesOfCoreMap.values());
        if(relevantMethodReferencesOfCoreMap.size() == 0 || relevantMethodReferencesOfCoreMap == null){
            // return empty if no method are references from the core which are relevant for further analysis
            return  listOfSortedMethodRefs;
        }


        Collections.sort(listOfSortedMethodRefs);

        return listOfSortedMethodRefs;
    }






    private LinkedHashSet<String> getDeobfuscationRoutines(SmaliUnit coreClassSmaliUnit){
        LinkedHashSet<String> deobfuscationRoutinesList = new LinkedHashSet<String>();
        if(getSortedListDescOfUsageOfRelevantMethods() == null || getSortedListDescOfUsageOfRelevantMethods().isEmpty() || deobfuscationRoutine.equals("NONE")){
            // we return an empty list
            //System.out.println("Warum sind wir hier^^: "+deobfuscationRoutine);
            return deobfuscationRoutinesList;
        }

        // we assume that the static field which is used mostly in a core is the method
        if(this.doOnlyStaticMethodSlice){
                //for(CoreClassRef tmpCoreRef : getSortedListDescOfUsageOfRelevantMethods()){


                    // maybe we should set the type here to Ljava/lang/String;
                    String tmpCallRefsOfDeobRoutines = getSortedListDescOfUsageOfRelevantMethods().get(getSortedListDescOfUsageOfRelevantMethods().size()-1).getCallingRef();

                    //System.out.println("TMP: "+getSortedListDescOfUsageOfRelevantMethods().get(getSortedListDescOfUsageOfRelevantMethods().size()-1).getCallingRef());
                    String callRefAdjustedForTemplate = "Lcom/obfuscation/defuscadotemplate/" + SmaliLayerUtils.getClassNameWithoutPackage(SmaliLayerUtils.getCallingClassName(tmpCallRefsOfDeobRoutines))+"->"+SmaliLayerUtils.getCallingMethodName(tmpCallRefsOfDeobRoutines);
                    deobfuscationRoutinesList.add(callRefAdjustedForTemplate);
                //}
                //System.exit(1);
        }else{
        String relevantType = getMostUsedTypeOfStaticFieldFromCore(coreClassSmaliUnit);

        for(CoreClassRef tmpCoreRef : getSortedListDescOfUsageOfRelevantMethods()){
            String tmpCallRefsOfDeobRoutines = tmpCoreRef.getCallingRef();
            // temproaere lsite an moeglichen deobfuscation methoden
           // System.out.println("TMP: "+tmpCallRefsOfDeobRoutines);
            if(relevantType.equals(SmaliLayerUtils.getCallingMethodParameter(tmpCallRefsOfDeobRoutines))){
                String callRefAdjustedForTemplate = "Lcom/obfuscation/defuscadotemplate/" + SmaliLayerUtils.getClassNameWithoutPackage(SmaliLayerUtils.getCallingClassName(tmpCallRefsOfDeobRoutines))+"->"+SmaliLayerUtils.getCallingMethodName(tmpCallRefsOfDeobRoutines);
                deobfuscationRoutinesList.add(callRefAdjustedForTemplate);
                //System.out.println("Deobfuscation Method: "+tmpCallRefsOfDeobRoutines);
                //System.out.println("Deobfuscation Method: "+callRefAdjustedForTemplate);
            }
        }
        }
        //CoreClassRef tmp = getSortedListDescOfUsageOfRelevantMethods().get(posOfMethod);


        //String callRef = tmp.getCallingRef();

        //deobfuscationRoutines.put(callRefAdjustedForTemplate,tmp);


        return deobfuscationRoutinesList;
    }


/*
    public Map<String,Field> getFieldInvocationsOfCoreMap(){
        return null;
    }*/


    private CoreClassRef getDesignatedDeobfuscationRoutine(int posOfMethod){
        //System.out.println("Pos Value initial: "+posOfMethod);
        if(getSortedListDescOfUsageOfRelevantMethods() == null || getSortedListDescOfUsageOfRelevantMethods().isEmpty() || getSortedListDescOfUsageOfRelevantMethods().size() <= posOfMethod){
            //System.out.println("found error here: 225: "+getSortedListDescOfUsageOfRelevantMethods().isEmpty() + "  groeße: "+getSortedListDescOfUsageOfRelevantMethods().size());

            return null;
        }

        // System.out.println("pos: "+posOfMethod);
        return getSortedListDescOfUsageOfRelevantMethods().get(posOfMethod);
    }



    /*
     here we analyze the instruction used by this methods, if its an deobfuscation methods there will always be some invoke followed by an write/put-operation of the new literal

     letztendlich schaue ich zunächst nach allen aufrufen der mutmaßlich deob-routine - auch im core selbst
     danach schaue ich, ob im anschluß ein array oder ein objekt (string) beschrieben wird, welches aus dem core kommt

     jedoch will ich nur die oberst verwendetet methode haben, also jene welche den gleichen datentyp der der meisten statischen felders des core verwendet

     nach dem die erste deobfuskierungs-routine identifiziert wurde, muss auch bei den weiteren Methoden, welche einen schreibende zugriff auf die statischen felder hat geprüft werden,
     ob auch andere methoden diese schreibend-verändern, besonders wenn diese vom typ byte, char, int, string (Ljava/lang/String;), string[] ([Ljava/lang/String;), stringbuilder (Ljava/lang/StringBuilder;) sind



     WICHTIG: DIESER Ansatz hier ist für das CLINIT-Verfahren gedacht. Wurde die Methode eigenständig identifiziert und mitangegeben (-bs), so ist das BACKWARD-Slicing nicht so wichtig

     SmaliForwardSlicing.followInstructionByRegister --> ich könnte diese Methode nutzen, um den Datenfluß des REgisters zu folgen, welches für ein [B oder [C verwendet wurde, umso dessen
     Deob-/Entschlüsselungs-Route zu identifizieren


  */
    private int analyzeMethodInvocation(DexFile dx){
        int posInListOfUsageOfRelevantMethods = 0;
        String tmpNameOfRoutine = "";
        while(true){


            CoreClassRef designatedDeobfuscationRoutine = getDesignatedDeobfuscationRoutine(posInListOfUsageOfRelevantMethods);

            if(designatedDeobfuscationRoutine == null || dx == null){
                //System.out.println("faield....");
                return -1;
            }

            for(ClassDef tmpClassDef : dx.getClasses()){
                boolean cfg = false;
                short tripleCounter = 0; // if this value is three we have a triple


                if(tmpClassDef.getType().startsWith("Landroid/support/v") || tmpClassDef.getType().startsWith("Landroid/support/annotation/") || tmpClassDef.getType().contains("R$")){
                    continue;
                }

                for(Method tmpMethod : tmpClassDef.getMethods()){




                    if(tmpMethod.getImplementation() == null )
                        continue;

                    for (Instruction instruction : tmpMethod.getImplementation().getInstructions()) {
                        Opcode instructionOpcode = instruction.getOpcode();

                        // SpecialisedInstruction instructionOfInterest = new SpecialisedInstruction(instruction, tmpMethod.getImplementation().getRegisterCount(), tmpMethod.getParameters().size());

                        // of course this has to be improved, because we can also have here an byte-array which is passed to the deobfuscationRoutine or
                        // we can have a method which returns a strings which is than passed to the deobfuscationRoutine
                        if(instructionOpcode.referenceType == ReferenceType.STRING ){
                            String callingRef = ((ReferenceInstruction) instruction).getReference().toString();
                            tripleCounter = 1;


                            continue;
                            //System.out.println("possible obfuscated string: "+callingRef);
                        }


                        if(instructionOpcode.setsResult() || instructionOpcode.referenceType == ReferenceType.METHOD  ){
                            String callingRef = ((ReferenceInstruction) instruction).getReference().toString();

                            if(designatedDeobfuscationRoutine.getCallingRef().equals(callingRef)){
                                cfg = true;
                               // System.out.println("possible DeobfuscationRoutine: "+callingRef);
                                //System.out.println("Format: "+Format.Format21c. );
                                if (tripleCounter == 1){
                                    tripleCounter = 2;
                                    tmpNameOfRoutine = callingRef;
                                }
                            }

                            continue; // go to the next instruction
                        }

                    /*if(!instructionOpcode.setsResult() && instructionOpcode.format == Format.Format21c && cfg){ eher nuetzlich um die Verwendung von Felder weiter zu pruefen
                     * man koennte fuer die field-variante pruefen was mit den eigenen felder passiert, also sowas wie
                     * zeige mir alle felder der core-klasse, schaue nun was passiert mit den feldern bevor diese schreibend aufgerufen werden bzw.
                     *
                     * in https://github.com/JesusFreke/smali/blob/master/dexlib2/src/main/java/org/jf/dexlib2/Opcode.java we can find a good overview of the Format relating to instrctions
                     *
                     */
                        if(!instructionOpcode.setsRegister() && instructionOpcode.format == Format.Format21c && cfg){

                            String callingRef = ((ReferenceInstruction) instruction).getReference().toString();
                            String callingClassOfField = CoreUtils.removeArraySign(SmaliLayerUtils.getCallingClassName(callingRef));

                            if(designatedDeobfuscationRoutine.getCoreClass().getClassDefinition().getType().equals(callingClassOfField)){
                                // System.out.println("Class: "+tmpClassDef.getType() +" Method: "+tmpMethod.getName()+" Opcode:"+instructionOpcode.name() +" instruction-ref: "+callingRef);
                                //System.out.println("\n\n");
                                this.deobfuscationRoutine = designatedDeobfuscationRoutine.getCallingRef();
                                cfg = false;
                                return posInListOfUsageOfRelevantMethods;
                            }


                        }

                        // for now we should also
                        else if(tripleCounter == 2 && instructionOpcode.name.equals("move-result-object")){ // here we check for a triple of instruction which can be interesting for identify deobfuscation routines
                            System.out.println("This is the possible deobfuscationRoutine: "+tmpNameOfRoutine);
                            this.tmpDeobfuscationRoutineName = tmpNameOfRoutine;
                            this.deobfuscationRoutine = designatedDeobfuscationRoutine.getCallingRef();
                            this.doOnlyStaticMethodSlice = true;
                            return posInListOfUsageOfRelevantMethods;
                        }


                    }

                }
            }

            posInListOfUsageOfRelevantMethods++;
        }

    }


    /**
     *  The Analysis is performd/started from this method
     */
    private void createCoreInstance(){

        MethodImplementation methodImplementation;
        if(this.doOnlyStaticMethodSlice){
            methodImplementation= this.currentCoreClassInstance.getBackwardSliceMethodDefinition().getImplementation();
        }else{
            methodImplementation= this.currentCoreClassInstance.getCoreMethodDefinition().getImplementation();
        }


        if (methodImplementation != null) {
            //SmaliLayerUtils.setCoreClassPackage(this.currentCoreClassInstance.getClassDefinition());







            //this.createCurrentCoreClassRefs(coreClass); //ensures that we can access the relevantMethodReferencesOfCoreMap
            // for now we ignore the case, when the core is used to read or write fields of other classes
            //System.out.println("Methods we have to keep inside the core");

            /*for (Map.Entry<String, CoreClassRef> e : relevantMethodReferencesOfCoreMap.entrySet()) {

                String s = e.getKey();
                //   System.out.println(s);
            }*/
            //System.out.println("\n");




            /*
             * The DEOBFUSCATION_TYPE_FIELD can only be used if we access the obfuscated Strings for the deobfuscationsProcess when accessing Fields.
             * Therefore we need for usesases where the obfuscated string is directly accessed an addtional metric, for now this metric is only used when we specify the method of interest -bs parameter
             *
             * In those cases we often have something like this:
             *
             *  const-string v2, "guvf vf zl frperpg zrffntr...abobql pna ernq guvf unununun"
             *
             *  invoke-virtual {v0, v2}, Lcom/example/obfuscator/customstringobfuscator/CustomStringObfuscation;->getRot13Decrypted(Ljava/lang/String;)Ljava/lang/String;
             *
             *  move-result-object v2
             *
             */

            int posOfDeobRoutine = analyzeMethodInvocation(this.dexFile);

            // if we have here -1 it only means that we couldn't easily determine the deobfuscationRoutine, therefore we try know to identify the fields in which we
            // expect the obfuscated strings...but this has some logic flaw, because what if the strings aren't in fields but directly used (this means the obfuscated strings stored inside a register value of type string)
            // like const-string v2, "guvf vf zl frperpg zrffntr...abobql pna ernq guvf unununun"
            //System.out.println("Pos of DeobRoutine: "+posOfDeobRoutine);
            this.deobfuscationType =this.coreInstanceType.getDeobfuscationType(this.currentCoreClassInstance,this.dexFile,posOfDeobRoutine,relevantMethodReferencesOfCoreMap);

            if(deobfuscationType == SmaliLayer.DEOBFUSCATION_TYPE_FIELD){
                //System.out.println("Identified DeobfuscationType: FIELD for class: "+this.classDescriptor);
                SmaliLayer.FIELDS_DEBOFSCATION_TYPE_LIST.add(this.classDescriptor);
            }else if(deobfuscationType == SmaliLayer.DEOBFUSCATION_TYPE_METHOD){
                SmaliLayer.METHODS_DEBOFSCATION_TYPE_LIST.add(this.classDescriptor);
                //System.out.println("Identified DeobfuscationType: METHOD for class: "+this.classDescriptor);
            }else{
                SmaliLayer.UNKNOWN_DEOBFUSCATION_TYPE_CNT++;
                SmaliLayer.UNKNOWN_DEOBFUSCATION_CLASSES_LIST.add(this.classDescriptor);

                //System.out.println("Identified DeobfuscationType: UNKNOWN");
                //System.out.println("aborting ....");
                //System.out.println("Plz send the analyzed APK or SHA256 to admin-[at]-remoteshell-security.com");
                //System.exit(2);
            }



            /*Iterator<String> itr = relevantReferenceOfCoreSet.iterator(); // was bedeuted dieses feld?
            while(itr.hasNext()){
               System.out.println(itr.next());
            }*/

           // System.out.println("\n");


            // normalerweise wenn der typ 2 ist, also vom typ field, reicht es das wir nicht

            // we ensure that those structuere can be accessed




            this.isCoreInstanceCreated = true;




        }else{
            SmaliLayer.printUnknownError();
        }


    }

    private Method getMethodByName(String methodName){
        for(ClassDef classDef : SmaliLayer.getDexFile2Analyze().getClasses()){
            for(Method method : classDef.getMethods()){
                if(method.toString().equals(methodName)){
                    return method;
                }
            }
        }
        return null;
    }


    /**
     *   public static final int DEOBFUSCATION_TYPE_METHOD = 0x1;
     *     public static final int DEOBFUSCATION_TYPE_METHOD_BS = 0x11; // we have no clinit method, but we could identify the deobfuscation method and its input
     *     public static final int DEOBFUSCATION_TYPE_FIELD = 0x2;
     *     public static final int DEOBFUSCATION_TYPE_UNKNOWN = 0xd;
     *
     *
     * @return
     */



    public CoreInstanceResults getResultsOfCoreInstance(){
        if(this.isCoreInstanceCreated){
            LinkedHashSet<String> deobfuscationRoutinesList = getDeobfuscationRoutines(this.currentCoreClassInstance);
            /*System.out.println("possible deobfuscation Routines:");
            for(String s : deobfuscationRoutinesList){
                System.out.println("Routine: "+s);
            }*/

            // Somehow this is empty: this.coreInstanceType.getFieldInvocationsOfCoreMap()


            CoreInstanceResults currentCoreResults = new CoreInstanceResults(this.currentCoreClassInstance,this.deobfuscationType,deobfuscationRoutinesList,this.coreInstanceReferences.getRelevantClassesSet(this.classDescriptor),this.coreInstanceType.getFieldInvocationsOfCoreMap(),this.getSortedListDescOfUsageOfRelevantMethods(),relevantMethodReferencesOfCoreMap,this.coreInstanceReferences.getRelevantMethodClassesReferences(),this.coreInstanceReferences.getRelevantClassDefsSet(),this.coreInstanceReferences.getCurrentCoreInstanceNativeCallsMap(),this.coreInstanceReferences.getInitCallRefsMap());

            /* DEBUG-OUTPUT
            System.out.println("+++++++++++++++++++++++++++ OUTPUT ++++++++++++++++++++++++++++++++++");
            System.out.println("currentCoreResults.getRelevantMethodReferencesOfCoreMap().size() ="+currentCoreResults.getRelevantMethodReferencesOfCoreMap().size());


            System.out.println("currentCoreResults.getCurrentCoreClassSmaliUnit().getRelevantMethodDefinitions().length ="+currentCoreResults.getCurrentCoreClassSmaliUnit().getRelevantMethodDefinitions().length);
            System.out.println("currentCoreResults.getDeobfuscationType() ="+currentCoreResults.getDeobfuscationType());
            System.out.println("currentCoreResults.getDeobfuscationRoutinesList().size() ="+currentCoreResults.getDeobfuscationRoutinesList().size());
            System.out.println("currentCoreResults.getFieldInvocationsOfCurrentCoreMap().size() ="+currentCoreResults.getFieldInvocationsOfCurrentCoreMap().size());
            System.out.println("currentCoreResults.getNativeMethodNamesOfCurrentCoreInstance().size() ="+currentCoreResults.getNativeMethodNamesOfCurrentCoreInstance().size());
            System.out.println("currentCoreResults.getRelevantMethodClassesReferences().size() ="+currentCoreResults.getRelevantMethodClassesReferences().size());
            System.out.println("currentCoreResults.getSortedListDescOfUsageOfRelevantMethods().size() ="+currentCoreResults.getSortedListDescOfUsageOfRelevantMethods().size());
            System.out.println("currentCoreResults.getClassNamesOfRelevantClasses().size() ="+currentCoreResults.getClassNamesOfRelevantClasses().size());
            System.out.println("currentCoreResults.getInitCallRef().size() ="+currentCoreResults.getInitCallRef().size());
            System.out.println("currentCoreResults.getRelevantClassDefsSet().size() ="+currentCoreResults.getRelevantClassDefsSet().size());

            System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
            */

            if(currentCoreResults.hasNativeCall()){
                currentCoreResults.setNativeMethodNamesOfCurrentCoreInstance(this.coreInstanceReferences.getNativeMethodNamesOfCurrentCoreInstance());
            }


            // testing for false positives
            if(currentCoreResults.getDeobfuscationType() == SmaliLayer.DEOBFUSCATION_TYPE_FIELD && currentCoreResults.getRelevantMethodReferencesOfCoreMap().size() == 0 &&
                    currentCoreResults.getCurrentCoreClassSmaliUnit().getRelevantMethodDefinitions().length == 0 &&
                    currentCoreResults.getDeobfuscationRoutinesList().size() == 0 &&
                    currentCoreResults.getRelevantMethodClassesReferences().size() == 0 &&
                    currentCoreResults.getSortedListDescOfUsageOfRelevantMethods().size() == 0 &&
            currentCoreResults.getClassNamesOfRelevantClasses().size() == 0 &&
                    currentCoreResults.getInitCallRef().size() == 0 &&
                    currentCoreResults.getRelevantClassDefsSet().size()  == 0){



                return null;

            }


            /**
             *  This has to be improved, for now this is only a workaround
             */
            if(currentCoreResults.getRelevantMethodReferencesOfCoreMap().size() ==1 && currentCoreResults.getDeobfuscationRoutinesList().size() == 1 && currentCoreResults.getRelevantMethodClassesReferences().size() == 1 && currentCoreResults.getSortedListDescOfUsageOfRelevantMethods().size() == 1 && currentCoreResults.getClassNamesOfRelevantClasses().size() == 1 && currentCoreResults.getInitCallRef().size() == 0 &&
                    currentCoreResults.getRelevantClassDefsSet().size() == 1 && this.tmpDeobfuscationRoutineName.length() > 1){

                currentCoreResults.setDeobfuscationType(SmaliLayer.DEOBFUSCATION_TYPE_METHOD_BS);

                Method method = getMethodByName(this.tmpDeobfuscationRoutineName);
                if(method == null){
                    System.out.println("Error while receiving the method object");
                    return null;
                }

                Map<InstructionRef, MethodParameters> obfuscatedStringsForDeobMethodMap = new HashMap<InstructionRef,MethodParameters>();

                for(ArrayList<InstructionRef> tmpList : BackwardSliceOnRegisterLevel.getAllMethodsRegs(method)){ //
                    obfuscatedStringsForDeobMethodMap.putAll(BackwardSliceOnRegisterLevel.getObfuscatedParamtersForDeobMethod(tmpList));
                    // obfuscatedStringsForDeobMethodMap.putAll(BackwardSliceOnRegisterLevel.getObfuscatedStringsForDeobMethod(tmpList));
                }


                currentCoreResults.setObfuscatedStringsForDeobMethodMap(obfuscatedStringsForDeobMethodMap);


            }


            return currentCoreResults;
        }


        return null;
    }




    /**
     *  old methods which are removed in the next release
     */

    /**
     *  this was an old check for possible core-classes (clinit-classes) but with only refs to already existing cores.
     *  Those classes aren't cores, but have a clinit which refs to an real core
     * @param coreInstanceResultsList
     * @param tmpRelevantFieldReferencesOfCoreListReading
     * @return
     */
    private boolean onlyRefsToCore(List<CoreInstanceResults> coreInstanceResultsList,List<String> tmpRelevantFieldReferencesOfCoreListReading){

        if(tmpRelevantFieldReferencesOfCoreListReading.isEmpty()){
            return true;
        }

        for(CoreInstanceResults previousResults : coreInstanceResultsList){
            String prevCoreClassName = previousResults.getCurrentCoreClassSmaliUnit().getClassDefinition().getType();
            for(String fieldNameOfPossibleNewCore : tmpRelevantFieldReferencesOfCoreListReading){

                if(!SmaliLayerUtils.getCallingClassName(fieldNameOfPossibleNewCore).equals(prevCoreClassName)){
                    return true;
                }
            }

        }





        return false;
    }

}
