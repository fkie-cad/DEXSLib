package dexslib; /**
 * Created by Daniel Baier on 29.10.16.
 */


import dexslib.core.CoreInstance;
import dexslib.core.CoreInstanceResults;
import dexslib.core.analyse.IdentifyDeobRoutines;
import dexslib.core.slicing.BackwardSliceOnRegisterLevel;
import dexslib.core.slicing.MethodParameters;
import dexslib.types.DeobUnit;
import dexslib.types.InstructionRef;
import dexslib.types.SmaliUnit;
import dexslib.util.SmaliLayerUtils;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.lang.System.exit;


public class SmaliLayer {

    public static int UNKNOWN_DEOBFUSCATION_TYPE_CNT = 0;
    public static List<String> UNKNOWN_DEOBFUSCATION_CLASSES_LIST = new ArrayList<String>();
    public static List<String> METHODS_DEBOFSCATION_TYPE_LIST = new ArrayList<String>();
    public static List<String> FIELDS_DEBOFSCATION_TYPE_LIST = new ArrayList<String>();
    public static Map<String,DeobUnit> MAP_OF_DEOB_ROUTINES = new HashMap<String,DeobUnit>();
    public static Map<String,Method> MAP_OF_NATIVE_DEOB_ROUTINES = new HashMap<String,Method>();
    //public static List<String> VALIDATION_LIST = new ArrayList<String>();



    /* instances classes */
    private ClassInitializerFilter filterdClinitOfDex;
    private ClassInitializerFilter filterdForMethodOfDex;
    private boolean dissassemble = false;
    private boolean initialRun = true;

    /* class variables */

    // all classes which aren't a core class, but still needed to run the template
    private static LinkedHashSet<String> globalClassesSet = new LinkedHashSet<String>();


    // all possible identified Core-Classes are listed here, and our obfuscation algorithm is run on each core instance as long as entries are left
    public static TreeSet<SmaliUnit> remainingCoresSet =new TreeSet<SmaliUnit>();

    // all possible identified BS-Classes are listet here - normaly it should only one BS class, but just in case
    // maybe later also as smaliunit...but we have to see what is better
    public static TreeSet<ClassDef> remainingBSSet =new TreeSet<ClassDef>();

    public static TreeSet<SmaliUnit> backwardSliceSet =new TreeSet<SmaliUnit>();


    public static LinkedHashSet<String> globalRelevantFieldList = new LinkedHashSet<String>();


    // this hashmap contains all results of all core instances (ClassDescriptor,CoreInstanceResults)
    private  Map<String,CoreInstanceResults> coreInstanceResultsMap = new HashMap<String,CoreInstanceResults>();



    /* internal class type definitions  */

    public static final int DEOBFUSCATION_TYPE_METHOD = 0x1;
    public static final int DEOBFUSCATION_TYPE_FIELD = 0x2;
    public static final int DEOBFUSCATION_TYPE_METHOD_BS = 0x3; // we have no clinit method, but we could identify the deobfuscation method and its input
    public static final int DEOBFUSCATION_TYPE_UNKNOWN = 0xd;

    /* static backward slice option */
    public static boolean isOnlyBackwardSlicePrinted = false;

    private static DexFile globalDexFile;

    public static HashMap<String,String> returnTypeLookupTable = new HashMap<String,String>();

    static {
        returnTypeLookupTable.put("Ljava/lang/String;","const-string");
        returnTypeLookupTable.put("new-array","[B");
    }



    /*
     * Todo:
     *
     * - some super-classes aren't detected, seems like a problem in the field
     *
     *
     *  0. ordentlich machen
     *  1. relevante statische variablen, also jene welche bei privat lokal gelesen werden und bei publich/protected extern gelesen werden
     *     beide werden jedoch lokal in clinit beschrieben
     *
     *     d.h. die methode getFieldsInvocationsOfCore muss noch angepasst werden, für den fall das die fieldinvocation lokal erfolgt, aber nicht in clinit und auch nur dann wenn diese private ist
     *      (bsp. feld x wird in methode useX() lesend benutzt aber nicht in der clinit)
     *  1.1 wenn keine methode im core (also bei typ=1) auf die mehrzahl der statischen variablen angewendet wird, so muss ich das vollständige binary nach dem auftauchen einer Methode suchen, welche diese felder aufruft
     *  2. es gibt mehrere deobfuskierungs-routinen --> rednanga bsp.
     *  3. Muss den deobfuskierungs-typ beachten, da pro klasse es eine clinit gibt, welch nur für diese klasse die literale deobfuskiert -> bsp. whatsapp
     *  4. whatsapp
     *
     *   es sollte mal testweise eine obfuskierung ohne statischen String realisiert werden
     *   und es sollte mal eine obfuskierung gemacht werden die sowas wie
     *
     *   .field private static final ALPHA_PHONE_MAPPINGS:Ljava/util/Map;
            .annotation system Ldalvik/annotation/Signature;
            value = {
                    "Ljava/util/Map",
                    "<",
                    "Ljava/lang/Character;",
                    "Ljava/lang/Character;",
                    ">;"
            }
            .end annotation
        .end field

     *  hat
     *
     *  5. dexFile sollte hier statisch global zur verfügung stehen
     *  -1: default-konstruktor methode, falls keiner vorhanden fuer deobroutinen klasse, damit ich da immer eine instanz bilden kann

     */




    public static void printUnknownError(){
        System.out.println("Unknown Error in");

        StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        StringBuilder sb = new StringBuilder();

        for (StackTraceElement st : ste) {
            sb.append(st.toString() + System.lineSeparator());
        }
        System.out.println(sb);
        System.out.println("\n");
        System.out.println("aborting ....");
        System.out.println("Plz send the analyzed APK or SHA256 to admin-[at]-remoteshell-security.com");
        exit(2);
    }


    public static DexFile getDexFile2Analyze(){
        return SmaliLayer.globalDexFile;
    }


    private DexFile getDexFile2Analyze(String apkName){
        try {
            if(apkName.equals("") || apkName == null){
                return null;
            }

            File srcFile = new File(apkName);
            // Loads a dex/apk/odex/oat file.
            if(!srcFile.exists() ){
                return null;
            }
            DexFile dexFile = DexFileFactory.loadDexFile(srcFile, Opcodes.getDefault());
            SmaliLayer.globalDexFile = dexFile;


            return dexFile;
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }



    private void startTemplateBuilding(DexFile dxFile,Map<String,CoreInstanceResults> resultsOfCoreInstanceMap,String apkName, boolean noCore){

        final String outputDir ="template/";
        File outputDirectoryFile = new File(outputDir);
        if (!outputDirectoryFile.exists()) {
            if (!outputDirectoryFile.mkdirs()) {
                System.err.println("Can't create the output directory " + outputDir);
                exit(-1);
            }
        }

        //TemplateBuilder templateBuilder = new TemplateBuilder(dxFile, resultsOfCoreInstanceMap, globalClassesSet);
        System.out.println("Finished analyzing...");
        if(this.dissassemble) {
            BaksmaliOptions options = new BaksmaliOptions(); // disassembler options, normally the defaults are enough

            int jobs = 2; // number of processors
            //7templateBuilder.disassembleDexFile(outputDirectoryFile,jobs,options);
        }else{
            System.out.println("Start building the template, this may take a while :-) ...");

            if(noCore) {
                /*TemplateBuilder templateBuilderSingleCore = new TemplateBuilder(dxFile, resultsOfCoreInstanceMap.values().iterator().next());
                templateBuilderSingleCore.createDEX(outputDirectoryFile, apkName);*/
            }else if(resultsOfCoreInstanceMap.size() > 1){
               // templateBuilder.createDEX(outputDirectoryFile,apkName);
            }else{
                if(resultsOfCoreInstanceMap.values().isEmpty() || !resultsOfCoreInstanceMap.values().iterator().hasNext()){
                    System.err.println("Error: couldn't build deobfuscation template");
                    System.err.println("plz send APK to admin-[at]-remoteshell-security.com");
                    System.err.println("Size of cores: "+resultsOfCoreInstanceMap.values().size());
                    System.exit(2);
                }
                /*TemplateBuilder templateBuilderSingleCore = new TemplateBuilder(dxFile,resultsOfCoreInstanceMap.values().iterator().next());
                templateBuilderSingleCore.createDEX(outputDirectoryFile,apkName);*/
            }


        }

    }



    // do a static backward slice only for the clinit-methods
    private CoreInstanceResults startCoreInstanceAnalyzing(DexFile dxFile, SmaliUnit currentCoreClass){
        CoreInstance ci = new CoreInstance(dxFile,currentCoreClass,false);

        return  ci.getResultsOfCoreInstance();
    }


    // do a static backward slice only for the given method
    private CoreInstanceResults startMethodInstanceAnalyzing(DexFile dxFile, SmaliUnit currentClass){
        CoreInstance ci = new CoreInstance(dxFile,currentClass,true);

        return  ci.getResultsOfCoreInstance();
    }

    private void startTemplateWithoutCore(DexFile dxFile,String apkName, List<DeobUnit> listOfMethod){
        Map<InstructionRef, MethodParameters> obfuscatedStringsForDeobMethodMap = new HashMap<InstructionRef,MethodParameters>();


       if(listOfMethod == null || listOfMethod.isEmpty()){



       listOfMethod = IdentifyDeobRoutines.getDeobRoutinesSortetByInvocation(MAP_OF_DEOB_ROUTINES);
       }

        // for now we only use the first element as deob routine --> maybe later on we use the trashold which we can safe inside the deobroutine
        Method designatedDeobMethod = listOfMethod.get(0).getMethodDefinition();


        for(ArrayList<InstructionRef> tmpList : BackwardSliceOnRegisterLevel.getAllMethodsRegs(designatedDeobMethod)){ //
            obfuscatedStringsForDeobMethodMap.putAll(BackwardSliceOnRegisterLevel.getObfuscatedParamtersForDeobMethod(tmpList));
            // obfuscatedStringsForDeobMethodMap.putAll(BackwardSliceOnRegisterLevel.getObfuscatedStringsForDeobMethod(tmpList));
        }


        //currentCoreResults.setObfuscatedStringsForDeobMethodMap(obfuscatedStringsForDeobMethodMap);


        CoreInstanceResults tmpCoreInstanceResult  = new CoreInstanceResults(SmaliLayer.DEOBFUSCATION_TYPE_METHOD_BS,obfuscatedStringsForDeobMethodMap );

        this.coreInstanceResultsMap.put("",tmpCoreInstanceResult);
        //this.addGlobalClassRefsList(tmpCoreInstanceResult.getClassNamesOfRelevantClasses());

        this.startTemplateBuilding(dxFile,coreInstanceResultsMap,apkName,true);
        exit(0);
    }



    private void createCore(DexFile dxFile,boolean generateDissassembletTemplate,String apkName){
        this.dissassemble = generateDissassembletTemplate;


        System.out.println("------------------------");
        System.out.println("We have a lot to analyze plz keep calm while building the template...");





            do {

                // auch hier brauche ich eine Abfrage, falls es keine Core-Klasse gibt


                if(remainingCoresSet.isEmpty() ){
                        if(SmaliLayer.MAP_OF_DEOB_ROUTINES.isEmpty()){
                            SmaliLayer.printUnknownError();
                        }
                    startTemplateWithoutCore(dxFile,apkName,null);

                }

                SmaliUnit firstCoreClass = remainingCoresSet.first();
                if(firstCoreClass == null){
                    SmaliLayer.printUnknownError();
                }
                remainingCoresSet.remove(firstCoreClass);

                SmaliLayerUtils.setCoreClassPackage(firstCoreClass.getClassDefinition());

                // the results for the temporary analyzed core instance
                CoreInstanceResults tmpCoreInstanceResult  = this.startCoreInstanceAnalyzing(dxFile,firstCoreClass);

                if(tmpCoreInstanceResult == null){
                    continue;
                }

                if(tmpCoreInstanceResult.getDeobfuscationType() == SmaliLayer.DEOBFUSCATION_TYPE_FIELD){
                    System.out.println("DeobfuscationType: SmaliLayer.DEOBFUSCATION_TYPE_FIELD");
                }else if(tmpCoreInstanceResult.getDeobfuscationType() == SmaliLayer.DEOBFUSCATION_TYPE_METHOD){
                    System.out.println("DeobfuscationType: SmaliLayer.DEOBFUSCATION_TYPE_METHOD");
                }else if(tmpCoreInstanceResult.getDeobfuscationType() == SmaliLayer.DEOBFUSCATION_TYPE_METHOD_BS){
                    System.out.println("DeobfuscationType: SmaliLayer.DEOBFUSCATION_TYPE_METHOD_BS");
                }else{
                    System.out.println("DeobfuscationType: SmaliLayer.DEOBFUSCATION_TYPE_UNKOWN");
                }

                // classDescriptor for the
                String classDescriptor = tmpCoreInstanceResult.getCurrentCoreClassSmaliUnit().getClassDescriptor();

                if((tmpCoreInstanceResult.getFieldInvocationsOfCurrentCoreMap().isEmpty() && tmpCoreInstanceResult.getDeobfuscationType() == SmaliLayer.DEOBFUSCATION_TYPE_FIELD) || (tmpCoreInstanceResult.getDeobfuscationRoutinesList().isEmpty()  && tmpCoreInstanceResult.getDeobfuscationType() == SmaliLayer.DEOBFUSCATION_TYPE_METHOD)){
                    System.out.println("possible false positive, plz verify manually if this class contain some kind of literal obfuscation: "+classDescriptor);
                    if(tmpCoreInstanceResult.getDeobfuscationType() == SmaliLayer.DEOBFUSCATION_TYPE_FIELD){
                        System.out.println("DeobfuscationType: SmaliLayer.DEOBFUSCATION_TYPE_FIELD");
                    }else if(tmpCoreInstanceResult.getDeobfuscationType() == SmaliLayer.DEOBFUSCATION_TYPE_METHOD){
                        System.out.println("DeobfuscationType: SmaliLayer.DEOBFUSCATION_TYPE_METHOD");
                    }else{
                        System.out.println("DeobfuscationType: SmaliLayer.DEOBFUSCATION_TYPE_UNKOWN");
                    }

                    continue;
                }
                this.coreInstanceResultsMap.put(classDescriptor,tmpCoreInstanceResult);
                this.addGlobalClassRefsList(tmpCoreInstanceResult.getClassNamesOfRelevantClasses());



            }while (remainingCoresSet.size() > 0);



            // no more remaining clinits, we are finished
        System.out.println("Found "+coreInstanceResultsMap.size() +" possible Core-Instances...");
        this.startTemplateBuilding(dxFile,coreInstanceResultsMap,apkName,false);



            if(SmaliLayer.UNKNOWN_DEOBFUSCATION_TYPE_CNT > 0){
               System.out.println("The following classes are from DeobfuscationType: UNKNOWN");
                System.out.println("Identified DeobfuscationType: UNKNOWN");
                for(String classDescriptor : SmaliLayer.UNKNOWN_DEOBFUSCATION_CLASSES_LIST){
                    System.out.println(classDescriptor);
                }

                System.out.println("Plz verify if those classes are really obfuscated.");
                System.out.println("If they are obfuscated send the analyzed APK or SHA256 to admin-[at]-remoteshell-security.com");
                System.out.println("\n\n\n");

               /*

                System.out.println("identified method type core classes:");
                for(String classDescriptor : SmaliLayer.METHODS_DEBOFSCATION_TYPE_LIST){
                    System.out.println(classDescriptor);
                }


        System.out.println("\n\n\n\nidentified field type core classes:");
        for(String classDescriptor : SmaliLayer.FIELDS_DEBOFSCATION_TYPE_LIST){
            System.out.println(classDescriptor);
        }*/
            }


/*
        System.out.println("\n\n\n\nmissing core classes: " +SmaliLayer.VALIDATION_LIST.size()+"\n\n");
        for(String classDescriptor : SmaliLayer.VALIDATION_LIST){
            System.out.println(classDescriptor);}*/

            System.out.println("------------------------");



    }

    private void addGlobalClassRefsList(LinkedHashSet<String> tmpClassesSet){
       // System.out.println("Size of remainings before: "+remainingCoresMap.size());
        removeNonCoreClasses(tmpClassesSet);
        globalClassesSet.addAll(tmpClassesSet);
        //System.out.println("Size of remainings after: "+remainingCoresMap.size());
    }


    private void removeNonCoreClasses(LinkedHashSet<String> tmpClassesSet){

        //for(Iterator<SmaliUnit> smaliUnitIterator = remainingCoresMap.keySet().iterator(); smaliUnitIterator.hasNext();){
        for(Iterator<SmaliUnit> smaliUnitIterator = remainingCoresSet.iterator(); smaliUnitIterator.hasNext();){
            SmaliUnit tmpSmaliUnit = smaliUnitIterator.next();
            if(tmpClassesSet.contains(tmpSmaliUnit.getClassDefinition().getType())){
               // System.out.println("is already there..will be removed");
                //remainingCoresMap.remove(tmpSmaliUnit);
                // i need here an additional metric for removing, maybe the length or some other stuff
                //smaliUnitIterator.remove();
            }
        }
        /*

        this solution is not possible due  java.util.ConcurrentModificationException, because while iterating with foreach the iterating data strcuture is blocked for modifications

        for(SmaliUnit tmpSmaliUnit : remainingCoresMap.keySet()){
            if(tmpClassesSet.contains(tmpSmaliUnit.getClassDefinition().getType())){
                System.out.println("is already there..will be removed");
                remainingCoresMap.remove(tmpSmaliUnit);
            }
        }*/

    }


    public void createStaticBackwardSlice(DexFile dexFile,String apkName,String backwardSliceName,ClassDef backwardSliceClassDef){
        this.dissassemble = false;


        System.out.println("------------------------");
        System.out.println("starting with the static backward slice of method "+backwardSliceName+ " ...");

        SmaliUnit firstSliceUnit = backwardSliceSet.first();
        if(firstSliceUnit == null){
            SmaliLayer.printUnknownError();
        }
        backwardSliceSet.remove(firstSliceUnit);

        SmaliLayerUtils.setCoreClassPackage(firstSliceUnit.getClassDefinition());

        // get Method from ClassDef -->
        // getClassDefForBackwardSlice
        //
        // the results for the temporary analyzed core instance
        CoreInstanceResults tmpCoreInstanceResult  = this.startMethodInstanceAnalyzing(dexFile,firstSliceUnit);

        // classDescriptor for the
        /*
        String classDescriptor = tmpCoreInstanceResult.getCurrentCoreClassSmaliUnit().getClassDescriptor();

        this.coreInstanceResultsMap.put(classDescriptor,tmpCoreInstanceResult);
        this.addGlobalClassRefsList(tmpCoreInstanceResult.getClassNamesOfRelevantClasses());


        System.out.println("Found "+coreInstanceResultsMap.size() +" possible Core-Instances...");
        this.startTemplateBuilding(dexFile,coreInstanceResultsMap,apkName);



        if(SmaliLayer.UNKNOWN_DEOBFUSCATION_TYPE_CNT > 0) {
            System.out.println("The following classes are from DeobfuscationType: UNKNOWN");
            System.out.println("Identified DeobfuscationType: UNKNOWN");
            for (String classDescriptor2 : SmaliLayer.UNKNOWN_DEOBFUSCATION_CLASSES_LIST) {
                System.out.println(classDescriptor2);
            }

            System.out.println("Plz verify if those classes are really obfuscated.");
            System.out.println("If they are obfuscated send the analyzed APK or SHA256 to admin-[at]-remoteshell-security.com");
            System.out.println("\n\n\n");

               /*

                System.out.println("identified method type core classes:");
                for(String classDescriptor : SmaliLayer.METHODS_DEBOFSCATION_TYPE_LIST){
                    System.out.println(classDescriptor);
                } /
        }*/

    }


    public void startObfuscationHeuristic(String apkName,boolean dissassembleTemplate, String backwardSliceName, boolean defaultRun, boolean showPossibleDecryptionRoutine){
        DexFile dexFile = getDexFile2Analyze(apkName);


        if(dexFile == null){
            System.out.println("Error while loading DEX File");
        }

        if(backwardSliceName.length() > 0)
            SmaliLayer.isOnlyBackwardSlicePrinted = true;

        ClassInitializerFilter.getListOfMethodInvocations(dexFile);
        filterdClinitOfDex = new ClassInitializerFilter(dexFile,defaultRun,showPossibleDecryptionRoutine);

        if(showPossibleDecryptionRoutine){

        }

        if(SmaliLayer.isOnlyBackwardSlicePrinted){
            ClassDef backwardSliceClassDef = null;
            //System.out.println("Dies ist der Parameter: "+backwardSliceName);

            if(SmaliLayerUtils.hasClassName(backwardSliceName)){

                //backwardSliceClassDef = ClassInitializerFilter.getClassDefForBackwardSlice(dexFile,backwardSliceName);
                boolean foundMethod =  filterdClinitOfDex.getClassDefForBackwardSlice(dexFile,backwardSliceName);
                if(foundMethod){

                    backwardSliceSet = filterdClinitOfDex.getBackwardSliceSet();

                }else{
                    System.out.println("Couldn't find method :"+backwardSliceName);
                    System.out.println("Did you use the correct format: L<class>;-><methodname>(<params>)<ReturnValue> with single quotes");
                    exit(1);
                }


            }else{

                // right now not really used...
                remainingBSSet = ClassInitializerFilter.getAllClassDefsForBackwardSlice(dexFile,backwardSliceName);
                backwardSliceClassDef = remainingBSSet.pollFirst();
            }




            //remainingBSSet.addAll(filterdClinitOfDex.getAllImplementedClassesOfDex());
            //
            //createStaticBackwardSlice(dexFile,apkName, backwardSliceName, null);
            remainingCoresSet.addAll(ClassInitializerFilter.getInitialRemainingBackwardSlice());
            createCore(dexFile,dissassembleTemplate,apkName);

        }else{




        List<DeobUnit> tmpDeobUnitList = IdentifyDeobRoutines.getDeobRoutinesSortetByInvocation(MAP_OF_DEOB_ROUTINES);
        if(!tmpDeobUnitList.isEmpty() && tmpDeobUnitList != null){


        if(tmpDeobUnitList.get(0).getNumberOfInvocations() >=300){
            startTemplateWithoutCore(dexFile,apkName,tmpDeobUnitList);
        }
        }



        // initialize the local TreeMap of Cores which are identified in this DEX
        //remainingCoresMap.putAll(filterdClinitOfDex.getInitialRemainingCoresMap());
        remainingCoresSet.addAll(filterdClinitOfDex.getInitialRemainingCoresSet());

        if(remainingCoresSet.isEmpty()){
            if(SmaliLayer.MAP_OF_DEOB_ROUTINES.isEmpty()){
                System.out.println("We couldn't identify any deobfuscation routine....");
                SmaliLayer.printUnknownError();
            }
        }



            //System.out.println("läuft ....");
            createCore(dexFile,dissassembleTemplate,apkName);
        }



    }

}




