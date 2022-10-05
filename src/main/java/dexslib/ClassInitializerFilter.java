package dexslib;

import dexslib.core.analyse.DeobfuscationMethod;
import dexslib.core.analyse.IdentifyDeobRoutines;
import dexslib.core.slicing.SmaliForwardSlicing;
import dexslib.types.DeobUnit;
import dexslib.types.InstructionRef;
import dexslib.types.SmaliUnit;
import dexslib.util.SmaliLayerUtils;
import dexslib.util.SmaliLineParser;
import dexslib.util.SpecialisedInstruction;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.ReferenceType;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21s;

import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 * Created by Daniel Baier on 03.01.17.
 */
public class ClassInitializerFilter {

    private List<SmaliUnit> clinitClassesList =  new ArrayList<SmaliUnit>();
    private DexFile dexFile2Analyze;
    // this list contains all ClassDefs which are implemented in the Dex
    private static List<ClassDef> allImplementedClassesOfDex = new ArrayList<ClassDef>();
    //private static TreeMap<SmaliUnit,String> remainingCoresMap =new TreeMap<SmaliUnit,String>();
    private static TreeSet<SmaliUnit> remainingCoresSet =new TreeSet<SmaliUnit>();

    private static List<String> allImplementedClassesNamesOfDex = new ArrayList<String>();

    protected static boolean hasNativeCheckRun = false;
    private static ArrayList<String> nativeList = new ArrayList<String>();

    protected static boolean showPossibleDecryptionRoutine = false;




    public ClassInitializerFilter(DexFile dexFile, boolean startFiltering, boolean showPossibleDecryptionRoutine){
        this.dexFile2Analyze = dexFile;
        ClassInitializerFilter.showPossibleDecryptionRoutine = showPossibleDecryptionRoutine; //maybe we want only a list of possible decryption routines
        if(startFiltering || showPossibleDecryptionRoutine){
            this.startClinitFiltering();
        }

    }

    private void startClinitFiltering(){
        this.getClinitClasses(this.dexFile2Analyze);
        this.sortClinitListSize();
    }

    /**
     *
     * @param dexFile
     * @param methodOfClass
     *
     *  L{@literal <}class>;->{@literal <}methodname{@literal >}({@literal <}params{@literal >}){@literal <}ReturnValue{@literal >}
     *
     * @return
     */
    protected static InstructionRef getInsturctionRefForForwardSlice(DexFile dexFile, String methodOfClass, String smaliLine){
        ClassDef classOfInst = null;
        Method method = null;
        SpecialisedInstruction specialisedInstruction = null;

       classOfInst = getClassDefForForwardSlice(dexFile,methodOfClass);
       method = getMethodForForwardSlice(classOfInst,methodOfClass);

       if(method.getImplementation() == null){
            System.err.println("dexlib2: No implementation for this method\nQuiting...");
            System.exit(1);
       }


        /**
         *  smaliLine muss zunächst geparsed werden
         *  --> ich brauche hier also einmal die Überprüfung ob der Smali-String einen Wert hat oder nicht
         */

        for(Instruction instruction : method.getImplementation().getInstructions()){
           specialisedInstruction = new SpecialisedInstruction(instruction,method.getImplementation().getRegisterCount(), method.getParameters().size());
            SmaliLineParser smaliLineParser = new SmaliLineParser(smaliLine);


           //if(smaliLine.equals("const v6, 0x7f09001b") && specialisedInstruction.toString().equals("const v6, 0x7f09001b")){
                /* */

            /*
            if(smaliLine.equals("const v6, 0x7f09001b")){
                System.out.println("\nline="+specialisedInstruction.toString()+"|");
            System.out.println("\nparsedLine="+smaliLineParser.getRegValueFromSmaliLine()+"|");
            System.out.println("VValueLine="+specialisedInstruction.getValue()+"|");
                System.out.println("specialisedInstruction.getRegister(1)="+specialisedInstruction.getRegister(1));
                System.out.println("SmaliLineParser.getRegNumIntFromSmaliLine(1)="+smaliLineParser.getRegNumIntFromSmaliLine(1));
                System.out.println("SmaliLineParser.hasRegsFromSmaliLine()"+smaliLineParser.hasRegsFromSmaliLine());
                System.out.println("specialisedInstruction.hasValue()="+specialisedInstruction.hasValue());
                System.out.println("specialisedInstruction.hasRegister()="+specialisedInstruction.hasRegister());
                System.out.println("specialisedInstruction.getRegNumbers()="+specialisedInstruction.getRegNumbers());
                System.out.println("\n");
            } */


           if(specialisedInstruction.getRegister(1) == smaliLineParser.getRegNumIntFromSmaliLine(1) && smaliLineParser.hasRegsFromSmaliLine() == specialisedInstruction.hasValue()){


               if(smaliLine.equals("const v6, 0x7f09001b")){
                   //System.out.println("\nbeforehasVlaue="+specialisedInstruction.toString());
               }

                if(specialisedInstruction.hasValue()){

                    if(smaliLine.equals("const v6, 0x7f09001b")){
                        //System.out.println("hasVlaue="+specialisedInstruction.toString());
                    }


                    if(specialisedInstruction.getValue().equals(smaliLineParser.getRegValueFromSmaliLine())){
                        System.out.println("finaly found instruction: "+specialisedInstruction.toString());
                        InstructionRef instructionRef = new InstructionRef(classOfInst, method, specialisedInstruction);
                        return instructionRef;
                    }


                }


           }else{
                continue;
           }
       }




        return null;

    }

    protected static ClassDef getClassDefForForwardSlice(DexFile dexFile, String methodOfClass){
        ClassDef classOfInst = null;
        for (ClassDef classDef : dexFile.getClasses()) {


            if(classDef.getType().startsWith("Landroid/support/") || classDef.getType().startsWith("Landroid/support/annotation/") || classDef.getType().contains("R$")){
                continue;
            }
            allImplementedClassesOfDex.add(classDef);
            allImplementedClassesNamesOfDex.add(classDef.getType());


            if(classDef.getType().equals(SmaliLayerUtils.getCallingClassName(methodOfClass))){
                classOfInst = classDef;
            }

        }

        return classOfInst;
    }


    protected static Method getMethodForForwardSlice(ClassDef classDef, String methodOfClass){
        Method emptyMethod = null;
        for(Method tmpMethod : classDef.getMethods()){

            if(tmpMethod.toString().equals(methodOfClass)){
                return tmpMethod;
            }
        }

        return emptyMethod;
    }



    protected static Method getMethodForForwardSlice(DexFile dexFile, String methodOfClass){
        Method method = null;
        for (ClassDef classDef : dexFile.getClasses()) {


            if(classDef.getType().startsWith("Landroid/support/") || classDef.getType().startsWith("Landroid/support/annotation/") || classDef.getType().contains("R$")){
                continue;
            }
            allImplementedClassesOfDex.add(classDef);
            allImplementedClassesNamesOfDex.add(classDef.getType());


            System.out.println("asdds: "+SmaliLayerUtils.getCallingClassName(methodOfClass));
            if(classDef.getType().equals(SmaliLayerUtils.getCallingClassName(methodOfClass))){

                if(method.toString().equals(methodOfClass)){
                    return method;
                }
            }else{
                continue;
            }

        }

        return method;
    }




    /**
        This methods return the ClassDef of the L{@literal <}Class{@literal >};->{@literal <}Method{@literal >};

        @return returns true if the method was found and false if not.
     */
    protected boolean getClassDefForBackwardSlice(DexFile dexFile, String sliceName){
        System.out.println("we are here"+sliceName);
        System.out.println("slice MEthod Name:"+SmaliLayerUtils.getCallingMethodName(sliceName));
        boolean foundMethod = false;
        for (ClassDef classDef : dexFile.getClasses()) {

            //if(classDef.getType().startsWith("Landroid/support/v4/") || classDef.getType().startsWith("Landroid/support/v7/") || classDef.getType().startsWith("Landroid/support/annotation/") || classDef.getType().contains("R$")){
            if(classDef.getType().startsWith("Landroid/support/") || classDef.getType().startsWith("Landroid/support/annotation/") || classDef.getType().contains("R$")){
                continue;
            }
            allImplementedClassesOfDex.add(classDef);
            allImplementedClassesNamesOfDex.add(classDef.getType());


            if(classDef.getType().equals(SmaliLayerUtils.getCallingClassName(sliceName))){
                System.out.println("we found the class of the method");
                for (Method method : classDef.getMethods()) {




                    if (method.toString().equals(sliceName)){
                        System.out.println("we found the *method*");
                        foundMethod = true;

                        SmaliUnit sm = new SmaliUnit();
                        sm.setClassDefinition(classDef);
                        sm.setCoreMethodDefinition(method);
                        sm.setMethod2Analyze(method.toString());
                        sm.setBackwardSliceMethodDefinition(method);
                        sm.setClassDescriptor(classDef.getType());
                        //clinitClassesList.add(sm);
                        remainingCoresSet.add(sm);



                    }
                    continue;
                }
            }else{
                continue;
            }



        }

        return foundMethod;

    }








    /**
      This methods return the ClassDef of all classes which have this {@literal <}Method{@literal >}; for a backslice

   */
    protected static TreeSet<ClassDef> getAllClassDefsForBackwardSlice(DexFile dexFile, String sliceMethod){


        System.out.println("The slicing by only a method name is right now not implemented: "+sliceMethod);
        System.exit(1);

        for (ClassDef classDef : dexFile.getClasses()) {

            //if(classDef.getType().startsWith("Landroid/support/v4/") || classDef.getType().startsWith("Landroid/support/v7/") || classDef.getType().startsWith("Landroid/support/annotation/") || classDef.getType().contains("R$")){
            if(classDef.getType().startsWith("Landroid/support/v") || classDef.getType().startsWith("Landroid/support/annotation/") || classDef.getType().contains("R$")){
                continue;
            }

        }

        return null;

    }


    public static TreeSet<SmaliUnit> getInitialRemainingBackwardSlice(){

        return remainingCoresSet;
    }


    private static ArrayList<String> listOfNativeMethods() {

        if(SmaliLayer.getDexFile2Analyze() == null){
            return null;
        }


        for (ClassDef classDef : SmaliLayer.getDexFile2Analyze().getClasses()) {

            //if(classDef.getType().startsWith("Landroid/support/v4/") || classDef.getType().startsWith("Landroid/support/v7/") || classDef.getType().startsWith("Landroid/support/annotation/") || classDef.getType().contains("R$")){
            if (classDef.getType().startsWith("Landroid/support/") || classDef.getType().startsWith("Landroid/support/annotation/") || classDef.getType().contains("R$") || classDef.getType().startsWith("Lorg/apache/commons/net") || classDef.getType().startsWith(" Lokhttp3/")) {
                continue;
            }



            for (Method method : classDef.getMethods()) {
                if(AccessFlags.formatAccessFlagsForMethod(method.getAccessFlags()).contains("native")){
                    //System.out.println(method.toString());

                    ClassInitializerFilter.nativeList.add(method.toString());
                }
            }
        }

        ClassInitializerFilter.hasNativeCheckRun = true;
        return  ClassInitializerFilter.nativeList;

    }




    public static ArrayList<String> getListOfNativeMethods(){
        if(ClassInitializerFilter.hasNativeCheckRun){
            return  ClassInitializerFilter.nativeList;
        }else{
            return  listOfNativeMethods();
        }
    }

    private static void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,DeobfuscationMethod> pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " +  pair.getValue().getNumOfInvocations());
           // it.remove(); // avoids a ConcurrentModificationException
        }
    }

    private static void printMap2(Map.Entry<String,DeobfuscationMethod> pair) {

            System.out.println(pair.getKey() + " = " +  pair.getValue().getNumOfInvocations());

    }

    private void setNumOfMethodInvocations(){

        /*DeobfuscationMethod deobfuscationMethod = SmaliForwardSlicing.getMethodsInvocationsOfMethod(dexFile,method.toString());

        if(deobfuscationMethod != null) {
            TreeMap<String,DeobfuscationMethod> methodInvokedNumDeobs = new TreeMap<String,DeobfuscationMethod>();
            String fullMethodName = deobfuscationMethod.getFullMethodName();
            if (methodInvokedNumDeobs.isEmpty()) {
                methodInvokedNumDeobs.put(fullMethodName, deobfuscationMethod);
            } else if (methodInvokedNumDeobs.containsKey(fullMethodName)) {
                DeobfuscationMethod tmpDeobfuscationMethod = methodInvokedNumDeobs.get(fullMethodName);
                tmpDeobfuscationMethod.increaseNumOfInvocations();
                methodInvokedNumDeobs.replace(fullMethodName, tmpDeobfuscationMethod);
            } else {
                methodInvokedNumDeobs.put(fullMethodName, deobfuscationMethod);
            }
        }*/

    }

    private static ArrayList<ClassDef> getListOfInternalClasses(DexFile dexFile){
        ArrayList<ClassDef> internalClassesList = new ArrayList<ClassDef>();

        for (ClassDef classDef : dexFile.getClasses()) {

            // Ljava/lang/  Landroid/if(classDef.getType().startsWith("Landroid/support/v4/") || classDef.getType().startsWith("Landroid/support/v7/") || classDef.getType().startsWith("Landroid/support/annotation/") || classDef.getType().contains("R$")){
            if (classDef.getType().startsWith("Ljava/util/") || classDef.getType().startsWith("Ljava/lang/") || classDef.getType().startsWith("Landroid/") || classDef.getType().startsWith("Landroid/support/") || classDef.getType().startsWith("Landroid/support/annotation/") || classDef.getType().contains("R$") || classDef.getType().startsWith("Lorg/apache/commons/") || classDef.getType().startsWith("Lokhttp3/") || classDef.getType().startsWith("Lokio/Base64") || classDef.getType().startsWith("Lorg/sqlite/database")) {
                continue;
            }
            internalClassesList.add(classDef);
        }


        return  internalClassesList;
    }

    public static ArrayList<String> methodInvocationList = new ArrayList<String>();
    public static  void getListOfMethodInvocations(DexFile dexFile){
        ArrayList<ClassDef> internalClassesList = getListOfInternalClasses(dexFile);
        int i = 0;

        for(ClassDef classDef : internalClassesList){

            for (Method method : classDef.getMethods()) {


                if (method.getImplementation() == null) {
                    continue;
                }
                try {

                    for (Instruction currentInstruction : method.getImplementation().getInstructions()) {

                        if (!currentInstruction.getOpcode().setsResult()) {
                            continue;
                        }

                        SpecialisedInstruction currentSpecialisedInstruction = new SpecialisedInstruction(currentInstruction, method.getImplementation().getRegisterCount(), method.getParameters().size());


                        if (currentSpecialisedInstruction.isMethodInvocation() == false) {
                            continue;
                        }


                        if (currentSpecialisedInstruction == null || currentSpecialisedInstruction.getFullMethodName() == null || currentSpecialisedInstruction.getFullMethodName().isEmpty()) {
                            //System.out.println("error in retrieving method name with ReferenceType: " + ReferenceType.toString(currentSpecialisedInstruction.getReferenceType()));
                            continue;
                        }

                        if (currentSpecialisedInstruction.getFullMethodName().startsWith("Ljava/io/") || currentSpecialisedInstruction.getFullMethodName().startsWith("Ljava/net/") || currentSpecialisedInstruction.getFullMethodName().startsWith("Ljava/util/") || currentSpecialisedInstruction.getFullMethodName().startsWith("Ljava/lang/") || currentSpecialisedInstruction.getFullMethodName().startsWith("Landroid/") || currentSpecialisedInstruction.getFullMethodName().startsWith("Lorg/json/")) {
                            continue;
                        }

                        methodInvocationList.add(currentSpecialisedInstruction.getFullMethodName());


                    }
                }catch (Exception e){
                    e.printStackTrace();
                    continue;
                }



            }
        }


    }

    public int getSortedMethodInvocations(String getMethod2LookFor){


        TreeMap<String, Integer> tmap = new TreeMap<String, Integer>();
        //System.out.println("We sort now "+methodInvocationList.size());
        for (String smaliStringMethodInvocation : methodInvocationList) {
            Integer elementCount = tmap.get(smaliStringMethodInvocation);
            tmap.put(smaliStringMethodInvocation, (elementCount == null) ? 1 : elementCount + 1);
        }


        // let's sort this map by values first
        Map<String, Integer> sorted = tmap
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2,
                                LinkedHashMap::new));


        int i =0;
        for(Map.Entry sortedEntry: sorted.entrySet()){
            //System.out.println(sortedEntry.getKey() +"\t  :" + sortedEntry.getValue());
            if(((String) sortedEntry.getKey()).equals(getMethod2LookFor)){
                //System.out.println("getMethod2LookFor: "+getMethod2LookFor);
                return (Integer) sortedEntry.getValue();
            }
            i++;

        }

        return 0;

    }

    public ArrayList<DeobfuscationMethod> getSortedMethodInvocations(int firstNthMethods, String toLookFor){
        ArrayList<DeobfuscationMethod> tmpDeobList = new ArrayList<DeobfuscationMethod>();

        TreeMap<String, Integer> tmap = new TreeMap<String, Integer>();
        //System.out.println("We sort now "+methodInvocationList.size());
        for (String smaliStringMethodInvocation : methodInvocationList) {
            Integer elementCount = tmap.get(smaliStringMethodInvocation);
            tmap.put(smaliStringMethodInvocation, (elementCount == null) ? 1 : elementCount + 1);
        }


        // let's sort this map by values first
        Map<String, Integer> sorted = tmap
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2,
                                LinkedHashMap::new));


        int i =0;
        for(Map.Entry sortedEntry: sorted.entrySet()){

            DeobfuscationMethod tmpDeobfuscationMethod = new DeobfuscationMethod((String)sortedEntry.getKey(),(Integer) sortedEntry.getValue());
//

           // if(tmpDeobfuscationMethod.getNumOfInvocations() == 6 && ((String)sortedEntry.getKey()).equals(toLookFor)){
            if(((String)sortedEntry.getKey()).equals(toLookFor) && tmpDeobfuscationMethod.getNumOfInvocations() >=2){
                //System.out.println(sortedEntry.getKey() +"\t  :" + sortedEntry.getValue());
                System.out.println("Name of deobMethod: "+tmpDeobfuscationMethod.getFullMethodName());
                System.out.println("Number of invocations: "+tmpDeobfuscationMethod.getNumOfInvocations());
                tmpDeobList.add(tmpDeobfuscationMethod);
            }

            if(i == firstNthMethods){
                return tmpDeobList;
            }
            i++;

        }


        return tmpDeobList;
    }


    public static <T> List<T>
    getListFromIterator(Iterator<T> iterator)
    {

        // Create an empty list
        List<T> list = new ArrayList<>();

        // Add each element of iterator to the List
        iterator.forEachRemaining(list::add);

        // Return the List
        return list;
    }

    private void getClinitClasses(DexFile dexFile){
        boolean isStaticMethodHolder = false;
        int countMethods = 0;
        int numOfClinits = 0;
        int numwithoutLinks = 0;
        int realCore = 0;
        //Map<String,Integer> methodInvokedNum = new HashMap<String,Integer>();
        TreeMap<String,DeobfuscationMethod> methodInvokedNum = new TreeMap<String,DeobfuscationMethod>();
        TreeMap<String,DeobfuscationMethod> methodInvokedNumDeobs = new TreeMap<String,DeobfuscationMethod>();

        System.out.println("Number of Classes in DEX: "+dexFile.getClasses().size());

        //System.out.println("False CLINITS:...");

        for (ClassDef classDef : dexFile.getClasses()) {

            //if(classDef.getType().startsWith("Landroid/support/v4/") || classDef.getType().startsWith("Landroid/support/v7/") || classDef.getType().startsWith("Landroid/support/annotation/") || classDef.getType().contains("R$")){
            if(classDef.getType().startsWith("Landroid/support/") || classDef.getType().startsWith("Landroid/support/annotation/") || classDef.getType().contains("R$") || classDef.getType().startsWith("Lorg/apache/commons/") || classDef.getType().startsWith("Lokhttp3/")  || classDef.getType().startsWith("Lokio/Base64")  || classDef.getType().startsWith("Lorg/sqlite/database")  ){
                continue;
            }

            /* This has to be improved as the dectection of the inner class string table holder ....we can compare the idea with the clinit type
            if(classDef.getType().startsWith("Lcom/google/android/gms/common/zzc")){



            if(AccessFlags.formatAccessFlagsForClass(classDef.getAccessFlags()).contains("final") && SmaliLayerUtils.isInnerClass(classDef.getType())){
                System.out.println("we found some of the obfuscated classes..."+ classDef.getType());
                //Ldalvik/annotation/InnerClass;
                //System.out.println("we found some of the obfuscated classes..."+ AccessFlags.values().toString());

                /*if(classDef.getAnnotations().contains("Ldalvik/annotation/InnerClass;")){
                // spielt alles in skygofree_real apk ab muss noch verbesser werden
                    System.out.println("a inner class for real");
                }*

                 for(Annotation  t : getListFromIterator(classDef.getAnnotations().iterator())){
                     System.out.println("our t: "+t.getType());
                     if(t.getType().contains("Ldalvik/annotation/InnerClass;")){
                         for(AnnotationElement annotationElement : getListFromIterator(t.getElements().iterator())){
                            if(annotationElement.getName().contains("accessFlags")) {
                                System.out.println("our encoded value: " + ((IntEncodedValue) annotationElement.getValue()).getValue());
                            }



                         }


                     }
                 }


                //classDef.getAccessFlags()
            }
            }*/



            numwithoutLinks++;

            for (Method method : classDef.getMethods()) {



                if(method.getImplementation() == null){
                    continue;
                }
                countMethods++;
                //System.out.println("method name:"+method.toString());

                /*if(method.toString().equals("Lcom/example/obfuscator/customstringobfuscator/MainActivity;->getDeobValues()Ljava/util/Map;")){
                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ START INVESTIGATING +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                    for(Instruction tmpInstruction : method.getImplementation().getInstructions()){
                        SpecialisedInstructionHelper spHelper = new SpecialisedInstructionHelper(tmpInstruction,method.getImplementation().getRegisterCount(), method.getParameters().size());
                        System.out.println("Instruction: "+spHelper.toString()+" \n");
                    }
                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ STOP  INVESTIGATING +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                }*/


                /*
                if(method.toString().contains("onReceive(Landroid/content/Context;Landroid/content/Intent;)V") && classDef.getType().equals("Lh/h/AlarmReceiver;")){
                    for(Instruction tmpInstruction : method.getImplementation().getInstructions()){
                        //SpecialisedInstructionHelper spHelper = new SpecialisedInstructionHelper(tmpInstruction,method.getImplementation().getRegisterCount(), method.getParameters().size());
                        SpecialisedInstruction spHelper = new SpecialisedInstruction(tmpInstruction,method.getImplementation().getRegisterCount(), method.getParameters().size());
                        System.out.println("Instruction: "+spHelper.toString()+" \n");
                    }
                    System.exit(0);
                }*/

                String fullNativeMethodName = IdentifyDeobRoutines.isNativeDeobRoutine(method.getImplementation(),method);
                if(fullNativeMethodName != null){
                    //System.out.println("native: "+method.toString());
                    //SmaliLayer.MAP_OF_DEOB_ROUTINES.put(method.toString(),method);
                    // contains a list of possible deobroutines which are using native calls --> now we changed it
                    SmaliLayer.MAP_OF_NATIVE_DEOB_ROUTINES.put(method.toString(),method);
                }

                // ich könnte falls das Ergebnis war ist, diese in einer TreeSet aufnehmen...
                // is method a deobfuscation routine
                int trashold = IdentifyDeobRoutines.isDeobRoutine(method.getImplementation(),method);
                int secondTrashold = IdentifyDeobRoutines.isDecryptbRoutine(method.getImplementation(),method);
                if(trashold != 0 || secondTrashold == 20){
                    DeobUnit deobUnit = new DeobUnit(method);
                    deobUnit.setEncryptionTrashold(trashold);
                    SmaliLayer.MAP_OF_DEOB_ROUTINES.put(method.toString(),deobUnit);
                    /*if(method.toString().equals("Lcom/weather/kashmir/services/WeatherUe;->qd(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;")){
                        System.out.println("we are testing now for this method");
                    }*/

                   /* DeobfuscationMethod deobfuscationMethod = SmaliForwardSlicing.getMethodsInvocationsOfMethod(dexFile,method.toString());

                    if(deobfuscationMethod != null) {
                        if(method.toString().equals("Lcom/weather/kashmir/services/WeatherUe;->qd(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;")){
                            System.out.println("value of deobfuscationMethod="+deobfuscationMethod.getNumOfInvocations());
                        }
                        String fullMethodName = deobfuscationMethod.getFullMethodName();
                        if (methodInvokedNumDeobs.isEmpty()) {
                            methodInvokedNumDeobs.put(fullMethodName, deobfuscationMethod);
                        } else if (methodInvokedNumDeobs.containsKey(fullMethodName)) {
                            DeobfuscationMethod tmpDeobfuscationMethod = methodInvokedNumDeobs.get(fullMethodName);
                            tmpDeobfuscationMethod.increaseNumOfInvocations();
                            methodInvokedNumDeobs.replace(fullMethodName, tmpDeobfuscationMethod);
                        } else {
                            methodInvokedNumDeobs.put(fullMethodName, deobfuscationMethod);
                        }
                    }*/
                }


                /*
                if(method.toString().equals("Ltp5x/WGt12/StringDecoder;->decode(Ljava/lang/String;)Ljava/lang/String;")){
                    // Eigentlich sollte das Ergebnis 129 obfuskierter Strings liefern, derzeit sind es nur 59
                    //Map<String,InstructionRef> obfuscatedStringsForDeobMethodMap = new HashMap<String,InstructionRef>();
                    // Map<InstructionRef,String> obfuscatedStringsForDeobMethodMap = new HashMap<InstructionRef,String>();
                    Map<InstructionRef, MethodParameters> obfuscatedStringsForDeobMethodMap = new HashMap<InstructionRef,MethodParameters>();
                    System.out.println("time to identify");
                    for(ArrayList<InstructionRef> tmpList : BackwardSliceOnRegisterLevel.getAllMethodsRegs(method)){ //
                        obfuscatedStringsForDeobMethodMap.putAll(BackwardSliceOnRegisterLevel.getObfuscatedParamtersForDeobMethod(tmpList));
                        // obfuscatedStringsForDeobMethodMap.putAll(BackwardSliceOnRegisterLevel.getObfuscatedStringsForDeobMethod(tmpList));
                    }

                    System.out.println("------------------------------------------------ OBFUSCATED STRINGS --------------------------------------------");
                    for(MethodParameters key : obfuscatedStringsForDeobMethodMap.values()){
                        System.out.println("value = "+key.getFirstMethodParamterValue());
                    }
                }  */

                /*if(method.getParameters().isEmpty() == false){
                    System.out.println("ParameterType: "+method.getParameters().get(0).getType()); // Z I   Ljava/lang/Object;
                    System.out.println("ParameterSignatur: "+method.getParameters().get(0).getSignature());
                    System.out.println("ParameterName: "+method.getParameters().get(0).toString());
                }*/


                /*
                if(classDef.toString().equals("Ltp5x/WGt12/BootReceiver;") || classDef.toString().equals("Ltp5x/WGt12/Y6Cg03N$1$1;")){

                    System.out.println("------------WTF------------");


                        for(Instruction currentInstruction : method.getImplementation().getInstructions()){
                            SpecialisedInstruction currentSpecialisedInstruction = new SpecialisedInstruction(currentInstruction,method.getImplementation().getRegisterCount(), method.getParameters().size());
                            if(currentSpecialisedInstruction.toString().contains("aput") || currentSpecialisedInstruction.toString().contains("invoke-virtual")){
                                    System.out.println("RegisterList: "+currentSpecialisedInstruction.registerOfInstructionAsList());
                                System.out.println("RegisterAsString: "+currentSpecialisedInstruction.getRegistersAsString());
                            }

                        }




                }*/

                // this way we can identify a class which holds a lot of public static methods which only return the obfuscated string
                if(method.getReturnType().equals("Ljava/lang/String;") && method.getParameters().isEmpty() && AccessFlags.formatAccessFlagsForMethod(method.getAccessFlags()).contains("public static")){
                    if(method.getImplementation() != null){
                        boolean isStaticStringContainer = false;
                        for(Instruction tmpInstruction : method.getImplementation().getInstructions()){
                            SpecialisedInstruction specialisedInstruction = new SpecialisedInstruction(tmpInstruction,method.getImplementation().getRegisterCount(),method.getParameters().size());

                            if(specialisedInstruction.getOpcode().name.equals("return-object") && isStaticStringContainer){

                                try {
                                    //System.out.println("Container: "+method.toString()); // a specia singel core
                                    // detect decryption routine of it
                                    //String fullMethodName = SmaliForwardSlicing.getMethodsInvocationsOfMethod(dexFile,method.toString());
                                    DeobfuscationMethod deobfuscationMethod = SmaliForwardSlicing.getMethodsInvocationsOfMethod(dexFile, method.toString());
                                    String fullMethodName = deobfuscationMethod.getFullMethodName();
                                    if (methodInvokedNum.isEmpty()) {
                                        methodInvokedNum.put(fullMethodName, deobfuscationMethod);
                                    } else if (methodInvokedNum.containsKey(fullMethodName)) {
                                        DeobfuscationMethod tmpDeobfuscationMethod = methodInvokedNum.get(fullMethodName);
                                        tmpDeobfuscationMethod.increaseNumOfInvocations();
                                        methodInvokedNum.replace(fullMethodName, tmpDeobfuscationMethod);
                                    } else {
                                        methodInvokedNum.put(fullMethodName, deobfuscationMethod);
                                    }
                                    isStaticMethodHolder = true;
                                }catch (NullPointerException e){
                                    System.err.println("NullPointerException on ClassInitializerFilter:694");
                                    System.err.println("While working on method: "+method.toString());

                                    /*
                                    happend on apk: i evaluation/pornhub.apk
                                    NullPointerException on ClassInitializerFilter:694
While working on method: Lcom/tapjoy/TapjoyConnectCore;->getConnectURL()Ljava/lang/String;
java.lang.NullPointerException
	at defuscator.ClassInitializerFilter.getClinitClasses(ClassInitializerFilter.java:695)
	at defuscator.ClassInitializerFilter.startClinitFiltering(ClassInitializerFilter.java:62)
	at defuscator.ClassInitializerFilter.<init>(ClassInitializerFilter.java:56)
	at defuscator.SmaliLayer.startObfuscationHeuristic(SmaliLayer.java:495)
	at defuscator.Main.main(Main.java:389)


NullPointerException on ClassInitializerFilter:694
While working on method: Lcom/vungle/sdk/VunglePub;->getVersionString()Ljava/lang/String;
java.lang.NullPointerException
	at defuscator.ClassInitializerFilter.getClinitClasses(ClassInitializerFilter.java:695)
	at defuscator.ClassInitializerFilter.startClinitFiltering(ClassInitializerFilter.java:62)
	at defuscator.ClassInitializerFilter.<init>(ClassInitializerFilter.java:56)
	at defuscator.SmaliLayer.startObfuscationHeuristic(SmaliLayer.java:495)
	at defuscator.Main.main(Main.java:389)


                              this return null: maliForwardSlicing.getMethodsInvocationsOfMethod(dexFile, method.toString());


                                    */

                                    e.printStackTrace();
                                }

                            }

                            if(specialisedInstruction.getOpcode().name.equals("const-string")){
                                isStaticStringContainer=true;
                            }else{
                                isStaticStringContainer=false;
                            }


                        }
                    }
                }




                if (method.getName().equals("<clinit>")){

                    numOfClinits++;

                    if(SmaliLayerUtils.hasStaticLiteral(classDef)){
                        SmaliUnit sm = new SmaliUnit();
                        sm.setClassDefinition(classDef);
                        sm.setCoreMethodDefinition(method);
                        sm.setMethod2Analyze(method.toString());

                        realCore++;
                        clinitClassesList.add(sm);
                    }else{
                        //System.out.println(classDef.getType());
                    }
                    // sm.getClassDefinition().getSuperclass()



                    if(showPossibleDecryptionRoutine == false){
                        continue;
                    }

                }
            }

            // ins this Map we find all the ClassDefs which aren't filtered from the above state which is filtering for android support libraries
            allImplementedClassesOfDex.add(classDef);
            allImplementedClassesNamesOfDex.add(classDef.getType());
        }
        System.out.println("Number of CLINITs in DEX: "+numOfClinits);
        System.out.println("Number of classes in DEX: "+dexFile.getClasses().size());
        System.out.println("Number of implemented methods in DEX: "+countMethods);
        System.out.println("Number of static string invokes: ");
        //printMap(methodInvokedNum.descendingMap());

        if(showPossibleDecryptionRoutine){


            //
            //

            // DeobfuscationMethod deobfuscationMethod = SmaliForwardSlicing.getMethodsInvocationsOfMethod(dexFile,method.toString());


            if(SmaliLayer.MAP_OF_DEOB_ROUTINES.isEmpty()){
                System.out.println("some how this is empty...there seems to be a bug inside the deobfuscation detection heuristic");
            }else{
                System.out.println("\n");
                //int maxMethods = countMethods / 6;
                int maxMethods = 5000;
                //ArrayList<DeobfuscationMethod> tmpDeobList = getSortedMethodInvocations(280);
                System.out.println("list of possible deobfuscation method:"+maxMethods+ "\n");
                for(String fullMethodName : SmaliLayer.MAP_OF_DEOB_ROUTINES.keySet()){
                   // DeobfuscationMethod deobfuscationMethod = SmaliForwardSlicing.getMethodsInvocationsOfMethod(dexFile,deobUnit.getFullMethodName());

                    //System.out.println(fullMethodName);
                    //getSortedMethodInvocations(fullMethodName);

                    ArrayList<DeobfuscationMethod> tmpDeobList = getSortedMethodInvocations(maxMethods,fullMethodName);

                    /*if(deobfuscationMethod.getNumOfInvocations() >= 2){
                        System.out.println("Name of possible DeobMethod: "+deobfuscationMethod.getFullMethodName());
                        System.out.println("Num of invocations: "+deobfuscationMethod.getNumOfInvocations());
                    }*/

                }

            }

            if(isStaticMethodHolder) {

                if (methodInvokedNum.firstEntry().getValue().getNumOfInvocations() >= 2){
                    System.out.println("our deobfuscation method: ");
                    printMap2(methodInvokedNum.firstEntry());
                }

            }

            System.out.println("\nfinished with identifying the decryption routines...\n\n");
            System.exit(1);
        }


        if(isStaticMethodHolder) {
            System.out.println("our deobfuscation method: ");
            printMap2(methodInvokedNum.firstEntry());
            System.out.println("our obfuscatied values: ");

            // with this method we can get the obfuscated value through backward slicing from the deobfuscation method
           // BackwardSliceOnRegisterLevel.getObfuscatedStringsForDeobMethod(methodInvokedNum.firstEntry().getValue().getDeobMethod(), methodInvokedNum.firstEntry().getValue());
        }
        //System.out.println("\n\n\n\n\n\n-----------------------------------------------------------------------------------------------------------------------------------------------");//
        /*
        //BackwardSliceOnRegisterLevel.getAllMethodsRegs(methodInvokedNum.firstEntry().getValue().getDeobMethod());
        for(ArrayList<InstructionRef> tmpList : BackwardSliceOnRegisterLevel.getAllMethodsRegs(methodInvokedNum.firstEntry().getValue().getDeobMethod())) {
            // BackwardSliceOnRegisterLevel.getObfuscatedStringsForDeobMethod(BackwardSliceOnRegisterLevel.getAllMethodsRegs(methodInvokedNum.firstEntry().getValue().getDeobMethod()));
            Map<InstructionRef,String> obfuscatedStringsForDeobMethodMap = BackwardSliceOnRegisterLevel.getObfuscatedStringsForDeobMethod(tmpList);
            System.out.println("size: "+obfuscatedStringsForDeobMethodMap.values().size());
            for(String obfuscatedString : obfuscatedStringsForDeobMethodMap.values()){
                System.out.println(obfuscatedString);
            }


            for(InstructionRef tmpInstructionRef : tmpList){
                System.out.println(tmpInstructionRef.toString());
            }
        }*/

        //System.exit(1);
        //System.out.println("Total Num: "+numwithoutLinks);
        // it seems that by obfuscations like whatsapp using it, that we have a lot of clinits > 10-50% are clinits
        // but when we have something like pornplayer (despite the inner ones) it is more like only clinits 100%
        //System.exit(0);
    }


    /**
     *
     * @param instruction
     * @return
     */
    private boolean hasRequirementsForCore(Instruction instruction){
        Opcode instructionOpcode = instruction.getOpcode();

        if(instructionOpcode.name().equals("FILL_ARRAY_DATA") || instructionOpcode.name().equals("NEW_ARRAY") || instructionOpcode.referenceType == ReferenceType.STRING){
            return true;
        }


        return false;
    }


    private boolean hasRequirementsForCoreConstTest(Instruction instruction){
        Opcode instructionOpcode = instruction.getOpcode();

        if(instructionOpcode.name().equals("CONST_STRING") || instructionOpcode.name().equals("CONST_STRING_JUMBO") || instructionOpcode.referenceType == ReferenceType.STRING){
            return true;
        }


        return false;
    }



    private int getArrayLength(Instruction instruction,Instruction prevInstruction){

        Opcode instructionOpcode = instruction.getOpcode();
        if(instructionOpcode.name().equals("NEW_ARRAY")){
            Opcode prevInstructionOpcode = prevInstruction.getOpcode();
            if(prevInstructionOpcode.name().equals("CONST_16")){
                int arrSize = ((Instruction21s) prevInstruction).getNarrowLiteral();
                return arrSize;
            }


        }
        return 0;
    }

    /**
     * sort the Clinit-Classes by Size and filter it for some requirements which has to be fulfilled if
     */
    private void sortClinitListSize(){
        //System.out.println("clinitClassesList.size(): "+clinitClassesList.size());
        for(SmaliUnit sm : clinitClassesList){
            boolean fulfillCoreRequirements = false;
            boolean hasNoString = false;

            MethodImplementation methodImplementation = sm.getCoreMethodDefinition().getImplementation();
            Instruction prevInst = null;
            int instCount = 0;
            for (Instruction instruction : methodImplementation.getInstructions()) {

                if(fulfillCoreRequirements == false){
                    //System.out.println(instCount+ " : "+instruction.getCodeUnits());
                    /*if(prevInst == null && ){
                        continue;
                    }*/

                    // this has to be improved for now this is a workaround to deal with the issue that we have only two cores which
                    // have some cross refs
                    if(instCount > 40 || this.getArrayLength(instruction,prevInst) > 30 || this.hasRequirementsForCoreConstTest(instruction)) {
                        fulfillCoreRequirements = this.hasRequirementsForCore(instruction);
                    }
                }

                prevInst = instruction;
                instCount++;
            }


            if(fulfillCoreRequirements){
                //if (sm.getClassDefinition().getType().contains("whatssip")){
                /*if(instCount < 100 && hasNoString){

                }*/
                sm.setNumberOfInst(instCount);
                sm.setClassDescriptor(sm.getClassDefinition().getType());
                remainingCoresSet.add(sm);}
           // }

        }
    }


    protected List<ClassDef> getAllImplementedClassesOfDex(){
        return allImplementedClassesOfDex;
    }


    public static List<ClassDef> getListOfFilteredClinitClasses(){
        return allImplementedClassesOfDex;
    }

    public static List<String> getNamesListOfFilteredClinitClasses(){
        return allImplementedClassesNamesOfDex;
    }


    public TreeSet<SmaliUnit> getBackwardSliceSet(){
        Iterator<SmaliUnit> itr = remainingCoresSet.iterator();
        while(itr.hasNext()){
            System.out.println("Possible Core-Class: "+ itr.next().getClassDefinition().getType());
        }

        return remainingCoresSet;
    }


    public TreeSet<SmaliUnit> getInitialRemainingCoresSet(){
        System.out.println("Num of probably obfuscated CLINITs: "+remainingCoresSet.size());

        Iterator<SmaliUnit> itr = remainingCoresSet.iterator();
       /*while(itr.hasNext()){
            System.out.println("Possible Core-Class: "+ itr.next().getClassDefinition().getType());
        }*/

        return remainingCoresSet;
    }
}
