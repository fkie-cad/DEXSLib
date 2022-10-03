package dexslib.core.analyse;

import dexslib.ClassInitializerFilter;
import dexslib.SmaliLayer;
import dexslib.types.DeobUnit;
import dexslib.util.SmaliLayerUtils;
import dexslib.util.SpecialisedInstruction;
import org.jf.dexlib2.ReferenceType;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.instruction.Instruction;

import java.util.*;

public class IdentifyDeobRoutines {

    private final static String[] SMALI_BIT_OPERATIONS = {"add-double","add-float","add-int","add-long","and-int","and-long","div-double","div-float","div-int","div-long","double-to-float","double-to-int","double-to-long","float-to-double","float-to-int","float-to-long","int-to-byte","int-to-char","int-to-double","int-to-float","int-to-long","int-to-short","long-to-double","long-to-float","long-to-int","mul-double","mul-float","mul-int","mul-long","neg-double","neg-float","neg-int","neg-long","not-int","not-long","or-int","or-long","rem-double","rem-float","rem-int","rem-long","rsub-int","shl-int","shl-long","shr-int","shr-long","sub-double","sub-float","sub-int","sub-long","ushr-int","ushr-long","xor-int","xor-long"};
    private final static String[] ANDROID_CRYPTO_LIBS = {"Lcom/chilkatsoft", "Lgnu/crypto", "Ljavax/crypto", "Ljava/security", "Lorg/bouncycastle", "Lorg/jasypt"};
    /*

    1. Literal can be an byte or int array
    2. empty byte/int array created + filled with data
    3. byte array received over other method




       we need something like if return type is [B we can assume that after an invocation of this method
       a String cast will be applied

       if we have the return type of String we can assume that this method is invocated multpile times



    "

     */

    private static boolean isReturnTypeFulfilled(Method method){
        if(method.getReturnType().contains("Ljava/lang/String;") || method.getReturnType().contains("[Ljava/lang/String;") || method.getReturnType().contains("[C") || method.getReturnType().contains("[B")) {
            return true;
        }else{
            return false;
        }

    }


    /**
     *
     * @param method
     * @return 0x00 --> false ; 0x01 --> only [I oder [B and 0x11 --> String
     */
    private static byte areParamterTypesFulfilled(Method method){
        Iterator methodParamIterator = method.getParameters().iterator();
        int deobThreshold = 0;
        if (method.getParameters().size() > 0) {

            while (methodParamIterator.hasNext()) {
                MethodParameter mp = (MethodParameter) methodParamIterator.next();

                if(mp.getType().contains("Landroid/os")){
                    return 0x00; // this is probably no deobfuscation routine
                }



                if (mp.getType().contains("[I") || mp.getType().contains("[B") || mp.getType().contains("Ljava/lang/String;")) {
                    deobThreshold++;

                    if (mp.getType().contains("Ljava/lang/String;")) {
                        return 0x11;
                    }
                }
            }

            if(deobThreshold == 0){
                return 0x00; // no parameter fulfilled the requirements for a deobfuscation routine
            }else{
                return 0x01;
            }

        }else{
            return 0x00; // no parameter fulfilled the requirements for a deobfuscation routine
        }
    }


    /**
     *
     * @param method
     * @return 0x00 --> false ; 0x01 --> only [I oder [B and 0x11 --> String
     */
    private static byte checkDeobRequirements(Method method){

        // this filter has to be inproved for converting Method in general
        // e.g. Lcom/weather/kashmir/services/WeatherSt;->bytesToHex([B)Ljava/lang/String in apk evaluation/bahamut_sample1.apk
        if(method.getName().equals("bytesToHex")){
            return 0x00;
        }

        if(IdentifyDeobRoutines.isReturnTypeFulfilled(method)){
           return  IdentifyDeobRoutines.areParamterTypesFulfilled(method);
        }else{
            return 0x00;
        }

    }


    /*public static boolean hasDeobRoutines(MethodImplementation possibleMethodImpl, Method method){

        if(getDeobRoutinesList(possibleMethodImpl,method)){

        }
        return  false;
    }*/

    public static int isDecryptbRoutine(MethodImplementation possibleMethodImpl, Method method){
        boolean checkForDeob = false;

        byte requirementsFulfilled =  IdentifyDeobRoutines.checkDeobRequirements(method);
        if(requirementsFulfilled == 0x00){
            return 0;
        }
        ArrayList<SpecialisedInstruction> register2Analyze = new ArrayList<SpecialisedInstruction>();

        for (Instruction tmpInstruction : possibleMethodImpl.getInstructions()) {

            if (tmpInstruction.getOpcode().name.contains("new-instance") || tmpInstruction.getOpcode().name.contains("invoke-") || tmpInstruction.getOpcode().name.startsWith("const")) {

                SpecialisedInstruction instructionOfInterest = new SpecialisedInstruction(tmpInstruction, possibleMethodImpl.getRegisterCount(), method.getParameters().size());
                register2Analyze.add(instructionOfInterest);
                if(instructionOfInterest.toString().contains("Ljavax/crypto/Cipher;->doFinal([B)[B")){
                    checkForDeob = true;
                }
            }

        }

        if(checkForDeob){
            String lookForReg = "unknown";
            for(int j = register2Analyze.size()-1; j >= 0; j--){

                if(register2Analyze.get(j).toString().startsWith("const/4")){
                    if(register2Analyze.get(j).getIntValue() == 2){
                        //  	DECRYPT_MODE 	2

                        System.out.println("MethodName: " + method.toString() + "      ReturnType: " + method.getReturnType() + "   treshold: " + 20);
                        System.out.println("This is classicly AES decryption routine");

                        System.out.println("\n");


                        return 20;

                    }
                }

                if(register2Analyze.get(j).toString().contains("Ljavax/crypto/Cipher;->init(ILjava/security/Key;)V")){
                   lookForReg = register2Analyze.get(j).getNthRegisterAsString(2);
                   continue;
                }

            }
        }



        return 0;
    }


    public static int isDeobRoutine(MethodImplementation possibleMethodImpl, Method method){
        int deobThreshold = 0;
        int cryptoThreshold = 2;
        boolean cotainsStringAsParam = false;
        ArrayList<String> auswertungsHilfe = new ArrayList<String>();
        LinkedHashSet<String> hashSet = new LinkedHashSet<String>();

        ArrayList<String> auswertungsHilfe_crypto = new ArrayList<String>();
        LinkedHashSet<String> hashSet_crypto = new LinkedHashSet<String>();




        byte requirementsFulfilled =  IdentifyDeobRoutines.checkDeobRequirements(method);


        if(method.toString().equals("Lcom/vvt/configurationmanager/a;->a([B)Ljava/lang/String;")){
            System.out.println("Requirement="+requirementsFulfilled);
        }

        if(requirementsFulfilled == 0x11){
            deobThreshold = 2;
            cotainsStringAsParam = true;
        }else if(requirementsFulfilled == 0x01){
            deobThreshold = 2;
        }else{
            return 0;
        }
        if(method.toString().equals("Lcom/vvt/configurationmanager/a;->a([B)Ljava/lang/String;")){
            System.out.println("Requirement=1 deobThreshold-->"+deobThreshold);
        }



        /*
        if(method.getReturnType().contains("Ljava/lang/String;") || method.getReturnType().contains("[Ljava/lang/String;") || method.getReturnType().contains("[C")) {
            deobThreshold++;


            // check for return type of method --> string, string-array or char-array

            Iterator methodParamIterator = method.getParameters().iterator();
            // type is [I oder B[ or Ljava/lang/String;
            if (method.getParameters().size() > 0) {

                while (methodParamIterator.hasNext()) {
                    MethodParameter mp = (MethodParameter) methodParamIterator.next();



                    if (mp.getType().contains("[I") || mp.getType().contains("[B") || mp.getType().contains("Ljava/lang/String;")) {

                        deobThreshold=2;
                        if (mp.getType().contains("Ljava/lang/String;")) {
                            cotainsStringAsParam = true;
                        }
                    }
                }


            }else{
                return false;
            }

        }else {
            // we didn't find any necessary method invocation
            return false;
        }*/


                        boolean warum = false;
                        //ArrayList<>

                      for (Instruction tmpInstruction : possibleMethodImpl.getInstructions()) {
                          //SpecialisedInstruction instructionOfInterest = new SpecialisedInstruction(tmpInstruction,possibleMethodImpl.getRegisterCount(), method.getParameters().size());

                          /*if(method.toString().contains("qd(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;")){
                              System.out.println("warum ^^: "+tmpInstruction.getOpcode().name);
                              warum = true;
                          }*/


                          if (Arrays.binarySearch(SMALI_BIT_OPERATIONS, tmpInstruction.getOpcode().name.split("/")[0]) >= 0) {
                              auswertungsHilfe.add(tmpInstruction.getOpcode().name);
                              hashSet.add(tmpInstruction.getOpcode().name);
                              deobThreshold++;
                            //  if(warum == false){
                                  continue;
                              //}

                          }

                          /*if(method.getName().equals("qd")) {
                              SpecialisedInstruction instructionOfInterest2 = new SpecialisedInstruction(tmpInstruction, possibleMethodImpl.getRegisterCount(), method.getParameters().size());
                              System.out.println("we are in method of interest22: " + instructionOfInterest2.toString() + "         format: "+instructionOfInterest2.getFormatName());
                                if(instructionOfInterest2.getFormatName().equals("Format11n")){
                                    System.out.println("we are in method of interest22: value="+instructionOfInterest2.getIntValue());
                                }
                              /*if(tmpInstruction.getOpcode().format == Format31i){

                                  System.out.println("we are in method of interest111: " + ((Instruction31i) tmpInstruction).getNarrowLiteral());
                                  System.out.println("we are in method of interest111: " + ((Instruction31i) tmpInstruction).getWideLiteral());
                              }*

                              // Format31i
                              // || this.getOpcode().name.startsWith("const")
                          }*/

                          if (tmpInstruction.getOpcode().name.contains("new-instance") || tmpInstruction.getOpcode().name.contains("invoke-")) {

                              SpecialisedInstruction instructionOfInterest = new SpecialisedInstruction(tmpInstruction, possibleMethodImpl.getRegisterCount(), method.getParameters().size());

                              /*if(method.toString().equals("Lcom/vvt/configurationmanager/a;->a([B)Ljava/lang/String;")){
                                  System.out.println("test: "+SmaliLayerUtils.getLib(instructionOfInterest.getValue()));
                              }*/

                              /*if(method.getName().equals("qd")){
                                  //System.out.println("we are in method of interest"+instructionOfInterest.toString());

                              if(instructionOfInterest.toString().contains("Ljavax/crypto/Cipher;->init(ILjava/security/Key;)V")){
                                  System.out.println("we are in method of interest"+instructionOfInterest.getRegister(2));
                                  System.out.println("we are in method of interest"+instructionOfInterest.getNthRegisterAsString(2));

                              }

                              }*/


                              /*
                              if(instructionOfInterest.toString().contains("Ljavax/crypto/Cipher;->doFinal([B)[B")){

                                  cryptoThreshold = cryptoThreshold+2;
                              }*/

                              //System.out.println("test: "+SmaliLayerUtils.getLib(instructionOfInterest.getValue()));

                              if (tmpInstruction.getOpcode().name.contains("new-instance") ) {
                                  if (Arrays.binarySearch(ANDROID_CRYPTO_LIBS, SmaliLayerUtils.getLib(instructionOfInterest.getValue())) >= 0) {

                                      /*if (method.toString().equals("Lcom/vvt/configurationmanager/a;->a([B)Ljava/lang/String;")) {
                                          //System.out.println("Requirement=22 deobThreshold-->" + deobThreshold);
                                      }*/

                                      //System.out.println("Used crypto operations :/");
                                      auswertungsHilfe_crypto.add(tmpInstruction.getOpcode().name);
                                      hashSet_crypto.add(tmpInstruction.getOpcode().name);
                                      cryptoThreshold++;
                                  }

                              }else{
                                  for(String valuePart : ANDROID_CRYPTO_LIBS){
                                      /*if (method.toString().equals("Lcom/vvt/configurationmanager/a;->a([B)Ljava/lang/String;")) {
                                          //System.out.println("Requirement=232 valuePart-->" + valuePart);
                                          System.out.println("valuePartList"+SmaliLayerUtils.getFullMethodAsList(instructionOfInterest.getValue()));
                                      }*/




                                      if (SmaliLayerUtils.getFullMethodAsList(instructionOfInterest.getValue()).contains(valuePart)) {

                                          if (method.toString().equals("Lcom/vvt/configurationmanager/a;->a([B)Ljava/lang/String;")) {
                                              //System.out.println("Requirement=32 deobThreshold-->" + deobThreshold);
                                          }

                                          //System.out.println("Used crypto operations :/");
                                          auswertungsHilfe_crypto.add(tmpInstruction.getOpcode().name);
                                          hashSet_crypto.add(tmpInstruction.getOpcode().name);
                                          cryptoThreshold++;
                                      }
                                  }


                              }
                              if(SmaliLayer.MAP_OF_NATIVE_DEOB_ROUTINES.isEmpty() || SmaliLayer.MAP_OF_NATIVE_DEOB_ROUTINES == null){
                                  if(method.toString().equals("Lcom/vvt/configurationmanager/a;->a([B)Ljava/lang/String;")){
                                      System.out.println("Requirement=4 deobThreshold-->"+deobThreshold);}
                                  continue;
                              }

                              if(SmaliLayer.MAP_OF_NATIVE_DEOB_ROUTINES.keySet().contains(instructionOfInterest.getFullMethodName())){
                                  if(method.toString().equals("Lcom/vvt/configurationmanager/a;->a([B)Ljava/lang/String;")){
                                      System.out.println("Requirement=23 deobThreshold-->"+deobThreshold);}
                                  System.out.println("method of interest: "+method.toString());
                                  cryptoThreshold = cryptoThreshold +3;
                                  deobThreshold = deobThreshold +2;
                              }
                          }





                      }

        if(method.toString().equals("Lcom/vvt/configurationmanager/a;->a([B)Ljava/lang/String;")){
            System.out.println("Requirement=3 deobThreshold-->"+deobThreshold);
            System.out.println("Requirement=3 cryptoThreshold-->"+cryptoThreshold);
        }


                          if ((deobThreshold > 4 && cotainsStringAsParam == false) || (deobThreshold > 5 && cotainsStringAsParam == true)) {
                              // TreeSet<SmaliUnit> remainingCoresSet =new TreeSet<SmaliUnit>();
                              System.out.println("MethodName: " + method.toString() + "      ReturnType: " + method.getReturnType() + "   treshold: " + deobThreshold);
                              System.out.println("Used bit operations: " + auswertungsHilfe.toString());
                              System.out.println("Used bit operations (unique): " + hashSet.toString());
                              System.out.println("\n");
                              return deobThreshold;

                          }
                          if (cryptoThreshold >= 5) {

                              System.out.println("MethodName: " + method.toString() + "      ReturnType: " + method.getReturnType() + "   treshold: " + cryptoThreshold);
                              System.out.println("Used crypto operations: " + auswertungsHilfe_crypto.toString());
                              System.out.println("Used crypto operations (unique): " + hashSet_crypto.toString());
                              System.out.println("\n");
                              return cryptoThreshold;
                          } else {
                              return 0;
                          }






    }


    /**
     *
     * @param possibleDeobRoutineList
     * @return the first element has the most invocations
     */
    public static List<DeobUnit> getDeobRoutinesSortetByInvocation(Map<String,DeobUnit> possibleDeobRoutineList){

        // we iterate over all classes and the possible
        Map<String, DeobUnit> tmpDeobMap = new HashMap<String, DeobUnit>();

        //TreeSet<Method> sortetDeobRoutinesByInvocations =new TreeSet<Method>();
        TreeSet<DeobUnit> sortetDeobRoutinesByInvocations =new TreeSet<DeobUnit>();
        DexFile dexFile = SmaliLayer.getDexFile2Analyze();

        for(ClassDef currentClasDef : dexFile.getClasses()){

            if(currentClasDef.getType().startsWith("Landroid/support/") || currentClasDef.getType().startsWith("Landroid/support/annotation/") || currentClasDef.getType().contains("R$") || currentClasDef.getType().startsWith("Lorg/apache/commons/") || currentClasDef.getType().startsWith("Lokhttp3/")  || currentClasDef.getType().startsWith("Lokio/Base64")){
                continue;
            }

            for(Method currentMethod : currentClasDef.getMethods()){
                if(currentMethod.getImplementation() == null){
                    continue;
                }

                for(Instruction currentInstruction : currentMethod.getImplementation().getInstructions()){
                    if(currentInstruction.getOpcode().referenceType != ReferenceType.METHOD){
                        continue;
                    }

                    SpecialisedInstruction currentSpecialisedInstruction = new SpecialisedInstruction(currentInstruction, currentMethod.getImplementation().getRegisterCount(), currentMethod.getParameters().size());
                    DeobUnit currentDeobUnit = null;

                    //System.out.println("hier bin ich+ "+currentSpecialisedInstruction.getFullMethodName());
                    for(DeobUnit deobRoutine : possibleDeobRoutineList.values()){


                        if(currentSpecialisedInstruction.getFullMethodName().equals(deobRoutine.getMethodDescriptor())){
                            //System.out.println("hier bin ich");



                        if(tmpDeobMap.size() != 0 && tmpDeobMap.get(currentSpecialisedInstruction.getFullMethodName()) != null){
                            currentDeobUnit = tmpDeobMap.get(currentSpecialisedInstruction.getFullMethodName());
                            currentDeobUnit.increaseNumOfInvocations();
                        }else{
                            // we have a new deob Routine
                            currentDeobUnit = new DeobUnit(deobRoutine.getMethodDefinition());
                            currentDeobUnit.setEncryptionTrashold(deobRoutine.getEncryptionTrashold());
                        }
                        tmpDeobMap.put(currentSpecialisedInstruction.getFullMethodName(),currentDeobUnit);
                        //System.out.println("Methodname: "+currentDeobUnit.getMethodDescriptor()+"   Invocations:"+currentDeobUnit.getNumberOfInvocations());
                        }


                    }


                }
            }

        }

        // sorting now the values

        List<DeobUnit> listOfSortedMethodRefs = new ArrayList<DeobUnit>(tmpDeobMap.values());
       // System.out.println("finished analyzing deob routines:"+listOfSortedMethodRefs.size());
        Collections.sort(listOfSortedMethodRefs);

        //System.out.println("finished analyzing deob routines:"+listOfSortedMethodRefs.size());

        return listOfSortedMethodRefs;
    }





    public static String isNativeDeobRoutine(MethodImplementation possibleMethodImpl, Method method){
        int deobThreshold = 0;
        boolean cotainsStringAsParam = false;
        byte requirementsFulfilled =  IdentifyDeobRoutines.checkDeobRequirements(method);
        if(requirementsFulfilled == 0x11){
            deobThreshold = 2;
            cotainsStringAsParam = true;
        }else if(requirementsFulfilled == 0x01){
            deobThreshold = 2;
        }else{
            return null;
        }

        ArrayList<String> nativeList = ClassInitializerFilter.getListOfNativeMethods();

        if(nativeList.size() == 0){
            return null;
        }

        for (Instruction tmpInstruction : possibleMethodImpl.getInstructions()){
            if(tmpInstruction.getOpcode().name.toLowerCase().contains("invoke")){
                for(String checkNative : nativeList){
                    SpecialisedInstruction instructionOfInterest = new SpecialisedInstruction(tmpInstruction,possibleMethodImpl.getRegisterCount(), method.getParameters().size());
                    if(checkNative.contains(instructionOfInterest.getValue())){
                        System.out.println("native deobfuscation routine: "+checkNative);
                        return checkNative;
                    }
                }
            }
        }


        return null;
    }

}
