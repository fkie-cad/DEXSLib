package dexslib.core.slicing;

import com.google.common.collect.Lists;
import dexslib.SmaliLayer;
import dexslib.core.analyse.DeobfuscationMethod;
import dexslib.types.InstructionRef;
import dexslib.util.SmaliLayerUtils;
import dexslib.util.SpecialisedInstruction;
import org.jf.dexlib2.ReferenceType;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

public class BackwardSliceOnRegisterLevel {




    /**
     * When I identified an deobfuscation method which is not of type {@literal <}clinit{@literal >} it is easier to determine the strings which were put into the method
     *
     *
     * @return an ArrayList of ArrayList of Registers which are used as input for a given method
     */
    public static ArrayList<ArrayList<InstructionRef>> getAllMethodsRegs(Method deobfuscationMethod){


        ArrayList<ArrayList<InstructionRef>> toAnalyzeSpecialisedInstructionList = new ArrayList<ArrayList<InstructionRef>>();

        if(deobfuscationMethod.getImplementation() == null){
            return toAnalyzeSpecialisedInstructionList; // empty list == not possible to use this deob method
        }


        DexFile dexFile = SmaliLayer.getDexFile2Analyze();

        for(ClassDef classDef : dexFile.getClasses()){
            if(classDef.getType().startsWith("Landroid/support/") || classDef.getType().startsWith("Landroid/support/annotation/") || classDef.getType().contains("R$") || classDef.getType().startsWith("Lorg/apache/commons/") || classDef.getType().startsWith("Lokhttp3/")  || classDef.getType().startsWith("Lokio/Base64")){
                continue;
            }

            for(Method currentMethod : classDef.getMethods()){
                if( currentMethod.getImplementation() == null){
                    continue;
                }


                ArrayList<InstructionRef> tmpSpecialisedInstructionList = new ArrayList<InstructionRef>();

                for(Instruction currentInstruction : currentMethod.getImplementation().getInstructions()){
                    SpecialisedInstruction currentSpecialisedInstruction = new SpecialisedInstruction(currentInstruction,deobfuscationMethod.getImplementation().getRegisterCount(), deobfuscationMethod.getParameters().size());
                    InstructionRef instructionRef = new InstructionRef(classDef,currentMethod,  currentSpecialisedInstruction);

                    tmpSpecialisedInstructionList.add(instructionRef);
                    if(currentSpecialisedInstruction.isMethodInvocation()){
                        if(currentSpecialisedInstruction.getFullMethodName().equals(deobfuscationMethod.toString())){
                            // we found the invocation of the deobfuscation method

                            // this will print out the method which is using the deob-method
                            System.out.println("invoking Method: "+currentSpecialisedInstruction);
                            System.out.println("instruction as ref: "+currentSpecialisedInstruction.toString());
                            toAnalyzeSpecialisedInstructionList.add(tmpSpecialisedInstructionList);
                            tmpSpecialisedInstructionList = new ArrayList<InstructionRef>();
                        }
                    }
                }

            }


        }

        // each List of SpecialisedInstructionList contains as List of all Registers which has been used till the deobfuscationMethod was called
        return  toAnalyzeSpecialisedInstructionList;
    }


    public static String getInstructionOpcodeForReturnType(String returnType){
        switch (returnType) {
            case "[B":
                return "new-array";
            case "Ljava/lang/String;":
                return "const-string";
            default:
                return "null";


        }

    }


    private static String getConstOfMethod(SpecialisedInstruction specialisedInstruction){

        DexFile dexFile =  SmaliLayer.getDexFile2Analyze();
        for(ClassDef classDef : dexFile.getClasses()){
            if(classDef.toString().contains(SmaliLayerUtils.getCallingClassName(specialisedInstruction.getFullMethodName()))){
                System.out.println("Found class: "+classDef.toString());
                for(Method method : classDef.getMethods()){
                    if(method.getImplementation() == null){
                        continue;
                    }



                    if(method.toString().equals(specialisedInstruction.getFullMethodName())){
                        System.out.println("Found method: "+method.toString());

                        ArrayList instruction2Analyze =   Lists.newArrayList(method.getImplementation().getInstructions());
                        // we are only interested in the const which is returned
                        String register2Follow = "unknown";
                        int lastInstructionElement = instruction2Analyze.size() - 1;
                        for(int j = lastInstructionElement; j >= 0; j--){
                            SpecialisedInstruction currentSpecialisedInstruction = new SpecialisedInstruction(((Instruction) instruction2Analyze.get(j)), method.getImplementation().getRegisterCount(), method.getParameters().size());
                            if(j == lastInstructionElement){
                                register2Follow = currentSpecialisedInstruction.registerOfInstructionAsList().get(0);
                                System.out.println("looking for const value of register: "+register2Follow);
                                continue;
                            }

                            if(currentSpecialisedInstruction.getOpcode().setsRegister() && currentSpecialisedInstruction.getRegistersAsString().contains(register2Follow) ){
                                System.out.println("found const value");
                                System.out.println(currentSpecialisedInstruction);
                                System.out.println(currentSpecialisedInstruction.getValue());
                                break;
                            }


                        }



                    }
                    continue;



                }
            }else{
                continue;
            }
        }

        return null;
    }


    /**
     *
     * @param tmpSpecialisedInstructionList all the instructions till the method was invoked
     * @param regNum is reg we are searching for
     * @return the const value of a given register used as method parameter
     */
    private static String getConstValueFromReg(ArrayList<InstructionRef> tmpSpecialisedInstructionList, String regNum){

        boolean setsResultCase = false;
        boolean isNewReg = false;
        String newReg = "";
        int setsResultCaseCounter =0;
        ArrayList<String> newRegList = new ArrayList<>();

        System.out.println("Reg: "+regNum);
        // iterating in reverse order further away from the invoked method
        for (int j = tmpSpecialisedInstructionList.size() - 1; j >= 0; j--) {


            if(setsResultCase){
                newReg= "";
                System.out.println("smali: "+tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().toString());

                if(tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().getRegNumbers() == 0 && setsResultCaseCounter == 0) {
                    getConstOfMethod(tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction());
                    break; // if their is no method invocation with registers we are finished here
                }


                if(tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().isMethodInvocation()){
                    System.out.println("searching for method: "+tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().getFullMethodName());
                    getConstOfMethod(tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction());
                    System.out.println("-------------------------------          \n\n");
                }

                if(tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().getRegNumbers() == 1){
                    newReg = tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().getNthRegisterAsString(tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().getStartReg());


                }else if (tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().getRegNumbers() > 1){
                    newRegList = tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().registerOfInstructionAsList();
                    setsResultCaseCounter = tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().getRegNumbers();
                    setsResultCase = false;
                    /*
                    for(String nthReg : tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().registerOfInstructionAsList()){
                        //tmpSpecialisedInstructionList.remove(j);
                        //getConstValueFromReg(tmpSpecialisedInstructionList,nthReg);
                    }
                    //newRegList = tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().registerOfInstructionAsList();
                    for(String nthReg : tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().registerOfInstructionAsList()){

                        /*
                        if(tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().toString().contains(nthReg)){
                            System.out.println("smali: "+tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().toString());
                            if(tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().toString().contains("move-result")){
                                setsResultCase = true;

                            }
                            //System.out.println("do break;");
                            break;
                        }
                    }*/

                }

                //tmpSpecialisedInstructionList.remove(j);
                if(setsResultCaseCounter == 0)
                    setsResultCase = false;
                isNewReg = true;
                continue;
            }

            //System.out.println("after break;");
            if(isNewReg){

                if(newReg.length() > 0){
                    if(tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().toString().contains(newReg)){
                        System.out.println("smali: "+tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().toString());
                        if(tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().toString().contains("move-result")){
                            setsResultCase = true;

                        }
                    }
                    continue;
                }


                for(String nthReg : newRegList){
                    if(tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().toString().contains(nthReg)){
                        System.out.println("smali: "+tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().toString());
                        if(tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().toString().contains("move-result")){
                            newRegList.remove(nthReg);
                            setsResultCaseCounter--;
                            setsResultCase = true;
                            break;

                        }
                    }
                }

                //tmpSpecialisedInstructionList.remove(j);
                continue;

            }


            if(tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().toString().contains(regNum)){
                isNewReg = false;
                //System.out.println("smali: "+tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().toString());

                if(tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().getOpcode().setsRegister()){

                    if(tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().toString().contains("move-result"))
                        setsResultCase = true;
                    System.out.println("smali: "+tmpSpecialisedInstructionList.get(j).getSpecialisedInstruction().toString());
                    //tmpSpecialisedInstructionList.remove(j);
                    continue;
                }

            }



        }


        return null;
    }




    public static Map<InstructionRef,String> getObfuscatedStringsForDeobMethod(Method deobMethod, DeobfuscationMethod deobfuscationMethod){

        DexFile dexFile = SmaliLayer.getDexFile2Analyze();
        ArrayList<ArrayList<InstructionRef>> toAnalyzeSpecialisedInstructionList = new ArrayList<ArrayList<InstructionRef>>();
        int counter = 0;
        for(ClassDef classDef : dexFile.getClasses()) {
            if (classDef.getType().startsWith("Landroid/support/") || classDef.getType().startsWith("Landroid/support/annotation/") || classDef.getType().contains("R$") || classDef.getType().startsWith("Lorg/apache/commons/") || classDef.getType().startsWith("Lokhttp3/") || classDef.getType().startsWith("Lokio/Base64")) {
                continue;
            }

            for(Method currentMethod : classDef.getMethods()){


                if(currentMethod.getImplementation() == null){
                    continue;
                }

                ArrayList<InstructionRef> tmpSpecialisedInstructionList = new ArrayList<InstructionRef>();

                for(Instruction currentInstruction : currentMethod.getImplementation().getInstructions()) {
                    SpecialisedInstruction currentSpecialisedInstruction = new SpecialisedInstruction(currentInstruction, deobMethod.getImplementation().getRegisterCount(), deobMethod.getParameters().size());
                    InstructionRef instructionRef = new InstructionRef(classDef,currentMethod,  currentSpecialisedInstruction);
                    /*if(currentSpecialisedInstruction.hasValue()){
                        System.out.println("value: "+currentSpecialisedInstruction.getValue());
                        System.out.println("fullMethodName: "+deobfuscationMethod.getFullMethodName());
                    }*/

                    tmpSpecialisedInstructionList.add(instructionRef);

                    if(currentSpecialisedInstruction.isMethodInvocation()){
                        if(currentSpecialisedInstruction.getFullMethodName().contains("Lcom/google/android/gms/internal/zzal;->zzb(")){
                            counter++;
                            System.out.println("instruction toString(): "+currentSpecialisedInstruction.toString());
                            System.out.println("instruction method name: "+currentSpecialisedInstruction.getFullMethodName());
                            System.out.println("deobfullMethodName: "+deobfuscationMethod.getFullMethodName());
                            System.out.println("counter = "+counter);
                            toAnalyzeSpecialisedInstructionList.add(tmpSpecialisedInstructionList);
                            tmpSpecialisedInstructionList = new ArrayList<InstructionRef>();

                        }

                    }

                    /*
                    if(currentSpecialisedInstruction.isMethodInvocation() && currentSpecialisedInstruction.getFullMethodName().equals(deobfuscationMethod.getFullMethodName())){
                        System.out.println("invoking Method: "+currentSpecialisedInstruction);
                        System.out.println("instruction as ref: "+currentSpecialisedInstruction.toString());

                    }*/

                }



            }


        }

        System.out.println("\n\n\n\n\n\n-----------------------------------------------------------------------------------------------------------------------------------------------");

        for(ArrayList<InstructionRef> tmpList : toAnalyzeSpecialisedInstructionList){
            //for(InstructionRef tmpRef : tmpList){
               // System.out.println(tmpRef.toString());
                int regCount = tmpList.get(tmpList.size()-1).getSpecialisedInstruction().getRegNumbers();
            System.out.println("Deobfuscation invocation: "+tmpList.get(tmpList.size()-1).getSpecialisedInstruction().toString());
                System.out.println("We have "+regCount+" registers used for the deobfuscation method");
                BackwardSliceOnRegisterLevel.getConstValueFromReg(tmpList, tmpList.get(tmpList.size()-1).getSpecialisedInstruction().getNthRegisterAsString(1));
                for(int i = 2; i <= regCount;i++){
                    BackwardSliceOnRegisterLevel.getConstValueFromReg(tmpList, tmpList.get(tmpList.size()-1).getSpecialisedInstruction().getNthRegisterAsString(i));
                }
                //System.out.println(tmpRef.getSpecialisedInstruction().toString());


           // }
            break;

        }

        System.out.println("\n\n\n\n\n\n-----------------------------------------------------------------------------------------------------------------------------------------------");

        return null;
    }


    /**
     * If the deobfuscation method don't get an obfucasted String as input we need to use another form
     * @param tmpSpecialisedInstructionList
     * @return an Map which contains the obfuscatedStrings and the corresponding Deobfuscation Method with its exact position (InstructionRef); Return null if
     *
     * For now an simplified version - if it works --> we have to expand it for method --> this can be done with method.
     *
     */
    public static Map<InstructionRef,String> getObfuscatedStringsForDeobMethod(ArrayList<InstructionRef> tmpSpecialisedInstructionList){
        //Map<String,InstructionRef> obfuscatedStringsForDeobMethodMap = new HashMap<String,InstructionRef>();
        Map<InstructionRef,String> obfuscatedStringsForDeobMethodMap = new HashMap<InstructionRef,String>();

        if(tmpSpecialisedInstructionList == null || tmpSpecialisedInstructionList.isEmpty()){
            return null;
        }

        // the last element of this ArrayList contains always the deobfuscation method
        InstructionRef deobfuscationMethodRef = tmpSpecialisedInstructionList.get(tmpSpecialisedInstructionList.size() -1);

        ArrayList<String> deobfuscationRegister2FollowList = deobfuscationMethodRef.getSpecialisedInstruction().registerOfInstructionAsList();



        //deobfuscationMethodRef.getSpecialisedInstruction().getRegister(); starts by 1

        ListIterator<InstructionRef> it = tmpSpecialisedInstructionList.listIterator(tmpSpecialisedInstructionList.size());

        // for now we don't consider the case, when the string is received as an result of another method
        // further more we have to consider the case, that our deobfusation method has more parameters
        while (it.hasPrevious()) {
            InstructionRef currentInstructionRef = it.previous();
            if(currentInstructionRef.getSpecialisedInstruction().getReferenceType() == ReferenceType.STRING){
                for(String nthReg : deobfuscationRegister2FollowList){
                    if(deobfuscationMethodRef.getSpecialisedInstruction().getRegistersAsString().contains(nthReg)){
                        String obfuscatedString = currentInstructionRef.getSpecialisedInstruction().getValue();
                        //obfuscatedStringsForDeobMethodMap.put(obfuscatedString,deobfuscationMethodRef);
                        obfuscatedStringsForDeobMethodMap.put(deobfuscationMethodRef,obfuscatedString);
                        return obfuscatedStringsForDeobMethodMap;
                    }

                }
            }

        }



        return  obfuscatedStringsForDeobMethodMap;
    }



    /**
     * DeobfuscationParameters
     *
     * If the deobfuscation method don't get an obfucasted String as input we need to use another form
     * @param tmpSpecialisedInstructionList
     *  seconded paramter give more infos about the obfuscation Type
     * @return an Map which contains the obfuscatedStrings in another Form  and the corresponding Deobfuscation Method with its exact position (InstructionRef); Return null if
     * The InstructionRef itself has the possibility to get us the invocated method call
     *
     */
    public static Map<InstructionRef,MethodParameters> getObfuscatedParamtersForDeobMethod(ArrayList<InstructionRef> tmpSpecialisedInstructionList){
        Map<InstructionRef, MethodParameters> obfuscatedStringsForDeobMethodMap = new HashMap<InstructionRef,MethodParameters>();

        if(tmpSpecialisedInstructionList == null || tmpSpecialisedInstructionList.isEmpty()){
            return null;
        }

        // the last element of this ArrayList contains always the deobfuscation method
        InstructionRef deobfuscationMethodRef = tmpSpecialisedInstructionList.get(tmpSpecialisedInstructionList.size() -1);

        ArrayList<String> deobfuscationRegister2FollowList = deobfuscationMethodRef.getSpecialisedInstruction().registerOfInstructionAsList();

        // Now we have to fill the values of the parameters
        MethodParameters methodParametersOfDeobfuscationMethod = new MethodParameters(deobfuscationMethodRef.getMethod().getParameters());

        //deobfuscationMethodRef.getSpecialisedInstruction().getRegister(); starts by 1

        // this List contains all Instruction (InstructionRef's) which has beend executed till the invocation of the given Method (deobfuscationMethodRef)
        ListIterator<InstructionRef> it = tmpSpecialisedInstructionList.listIterator(tmpSpecialisedInstructionList.size());

        // ich brauche nur zu prüfen, ob das Register gesetzt wird  this.getOpcode().setsRegister() --> für Strings kann ich immer noch den ReferenceType= STRING machen

        while (it.hasPrevious()) {
            int methodParamCount = 0; // for each instruction we have to reset the methodParamCount
            InstructionRef currentInstructionRef = it.previous();

            //

                // we test if the current instruction is setting/assigning a value to the register
                if(currentInstructionRef.getSpecialisedInstruction().getOpcode().setsRegister() || currentInstructionRef.getSpecialisedInstruction().getOpcode().setsWideRegister()){

                    // is this register used for the deob method --> the first Register is also the first parameter
                    // therefore we  iterate over each parameter of the deobfuscation method implicitly
                    ListIterator<String> listIterator = deobfuscationRegister2FollowList.listIterator();
                    while(listIterator.hasNext()){
                        String nthReg = listIterator.next();

                        if(deobfuscationMethodRef.getSpecialisedInstruction().getRegistersAsString().contains(nthReg)){
                            // this register is already used by this method
                            listIterator.remove();

                            String obfuscatedString = currentInstructionRef.getSpecialisedInstruction().getValueWithoutExcapedString();
                            methodParametersOfDeobfuscationMethod.setNthMethodParameterValue(methodParamCount,obfuscatedString);
                            //System.out.println("Value at level: "+methodParamCount);
                            obfuscatedStringsForDeobMethodMap.put(deobfuscationMethodRef,methodParametersOfDeobfuscationMethod);
                        }
                        methodParamCount++;

                    }

                   /* for(String nthReg : deobfuscationRegister2FollowList){
                        if(deobfuscationMethodRef.getSpecialisedInstruction().getRegistersAsString().contains(nthReg)){
                            // this register is already used by this method

                            deobfuscationRegister2FollowList.removeIf(b -> b.equals(nthReg));
                            //deobfuscationRegister2FollowList.remove(deobfuscationRegister2FollowList.indexOf(nthReg));
                            String obfuscatedString = currentInstructionRef.getSpecialisedInstruction().getValue();
                            methodParametersOfDeobfuscationMethod.setNthMethodParameterValue(methodParamCount,obfuscatedString);
                            System.out.println("Value at level: "+methodParamCount);
                            obfuscatedStringsForDeobMethodMap.put(deobfuscationMethodRef,methodParametersOfDeobfuscationMethod);
                        }
                     methodParamCount++;
                    }*/
                }


            /*
            if(currentInstructionRef.getSpecialisedInstruction().getReferenceType() == ReferenceType.STRING){
                for(String nthReg : deobfuscationRegister2FollowList){
                    if(deobfuscationMethodRef.getSpecialisedInstruction().getRegistersAsString().contains(nthReg)){
                        String obfuscatedString = currentInstructionRef.getSpecialisedInstruction().getValue();
                        //obfuscatedStringsForDeobMethodMap.put(obfuscatedString,deobfuscationMethodRef);
                        obfuscatedStringsForDeobMethodMap.put(deobfuscationMethodRef,methodParametersOfDeobfuscationMethod);


                    }

                }
            }*/

        }




        return  obfuscatedStringsForDeobMethodMap;
    }
}



