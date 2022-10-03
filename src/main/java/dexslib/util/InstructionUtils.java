package dexslib.util;

import dexslib.types.InstructionRef;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;

public class InstructionUtils {
    private InstructionRef instructionRef;


    public static SpecialisedInstruction getSpecificInstruction(Instruction instruction,Method method){

        if(method.getImplementation() == null){
            return null;
        }


        //((Instruction21c) instruction).getRegisterA()

        if (instruction != null) {

            SpecialisedInstruction specialisedInstruction = new SpecialisedInstruction(instruction,method.getImplementation().getRegisterCount(), method.getParameters().size());
            return specialisedInstruction;


        }else{
            return null;
        }




    }


    protected static Method getMethodForForwardSlice(ClassDef classDef, String methodOfClass){
        Method method = null;
        for(Method tmpMethod : classDef.getMethods()){

            if(method.toString().equals(methodOfClass)){
                return method;
            }
        }

        return method;
    }



        public InstructionRef smaliLine2InstructionRef(String smaliLine){

        if(smaliLine.length() == 0 || smaliLine == null){
            return null;
        }

        //this.instructionRef = new InstructionRef();
        // ClassDef classOfInst, Method method, SpecialisedInstruction specialisedInstruction

        return null;
        }


    //const-string v7, "guvf vf zl frperpg zrffntr...abobql pna ernq guvf unununun"

}
