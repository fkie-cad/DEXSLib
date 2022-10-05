package dexslib.util;

import dexslib.ClassInitializerFilter;
import dexslib.types.SmaliUnit;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.ReferenceType;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.immutable.ImmutableDexFile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by Daniel Baier on 03.01.17.
 */
public class TemplateUtils {

    /**
     *
     * removeClasses removes from the dexfile and the defined coreClassDescriptors, all not used classes.
     *
     * @param dexfile
     * @param globalCoreClassList
     * @param relevantClassesSetMethod
     * @return
     */
    public static DexFile removeClasses(DexFile dexfile, List<String> globalCoreClassList,LinkedHashSet<String> relevantClassesSetMethod){
        // when we remove classes from the DEX, it is like recreating an DEX with a List of Classes we want to keep.
        // Therefore this List contains all classes we dont want to to remove
        List<ClassDef> classes2KeepInDex = new ArrayList<ClassDef>();

        if(dexfile == null) {
            return null;
        }

        for(ClassDef classDef : dexfile.getClasses()){
            if (relevantClassesSetMethod.contains(classDef.getType()) && !classDef.getType().startsWith("Landroid/support")) {
                classes2KeepInDex.add(classDef);
                continue;
            }

            // classDef.getType()
            // .equals(coreClassDescriptor)
            if(classDef.toString().startsWith("Lcom/obfuscation/defuscadotemplate/Core") || globalCoreClassList.contains(classDef.getType())){
                classes2KeepInDex.add(classDef);
                continue;
            }

        }




        dexfile = new ImmutableDexFile(Opcodes.getDefault(),classes2KeepInDex);

        return dexfile;
    }



    /**
     * removeClasses removes from the dexfile and the defined coreClassDescriptor, all not used classes.
     *
     * @param dexfile
     * @param coreClassDescriptor
     * @param relevantClassesSetMethod
     * @return
     */
    public static DexFile removeClasses(DexFile dexfile, String coreClassDescriptor,LinkedHashSet<String> relevantClassesSetMethod){
        // when we remove classes from the DEX, it is like recreating an DEX with a List of Classes we want to keep.
        // Therefore this List contains all classes we dont want to to remove
        List<ClassDef> classes2KeepInDex = new ArrayList<ClassDef>();

        if(dexfile == null) {
            return null;
        }

        for(ClassDef classDef : dexfile.getClasses()){
            if (relevantClassesSetMethod.contains(classDef.getType()) && !classDef.getType().startsWith("Landroid/support")) {
                classes2KeepInDex.add(classDef);
                continue;
            }


            if(classDef.toString().equals("Lcom/obfuscation/defuscadotemplate/Core;") || classDef.getType().equals(coreClassDescriptor)){
                classes2KeepInDex.add(classDef);
                continue;
            }

        }




        dexfile = new ImmutableDexFile(Opcodes.getDefault(),classes2KeepInDex);

        return dexfile;
    }


    /**
     *
     * @param instruction we want to analyze for call of {@literal <}init{@literal >} from a implemented class
     * @return
     */
    public static boolean isInitCallFromCore(Instruction instruction,SmaliUnit classSmaliUnit){
        String callingRef = ((ReferenceInstruction) instruction).getReference().toString();
        Opcode instructionOpcode = instruction.getOpcode();

        if(!CoreUtils.isImplemented(callingRef)){
            return false;
        }

        if(callingRef.contains("<init>")){
            //System.out.println("core:"+classSmaliUnit.getClassDefinition().getType() +"init: "+callingRef);
            //return true;
        }

        return false;
    }


    public static Iterable<? extends Instruction> removeInitMethodCallsOfCore(Iterable<? extends Instruction> analyzedInstructions){
        List<Instruction> instructionsWithoutNativeCallsList = new ArrayList<Instruction>();




            /*
        List<AnalyzedInstruction> analyzedInstructions = methodAnalyzer.getAnalyzedInstructions();
        int currentCodeAddress = 0;
        for (int i=0; i<analyzedInstructions.size(); i++) {
            AnalyzedInstruction instruction = analyzedInstructions.get(i);


        }*/

        boolean wasPrevCallNative = false;

        for(Instruction instruction : analyzedInstructions){

            Opcode instructionOpcode = instruction.getOpcode();
            if(instructionOpcode.setsResult() || instructionOpcode.referenceType == ReferenceType.METHOD) {

                String callingRef = ((ReferenceInstruction) instruction).getReference().toString();
                /*if (CoreUtils.isNativeCall(callingRef, nativeMethodNameOfCoreInstanceList)) {
                    wasPrevCallNative = true;
                    continue;
                }else{
                    wasPrevCallNative = false;
                }*/
            }else if(wasPrevCallNative && instructionOpcode.setsRegister()){
                wasPrevCallNative = false;
                continue;
            }else{
                wasPrevCallNative = false;
            }


            instructionsWithoutNativeCallsList.add(instruction);
        }


        return new Iterable<Instruction>() {
            @Override
            public Iterator<Instruction> iterator() {
                return  instructionsWithoutNativeCallsList.iterator();
            }
        };



    }



    public static Iterable<? extends Instruction> removeNativeMethodCalls(Iterable<? extends Instruction> analyzedInstructions,List<String> nativeMethodNameOfCoreInstanceList){
        List<Instruction> instructionsWithoutNativeCallsList = new ArrayList<Instruction>();




            /*
        List<AnalyzedInstruction> analyzedInstructions = methodAnalyzer.getAnalyzedInstructions();
        int currentCodeAddress = 0;
        for (int i=0; i<analyzedInstructions.size(); i++) {
            AnalyzedInstruction instruction = analyzedInstructions.get(i);


        }*/

        boolean wasPrevCallNative = false;

        for(Instruction instruction : analyzedInstructions){

            Opcode instructionOpcode = instruction.getOpcode();
            if(instructionOpcode.setsResult() || instructionOpcode.referenceType == ReferenceType.METHOD) {

                String callingRef = ((ReferenceInstruction) instruction).getReference().toString();
                if (CoreUtils.isNativeCall(callingRef, nativeMethodNameOfCoreInstanceList)) {
                    wasPrevCallNative = true;
                    continue;
                }else{
                    wasPrevCallNative = false;
                }
            }else if(wasPrevCallNative && instructionOpcode.setsRegister()){
                wasPrevCallNative = false;
                continue;
            }else{
                wasPrevCallNative = false;
            }


            instructionsWithoutNativeCallsList.add(instruction);
        }


        return new Iterable<Instruction>() {
            @Override
            public Iterator<Instruction> iterator() {
                return  instructionsWithoutNativeCallsList.iterator();
            }
            };



    }



    public static MutableMethodImplementation removeNativeMethodCalls(Method originMethod, List<String> nativeMethodNameOfCoreInstanceList){
       boolean wasPrevCallNative = false;
        MutableMethodImplementation mutableImplementation = new MutableMethodImplementation(originMethod.getImplementation());

        List<BuilderInstruction> analyzedInstructions = mutableImplementation.getInstructions();

        for(Instruction instruction : analyzedInstructions){

            Opcode instructionOpcode = instruction.getOpcode();
            if(instructionOpcode.setsResult() || instructionOpcode.referenceType == ReferenceType.METHOD) {

                String callingRef = ((ReferenceInstruction) instruction).getReference().toString();
                if (CoreUtils.isNativeCall(callingRef, nativeMethodNameOfCoreInstanceList)) {
                    wasPrevCallNative = true;
                    mutableImplementation.removeInstruction(analyzedInstructions.indexOf(instruction));
                    continue;
                }else{
                    wasPrevCallNative = false;
                }
            }else if(wasPrevCallNative && instructionOpcode.setsRegister()){
                mutableImplementation.removeInstruction(analyzedInstructions.indexOf(instruction));
                wasPrevCallNative = false;
                continue;
            }else{
                wasPrevCallNative = false;
            }

        }


        return mutableImplementation;



    }



    public static ClassDef getClassDefByName(String className){

        for(ClassDef classDefOfInteresst : ClassInitializerFilter.getListOfFilteredClinitClasses()) {
            if(classDefOfInteresst.getType().equals(className)){
                return classDefOfInteresst;
            }
        }
        return null;
    }

}
