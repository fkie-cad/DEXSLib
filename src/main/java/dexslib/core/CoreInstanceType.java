package dexslib.core;

import dexslib.ClassInitializerFilter;
import dexslib.SmaliLayer;
import dexslib.types.CoreClassRef;
import dexslib.types.SmaliUnit;
import dexslib.util.CoreUtils;
import dexslib.util.SmaliLayerUtils;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.ReferenceType;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel Baier on 03.01.17.
 */
public class CoreInstanceType {

    private Map<String,Field> fieldInvocationsOfCoreMap =new HashMap<String,Field>();

    private int numOfPossibleObfuscatedFieldsWithMethod = 0;




    public CoreInstanceType(){

    }






    /*
  soll benutzt werden um besser zu kapseln und wieder verwendbaren code zu haben für die Methode getFieldsInvocationsOfCore
 */
    private String fieldInvocationsOfCore(SmaliUnit coreClass,Iterable<? extends Instruction> instructions){

        for (Instruction instruction : instructions) {
            Opcode instructionOpcode = instruction.getOpcode();

            // later we can use something like this, to only check for certain calls (instructionOpcode.equals(Opcode.SGET_OBJECT))
            // or certain types for instance: IGET_BYTE, IGET_CHAR,IGET_OBJECT,...
            if (instructionOpcode.setsRegister() && instructionOpcode.referenceType == ReferenceType.FIELD) {
                //String fieldRef = CoreUtils.removeArraySign(((ReferenceInstruction) instruction).getReference().toString());
                String fieldRef = ((ReferenceInstruction) instruction).getReference().toString();


                // we only print those fields which are from the core
                if (CoreUtils.isImplemented(fieldRef) && SmaliLayerUtils.getCallingClassName(fieldRef).equals(coreClass.getClassDefinition().getType())) {
                    return fieldRef;
                }

            }

        }

        return "";
    }


    /*
     * it returns an Map of fields from core which are called from other clinits than the core
     *
     */

    private Map<String,Field> getFieldsInvocationsOfCore(SmaliUnit coreClass){
        // ,Map<String,CoreClassRef> passedRelevantMethodReferencesOfCoreMap,LinkedHashSet<ClassDef> passedRelevantClassDefsSet
        Map<String,Field> fieldInvocationsOfCore = new HashMap<String,Field>();
        List<String> fieldRefList =new ArrayList<String>();
        List<String> possibleFieldRefList =new ArrayList<String>();

        for(ClassDef cd :   ClassInitializerFilter.getListOfFilteredClinitClasses()) {


            if(!cd.getType().equals(coreClass.getClassDefinition().getType())) {

                for (Method tmpMethod : cd.getMethods()) {

                   // if (tmpMethod.getName().equals("<clinit>")) {
                    if (tmpMethod.toString().equals(coreClass.getMethod2Analyze())) {

                        for (Instruction instruction : tmpMethod.getImplementation().getInstructions()) {
                            Opcode instructionOpcode = instruction.getOpcode();

                            // later we can use something like this, to only check for certain calls (instructionOpcode.equals(Opcode.SGET_OBJECT))
                            // or certain types for instance: IGET_BYTE, IGET_CHAR,IGET_OBJECT,...
                            if (instructionOpcode.setsRegister() && instructionOpcode.referenceType == ReferenceType.FIELD) {
                                //String fieldRef = CoreUtils.removeArraySign(((ReferenceInstruction) instruction).getReference().toString());
                                String fieldRef =((ReferenceInstruction) instruction).getReference().toString();



                                // we only print those fields which are from the core
                                // CoreUtils.isImplemented(fieldRef) &&
                                if ( SmaliLayerUtils.getCallingClassName(fieldRef).equals(coreClass.getClassDefinition().getType())) {
                                    fieldRefList.add(fieldRef);
                                }

                            }

                        }
                    }

                }


            }else {

                /**
                 *
                 *
                 *
                 *
                 *      wichtig
                 *
                 *
                 */


                // moeglicherweise neuer state fuer internen call  wie es bei whatsapp ist
               // for (Method tmpMethod : cd.getMethods()) {
                for (Method tmpMethod : cd.getMethods()) {

                    //if (tmpMethod.getName().equals("<clinit>")) {
                    if (tmpMethod.toString().equals(coreClass.getMethod2Analyze())) {

                        for (Instruction instruction : tmpMethod.getImplementation().getInstructions()) {
                            Opcode instructionOpcode = instruction.getOpcode();

                            // later we can use something like this, to only check for certain wrting (instructionOpcode.equals(Opcode.SPUT_OBJECT))
                            // or certain types for instance: IPUT_BYTE, IPUT_CHAR,IPUT_OBJECT,...
                            if (!instructionOpcode.setsRegister() && instructionOpcode.referenceType == ReferenceType.FIELD) {
                                //String fieldRef = CoreUtils.removeArraySign(((ReferenceInstruction) instruction).getReference().toString());
                                String fieldRef = ((ReferenceInstruction) instruction).getReference().toString();


                                // we only print those fields which are from the core
                                if (CoreUtils.isImplemented(fieldRef) && SmaliLayerUtils.getCallingClassName(fieldRef).equals(coreClass.getClassDefinition().getType())) {
                                    possibleFieldRefList.add(fieldRef);
                                }

                            }

                        }

                    }


                    // if(cd.getType().equals(coreClass.getClassDefinition().getType()) && !tmpMethod.getName().equals("<clinit>")){

                    // das hier ist auf jedenfall normalerfield anasatz --> eigentlich sind alle vom typ field ansatz
                    //if(cd.getType().equals(coreClass.getClassDefinition().getType()) && !tmpMethod.getName().equals("<clinit>")){
                    if(cd.getType().equals(coreClass.getClassDefinition().getType()) && !tmpMethod.toString().equals(coreClass.getMethod2Analyze())){

                        MethodImplementation methodImplementation = tmpMethod.getImplementation();

                        if(methodImplementation == null){
                            continue;
                        }
                        for (Instruction instruction : methodImplementation.getInstructions()) {
                            Opcode instructionOpcode = instruction.getOpcode();



                            /*
                            if(instructionOpcode.referenceType == ReferenceType.TYPE){
                                String fieldRef = ((ReferenceInstruction) instruction).getReference().toString();
                                if(!CoreUtils.isBasicType(fieldRef) && !fieldRef.startsWith("Landroid") && !fieldRef.startsWith("Ljava") && !fieldRef.startsWith("[Landroid") && !fieldRef.startsWith("[Ljava") && !fieldRef.startsWith("Ljavax")){
                                    System.out.println("A Type: "+fieldRef);
                                }

                            }*/

                            // later we can use something like this, to only check for certain calls (instructionOpcode.equals(Opcode.SGET_OBJECT))
                            // or certain types for instance: IGET_BYTE, IGET_CHAR,IGET_OBJECT,...
                            if (instructionOpcode.referenceType == ReferenceType.FIELD) {
                                //String fieldRef = CoreUtils.removeArraySign(((ReferenceInstruction) instruction).getReference().toString());
                                String fieldRef = ((ReferenceInstruction) instruction).getReference().toString();
                                //SmaliLayer.globalRelevantFieldList.add(fieldRef);
                                /*
                                String methodCallRef = SmaliLayerUtils.getClassInstanceName(fieldRef)+"-><init>()V";
                                String className = SmaliLayerUtils.getClassInstanceName(fieldRef);

                                CoreInstanceReferences coreInstanceReferences = new CoreInstanceReferences();
                                SmaliUnit tmpSmaliUnit = coreInstanceReferences.getSmaliUnit(className, Arrays.asList(new String[] {methodCallRef}),passedRelevantClassDefsSet,passedRelevantMethodReferencesOfCoreMap);
                                CoreClassRef coreClassRef = new CoreClassRef(methodCallRef,instructionOpcode,tmpSmaliUnit);*/
                                if (instructionOpcode.setsRegister()) {

                                    // we only print those fields which are from the core
                                    if (CoreUtils.isImplemented(fieldRef) && SmaliLayerUtils.getCallingClassName(fieldRef).equals(coreClass.getClassDefinition().getType()) && possibleFieldRefList.contains(fieldRef)) {
                                        fieldRefList.add(fieldRef);

                                    }

                                }
                            }

                        }

                    }

                }




            }
        }




        for(Field fieldOfInterest : coreClass.getClassDefinition().getFields()){

            //if(fieldRefList.contains(CoreUtils.removeArraySign(fieldOfInterest.toString()))){
            if(fieldRefList.contains(fieldOfInterest.toString())){
                fieldInvocationsOfCore.put(fieldRefList.get(fieldRefList.indexOf(fieldOfInterest.toString())),fieldOfInterest);
                continue;
            }

            /*if((AccessFlags.formatAccessFlagsForField(fieldOfInterest.getAccessFlags()).contains("public static") || AccessFlags.formatAccessFlagsForField(fieldOfInterest.getAccessFlags()).contains("protected static")) && possibleFieldRefList.contains(fieldOfInterest.toString()) && (fieldOfInterest.getType().contains("Ljava/lang/String") || fieldOfInterest.getType().endsWith("[C") || fieldOfInterest.getType().endsWith("[B") || fieldOfInterest.getType().endsWith("[I")  || fieldOfInterest.getType().endsWith("[S"))){
                fieldInvocationsOfCore.put(possibleFieldRefList.get(possibleFieldRefList.indexOf(fieldOfInterest.toString())),fieldOfInterest);
            }*/
            if((AccessFlags.formatAccessFlagsForField(fieldOfInterest.getAccessFlags()).contains("public static") || AccessFlags.formatAccessFlagsForField(fieldOfInterest.getAccessFlags()).contains("protected static")) && possibleFieldRefList.contains(fieldOfInterest.toString()) && (fieldOfInterest.getType().contains("Ljava/lang/String") )){
                fieldInvocationsOfCore.put(possibleFieldRefList.get(possibleFieldRefList.indexOf(fieldOfInterest.toString())),fieldOfInterest);
            }

            if((AccessFlags.formatAccessFlagsForField(fieldOfInterest.getAccessFlags()).contains("public static") || AccessFlags.formatAccessFlagsForField(fieldOfInterest.getAccessFlags()).contains("protected static")) && (fieldOfInterest.getType().endsWith("[C") || fieldOfInterest.getType().endsWith("[B") || fieldOfInterest.getType().endsWith("[I")  || fieldOfInterest.getType().endsWith("[S"))){
                this.numOfPossibleObfuscatedFieldsWithMethod++;
            }

        }


        return fieldInvocationsOfCore;
    }


    protected Map<String,Field> getFieldInvocationsOfCoreMap(){

        return fieldInvocationsOfCoreMap;
    }


    protected int getDeobfuscationType(SmaliUnit coreClass, DexFile dxFile, int posOfDeobRoutine,Map<String,CoreClassRef> relevantMethodReferencesOfCurrentCoreMap){
        int type = SmaliLayer.DEOBFUSCATION_TYPE_UNKNOWN;

        // for now this case is not considering the case, where we have an deobfuscation method without using the core
        if(relevantMethodReferencesOfCurrentCoreMap.size() == 0 || relevantMethodReferencesOfCurrentCoreMap == null || posOfDeobRoutine == -1){
            //System.out.println("IC hbin hier ....."+relevantMethodReferencesOfCurrentCoreMap.size());
            Map<String,Field> fieldInvocationsOfCore = getFieldsInvocationsOfCore(coreClass);
            type = SmaliLayer.DEOBFUSCATION_TYPE_FIELD;
            fieldInvocationsOfCoreMap.putAll(fieldInvocationsOfCore);
            return  type;
        }

        Map<String,Field> fieldInvocationsOfCore = getFieldsInvocationsOfCore(coreClass);


        //System.out.println("fieldInvocationsOfCore.size(): "+fieldInvocationsOfCore.size());
        /*
        for(String s : fieldInvocationsOfCore.keySet()){
            System.out.println("Werte: "+s);
        }*/
       // System.out.println("this.numOfPossibleObfuscatedFieldsWithMethod: "+this.numOfPossibleObfuscatedFieldsWithMethod);

        if(fieldInvocationsOfCore.size() == 0 || fieldInvocationsOfCore.isEmpty() || this.numOfPossibleObfuscatedFieldsWithMethod > fieldInvocationsOfCore.size()){
            int pos = posOfDeobRoutine;
            if(pos == -1){
                System.out.println("aaaaaaaaaaaaaaaaaaaa------------------aaaaaaaaaaaaaaaaaaaa");
                type = SmaliLayer.DEOBFUSCATION_TYPE_UNKNOWN;
            }else{
                type = SmaliLayer.DEOBFUSCATION_TYPE_METHOD;
                //CoreClassRef ref2DeobfucsationRoutine = getDeobfuscationRoutine(pos);
            }

        }else{
            type = SmaliLayer.DEOBFUSCATION_TYPE_FIELD;
            fieldInvocationsOfCoreMap.putAll(fieldInvocationsOfCore);
        }


        return type;
    }
}
