package dexslib.core.analyse;

//import DEXSlib.main.SmaliLayer;
import dexslib.SmaliLayer;
import dexslib.util.CoreUtils;
import dexslib.util.SmaliLayerUtils;
import org.jf.dexlib2.Format;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.ReferenceType;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used for the analysing of the method specified with the -i parameter.
 * The goal is to find all the parameters which where given to invoke this method.
 */

public class Method_BS_Analyse {

    //private Map<String,String> deobfuscatedStringsMapping = new HashMap<String,String>();
    private List<String> obfuscatedStrings = new ArrayList<String>();

    private int analyzeMethodInvocation(DexFile dx, String sliceName){
        int posInListOfUsageOfRelevantMethods = 0;
        String tmpNameOfRoutine = "";

        while(true){


            //CoreClassRef designatedDeobfuscationRoutine = getDesignatedDeobfuscationRoutine(posInListOfUsageOfRelevantMethods);

            if(dx == null){
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


                    if(tmpMethod.toString().equals(sliceName))

                    for (Instruction instruction : tmpMethod.getImplementation().getInstructions()) {
                        Opcode instructionOpcode = instruction.getOpcode();



                        // of couse this has to be improved because we can also have here an byte-array which is passed to the deobfuscationRoutine or
                        // we can have a method which returns a strings which is than passed to the deobfuscationRoutine
                        if(instructionOpcode.referenceType == ReferenceType.STRING){
                            String callingRef = ((ReferenceInstruction) instruction).getReference().toString();
                            tripleCounter = 1;

                            continue;
                            //System.out.println("possible obfuscated string: "+callingRef);
                        }


                        if(instructionOpcode.setsResult() || instructionOpcode.referenceType == ReferenceType.METHOD  ){
                            String callingRef = ((ReferenceInstruction) instruction).getReference().toString();

                            /*
                            if(designatedDeobfuscationRoutine.getCallingRef().equals(callingRef)){
                                cfg = true;
                                // System.out.println("possible DeobfuscationRoutine: "+callingRef);
                                //System.out.println("Format: "+Format.Format21c. );
                                if (tripleCounter == 1){
                                    tripleCounter = 2;
                                    tmpNameOfRoutine = callingRef;
                                }
                            }*/

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

                          /*  if(designatedDeobfuscationRoutine.getCoreClass().getClassDefinition().getType().equals(callingClassOfField)){
                                // System.out.println("Class: "+tmpClassDef.getType() +" Method: "+tmpMethod.getName()+" Opcode:"+instructionOpcode.name() +" instruction-ref: "+callingRef);
                                //System.out.println("\n\n");
                                this.deobfuscationRoutine = designatedDeobfuscationRoutine.getCallingRef();
                                cfg = false;
                                return posInListOfUsageOfRelevantMethods;
                            }*/


                        }

                        // for now we should also
                        else if(tripleCounter == 2 && instructionOpcode.name.equals("move-result-object")){ // here we check for a triple of instruction which can be iteresting for identifiy eobfuscation rutines
                            System.out.println("This is the possible deobfuscationRoutine: "+tmpNameOfRoutine);
                           // this.deobfuscationRoutine = designatedDeobfuscationRoutine.getCallingRef();
                            //this.doOnlyStaticMethodSlice = true;
                            return posInListOfUsageOfRelevantMethods;
                        }


                    }

                }
            }

            posInListOfUsageOfRelevantMethods++;
        }

    }



    public static int showPossibleDeobRoutineInvocations(Method possibleDeobRoutine){
        DexFile currentDexFile = SmaliLayer.getDexFile2Analyze();


        return 0;
    }




    public static boolean chooseCore(Method possibleDeobRoutine){

        if(Method_BS_Analyse.showPossibleDeobRoutineInvocations(possibleDeobRoutine) >  10){
            return false;
        }

        return false;
    }



}
