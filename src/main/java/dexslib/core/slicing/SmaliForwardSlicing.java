package dexslib.core.slicing;

import dexslib.core.analyse.DeobfuscationMethod;
import dexslib.types.InstructionRef;
import dexslib.util.SmaliLayerUtils;
import dexslib.util.SpecialisedInstruction;
import org.jf.baksmali.Adaptors.ClassDefinition;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.ReferenceType;
import org.jf.dexlib2.analysis.*;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;

import java.util.ArrayList;

import static java.lang.Boolean.FALSE;

/*
https://github.com/JesusFreke/smali/wiki/Registers
 */

public class SmaliForwardSlicing {


    public static ClassDefinition classDef;

    private static ArrayList<InstructionRef>  listSlicedMethods = new ArrayList<InstructionRef>();




    public static ArrayList<String> followInstructionByRegister(InstructionRef instructionRef2analyze){
        ArrayList<String> register2FollowList = instructionRef2analyze.getSpecialisedInstruction().registerOfInstructionAsList();
        ArrayList<String> smaliSliceList = new ArrayList<String>();



        boolean beginSlicing = false;

        if(instructionRef2analyze.getMethodImplementation() != null){

            for(Instruction instruction : instructionRef2analyze.getMethodImplementation().getInstructions()) {
                SpecialisedInstruction currentSpecialisedInstruction = new SpecialisedInstruction(instruction,instructionRef2analyze.getMethodImplementation().getRegisterCount(),instructionRef2analyze.getMethodParameterCount());

                if(currentSpecialisedInstruction.toString().equals(instructionRef2analyze.getSpecialisedInstruction().toString())){
                    beginSlicing = true;
                    continue;
                }

                if(beginSlicing){
                    for(String nthReg : register2FollowList){
                        if(currentSpecialisedInstruction.getRegistersAsString().contains(nthReg)){
                            smaliSliceList.add(currentSpecialisedInstruction.toString());
                        }
                    }

                }else{
                    continue;
                }



            }


        }else{
            System.err.println("DEXLib2 error: no method implementation could be created!");
        }

        return smaliSliceList;
    }

    /**
     * More or less it just follow the data flow of the instruction without any logic
     *
     *
     * @param instructionRef2analyze
     */
    public static void doSliceInstruction(InstructionRef instructionRef2analyze){

        System.out.println("start building with forward slice: "+instructionRef2analyze.getSpecialisedInstruction().toString());

        ArrayList<String> smaliSliceList = followInstructionByRegister(instructionRef2analyze);


        System.out.println("start evaluating slice for "+instructionRef2analyze.getSpecialisedInstruction().getOpcode().name+" "+instructionRef2analyze.getSpecialisedInstruction().getRegistersAsString());
        for(String slicedSmaliLine: smaliSliceList){
            System.out.println(slicedSmaliLine);
        }
        System.out.println("\n");


    }


    /**
     *
     * It follows an instruction till a new register is set
     *
     * @param instructionRef2analyze
     */
    public static void doSliceInstructionWithCriteria(InstructionRef instructionRef2analyze){
        System.out.println("start building forward slice with criteria: "+instructionRef2analyze.getSpecialisedInstruction().toString());


        ArrayList<String> smaliSliceList = followInstructionTillNewRegisterIsSet(instructionRef2analyze);


        System.out.println("start evaluating slice for "+instructionRef2analyze.getSpecialisedInstruction().getOpcode().name+" "+instructionRef2analyze.getSpecialisedInstruction().getRegistersAsString());
        for(String slicedSmaliLine: smaliSliceList){
            System.out.println(slicedSmaliLine);
        }
        System.out.println("\n");


    }


    public static ArrayList<String> followInstructionTillNewRegisterIsSet(InstructionRef instructionRef2analyze){
        ArrayList<String> register2FollowList = instructionRef2analyze.getSpecialisedInstruction().registerOfInstructionAsList();
        ArrayList<String> smaliSliceList = new ArrayList<String>();



        boolean beginSlicing = false;
        boolean noNewRegister = true;
        boolean first=true;

        SpecialisedInstruction lastSmaliLine= null;

        if(instructionRef2analyze.getMethodImplementation() != null){

            for(Instruction instruction : instructionRef2analyze.getMethodImplementation().getInstructions()) {
                SpecialisedInstruction currentSpecialisedInstruction = new SpecialisedInstruction(instruction,instructionRef2analyze.getMethodImplementation().getRegisterCount(),instructionRef2analyze.getMethodParameterCount());

                if(currentSpecialisedInstruction.toString().equals(instructionRef2analyze.getSpecialisedInstruction().toString())){
                    beginSlicing = true;
                    continue;
                }

                if(beginSlicing && noNewRegister){



                   for(String nthReg : register2FollowList){
                        if(currentSpecialisedInstruction.getRegistersAsString().contains(nthReg)){
                            smaliSliceList.add(currentSpecialisedInstruction.toString());
                        }
                        if(first){
                            lastSmaliLine  = currentSpecialisedInstruction;
                            first = false; //
                        }
                    }

                    if(currentSpecialisedInstruction.getOpcode().name.startsWith("move-result") && lastSmaliLine.getOpcode().setsResult()){
                        for(String nthReg : register2FollowList){
                            if(lastSmaliLine.getRegistersAsString().contains(nthReg)){
                                smaliSliceList.add(currentSpecialisedInstruction.toString());
                                noNewRegister = false;
                                return smaliSliceList;
                            }
                        }
                    }

                    lastSmaliLine  = currentSpecialisedInstruction;

                }else{
                    continue;
                }



            }


        }else{
            System.err.println("DEXLib2 error: no method implementation could be created!");
        }


        return smaliSliceList;

    }





    /*



     hier teste ich zunaechst die Möglichkeit eine Instruktion zu folgen.

     Dabei kann ich eine instruktion in Abhänhigkeit dessen Register verfolgen



    verfolgen folgender Zeile in MainAcitivity

    in classe: MainActivity bzw. Lcom/example/obfuscator/customstringobfuscator/MainActivity;
    in der methode: .method protected onCreate(Landroid/os/Bundle;)V

    const-string v7, "guvf vf zl frperpg zrffntr...abobql pna ernq guvf unununun"

    in klasse public Lturs/rickertsit/f;
    in methode
    .method public static a(Landroid/content/Context;Ljava/lang/String;J)V

    const-string v0, "\u0005*\u00054\t"


    der Zeile bzw. dem Register soll so lange gefolgt werden, bis dieses Register einer Methode übergeben wird, welche
    als Rückgabe vom Typ Ljava/lang/String; oder StringBuilder oder so ähnlich ist <-- das ist eigentlich schon analyse


    eigentlich müsste ich das viel generischer halten.
    Optional kann man ggfs. ein  Abbruch-Kriterium definieren, ansonsten sollte  das Register so lange verfolgt werden, bis dies erneut beschrieben wird

     */

    public static void testInstructionRef(DexFile dx, String register) throws Exception{

        BaksmaliOptions options = new BaksmaliOptions();
        boolean mBakDeb = false;

        // options
        options.deodex = false;
        options.implicitReferences = false;
        options.parameterRegisters = true;
        options.localsDirective = true;
        options.sequentialLabels = true;
        options.debugInfo = mBakDeb;
        options.codeOffsets = false;
        options.accessorComments = false;
        options.registerInfo = 0;
        options.inlineResolver = null;

        boolean isArt = false;

        if (isArt) {
            options.classPath = new ClassPath(new ArrayList<ClassProvider>(), true, 56);
        } else {
            options.classPath = new ClassPath();
        }


        //InlineMethodResolver.createInlineMethodResolver(35);

         //MethodAnalyzer(ClassPath classPath, Method method, InlineMethodResolver inlineResolver, boolean normalizeVirtualMethods);
         //AnalyzedInstruction(MethodAnalyzer methodAnalyzer, Instruction instruction, int instructionIndex, int registerCount);




        for(ClassDef tmpClassDef : dx.getClasses()){



            if(tmpClassDef.getType().startsWith("Landroid/support/") || tmpClassDef.getType().startsWith("Landroid/support/annotation/") || tmpClassDef.getType().contains("R$")){
                continue;
            }


            if(tmpClassDef.getType().contains("rickertsit/f") || tmpClassDef.getType().contains("MainActivity") || tmpClassDef.getType().contains("CustomStringObfuscation")){

                System.out.println("\n\nstart analysing class : "+tmpClassDef.getType());



            for(Method method : tmpClassDef.getMethods()){


                // method.getImplementation().getInstructions();
                MethodImplementation methodImplementation = method.getImplementation();
                if(methodImplementation == null){
                    //System.err.println("Error while analyzing a method.");
                    continue;

                }




                classDef = new ClassDefinition(options,tmpClassDef);


                boolean analyzerTest = true;

                MethodAnalyzer methodAnalyzer = new MethodAnalyzer(classDef.options.classPath, method,
                        classDef.options.inlineResolver, classDef.options.normalizeVirtualMethods);






                boolean registerFollowing = false;
                if(analyzerTest){


                    for(AnalyzedInstruction am : methodAnalyzer.getAnalyzedInstructions()) {
                        String tmpLocalRegister = "null";

                        //if(am.setsRegister()){
                        if(am.getSetRegisters().isEmpty() == FALSE){

                            tmpLocalRegister = "v" + am.getDestinationRegister();
                        }

                        Opcode instructionOpcode = am.getInstruction().getOpcode();

                        if (instructionOpcode.setsResult()) {



                            // System.out.println("instructionOpcode.name :" + instructionOpcode.name);
                            //System.out.println("with instruction index: "+am.getInstructionIndex());

                            // System.out.println("is written to Register v"+am.getDestinationRegister()+"\n");


                            if (instructionOpcode.name.equals("invoke-virtual/range")) {

                                System.out.println("-----------------------------------------------------------------------\n\n");


                                //if (register.equals(tmpLocalRegister)) {
                                    SpecialisedInstruction specialisedInstruction = new SpecialisedInstruction(am.getOriginalInstruction(),methodImplementation.getRegisterCount(), method.getParameters().size());

                                    System.out.println("instructionOpcode.name :" + instructionOpcode.name);

                                if(am.getSetRegisters().isEmpty() == FALSE){
                                    System.out.println("is written to Register v" + am.getDestinationRegister() + "\n");
                                }
                                    System.out.println("count: "+am.getRegisterCount());
                                    if (am.getInstruction().getOpcode().referenceType != ReferenceType.NONE) {
                                        String callingRef = ((ReferenceInstruction) am.getInstruction()).getReference().toString();

                                        System.out.println("callingRef :" + callingRef);
                                    }
                                    registerFollowing = true;
                                    System.out.println("Instruction Index: "+am.getInstructionIndex());
                                    System.out.println("with instruction address: " + methodAnalyzer.getInstructionAddress(am));
                                    System.out.println("Register v"+specialisedInstruction.getRegister(1));

                                   // if(instructionOpcode.setsWideRegister()){
                                        System.out.println("\"Register v"+specialisedInstruction.getRegister(specialisedInstruction.getStartReg()));
                                        System.out.println("\" starrtREgVal="+specialisedInstruction.getStartReg());
                                        System.out.println("\" count="+specialisedInstruction.getRegNumbers());
                                        System.exit(0);
                                   // }
                                //}
                                //continue;

                            }









                               /* for(AnalyzedInstruction am2 : am.getSuccessors()){
                                    Opcode instructionOpcode2 = am2.getInstruction().getOpcode();
                                    System.out.println("2instructionOpcode.name :" + instructionOpcode2.name);

                                    if (am2.getInstruction().getOpcode().referenceType != ReferenceType.NONE) {
                                        String callingRef = ((ReferenceInstruction) am2.getInstruction()).getReference().toString();

                                        System.out.println("callingRef :" + callingRef);
                                    }
                                    //System.out.println("with instruction index: "+am.getInstructionIndex());

                                    //System.out.println("2is written to Register v"+am2.getDestinationRegister()+"\n");

                                }
                                System.exit(0);

                                System.out.println("-----------------------------------------------------------------------\n\n");
                            }




                        }*/

                        }


                        //if (registerFollowing && register.equals(tmpLocalRegister)) {
                        if (registerFollowing) {
                            SpecialisedInstruction specialisedInstruction = new SpecialisedInstruction(am.getOriginalInstruction(),methodImplementation.getRegisterCount(), method.getParameters().size());
                            System.out.println("instructionOpcode.name :" + instructionOpcode.name);

                            if(am.getSetRegisters().isEmpty() == FALSE){
                                System.out.println("is written to Register v" + am.getDestinationRegister() + "\n");
                            }

                            System.out.println("count: "+am.getRegisterCount());
                            System.out.println("Instruction Index: "+am.getInstructionIndex());
                            if (am.getInstruction().getOpcode().referenceType != ReferenceType.NONE) {
                                String callingRef = ((ReferenceInstruction) am.getInstruction()).getReference().toString();

                                System.out.println("callingRef :" + callingRef);
                            }
                            System.out.println("with instruction address: " + methodAnalyzer.getInstructionAddress(am));
                            System.out.println("***************************************************************************************\n\n");
                            System.out.println("Register v"+specialisedInstruction.getRegister(1));
                        }else{

                            if(registerFollowing){


                                /*
                                ((OneRegisterInstruction)instruction).getRegisterA(); --> hiermit wird normalreweise der Register-Wert bestimmt,
                                daher brauche ich nun eine Möglichkeit zu bestimmen, ob es sich um eine one,two.. RegisterInstruciton handelt


                                 */





                                if (am.getInstruction().getOpcode().referenceType != ReferenceType.NONE) {
                                    String callingRef = ((ReferenceInstruction) am.getInstruction()).getReference().toString();

                                    System.out.println("|callingRef :| " + callingRef);
                                }
                                SpecialisedInstruction specialisedInstruction = new SpecialisedInstruction(am.getOriginalInstruction(),methodImplementation.getRegisterCount(), method.getParameters().size());
                                System.out.println("|count:| "+am.getRegisterCount());
                                System.out.println("Debug: register="+register+"; tmpLocalRegister="+tmpLocalRegister);
                                System.out.println("|Register v"+specialisedInstruction.getRegister(1)+"|");
                                System.out.println("|instructionOpcode.name| :" + instructionOpcode.name);
                                System.out.println("|Instruction Index:| "+am.getInstructionIndex());

                                /*System.out.println(instructionOpcode.referenceType);
                                System.out.println(instructionOpcode.referenceType2);
                                System.out.println(instructionOpcode.flags);
                                System.out.println(am.getInstruction().getCodeUnits());
                                System.out.println(am.getInstruction().toString());
                                System.out.println(am.getOriginalInstruction().toString());
                                System.out.println(am.getOriginalInstruction().getCodeUnits());

                                //System.out.println("|is written to Register | v" +  + "\n");
                                System.out.println("|regCount:| ="+am.getRegisterCount());*/
                            }

                        }



                    }
                   /* for(Instruction instruction : methodAnalyzer.getInstructions()){
                        Opcode instructionOpcode = instruction.getOpcode();
                        System.out.println("instructionOpcode.name :" + instructionOpcode.name);
                        System.out.println("-----------------------------------------------------------------------\n\n");
                    }*/

                }else {

                    for (Instruction instruction : methodImplementation.getInstructions()) {
                        //if(instruction.)


                        //System.out.println("instruction.getOpcode() :"+instruction.getOpcode()); --> Instruktion in großbuchstaben

                        Opcode instructionOpcode = instruction.getOpcode();


                        System.out.println("instructionOpcode.name :" + instructionOpcode.name);
                        System.out.println("instructionOpcode.name() :" + instructionOpcode.name());
                        //   System.out.println("instructionOpcode.toString() :"+instructionOpcode.toString()); groß
                        System.out.println("instructionOpcode.referenceType :" + instructionOpcode.referenceType);
                        if (instruction.getOpcode().referenceType != ReferenceType.NONE) {
                            String callingRef = ((ReferenceInstruction) instruction).getReference().toString();

                            System.out.println("callingRef :" + callingRef);
                        }

                        System.out.println("-----------------------------------------------------------------------\n\n");


                    }
                }
            }

            }
        }
    }


    public static Method getMethodByName(DexFile dexFile, String fullMethodName){
        for (ClassDef classDef : dexFile.getClasses()) {
            if(classDef.toString().equals(SmaliLayerUtils.getCallingClassName(fullMethodName))){
                for(Method method : classDef.getMethods()){
                    if(method.toString().equals(fullMethodName)){
                        return method;
                    }
                }

            }else{
                continue;
            }
        }
        return null;
    }


    /**
     *
     *  This methods returns the ClassDef of the {@literal <}Method{@literal >}(){@literal <}ReturnValue{@literal >}
     *
     *  Immer wenn ich eine Methode identifiziere, welche z.B. statisch einen obfuskierten String zurückgibt, dann möchte ich nun alle Aufrufe dieser Methode im Binary sehen
     *
     * @param dexFile
     * @param fullMethod corresponds to the result of method.toString()
     *
     *
     *                   getMetodWhichUsesMethodsResult
     */
    public static DeobfuscationMethod getMethodsInvocationsOfMethod(DexFile dexFile, String fullMethod){
        System.out.println("\n");
        boolean followInstruction = false;
        for (ClassDef classDef : dexFile.getClasses()) {

            //if(classDef.getType().startsWith("Landroid/support/v4/") || classDef.getType().startsWith("Landroid/support/v7/") || classDef.getType().startsWith("Landroid/support/annotation/") || classDef.getType().contains("R$")){
            // || classDef.getType().equals(SmaliLayerUtils.getCallingClassName(fullMethod))
            if(classDef.getType().startsWith("Landroid/support/v") || classDef.getType().startsWith("Landroid/support/annotation/") || classDef.getType().contains("R$") ){
                continue;
            }

            for(Method method : classDef.getMethods()){
                if(method.getImplementation() == null || method.toString().equals(fullMethod)){
                    continue;
                }

                for(Instruction instruction : method.getImplementation().getInstructions()){
                    SpecialisedInstruction specialisedInstruction = new SpecialisedInstruction(instruction,method.getImplementation().getRegisterCount(), method.getParameters().size());
                    if(specialisedInstruction.hasValue()){
                        if(specialisedInstruction.getReferenceType() == ReferenceType.METHOD && followInstruction == false){
                            //System.out.println("specialisedInstruction.getValue(): "+specialisedInstruction.getValue());
                            //System.out.println("SmaliLayerUtils.getCallingMethodName(fullMethod): "+SmaliLayerUtils.getCallingMethodName(fullMethod));
                            if(specialisedInstruction.getValue().equals(fullMethod)){

                                //System.out.println("Class: "+SmaliLayerUtils.getCallingClassName(fullMethod));
                                //System.out.println("Method: "+SmaliLayerUtils.getCallingMethodName(fullMethod));
                                followInstruction=true;
                                //System.out.println("invoked in: "+method.toString());

                            }
                        }else if(followInstruction){

                            if(specialisedInstruction.getReferenceType() == ReferenceType.METHOD){
                                //return specialisedInstruction.getValue();
                                DeobfuscationMethod deobfuscationMethod = new DeobfuscationMethod(specialisedInstruction.getValue(),method);


                                return deobfuscationMethod;
                                     }
                        }else{
                            continue;
                        }
                    }else{
                        continue;
                    }
                }

            }


        }
        System.out.println("---------------------------------------------------------------------------------------\n\n");

        //return "";
        return null;
    }


}
