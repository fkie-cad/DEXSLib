package dexslib.types;

import dexslib.util.CoreUtils;
import org.jf.dexlib2.Opcode;

/**
 * Created by Daniel Baier on 02.12.16.
 */
public class ClassRef implements Comparable<ClassRef>{

    private String callingRef;
    private String callingClass;
    private String callingMethod;
    private String opcodeSmaliName;
    private String opcodeName;
    private Opcode instructionOpcode;

    private int numOfUsage;

    /**
     *
     * @param callingRef
     * @param instructionOpcode
     */
    public ClassRef(String callingRef, Opcode instructionOpcode){

        if(!callingRef.contains("->") && callingRef.endsWith(";")){
            // this is for the case, we want to check for a new-instance appearance and want the default constructor of it
            this.callingRef = callingRef+"-><init>()V";;
        }else{
            this.callingRef = callingRef;
        }


        this.callingClass = this.getCallingClassName(callingRef);
        this.callingMethod = this.getCallingMethodOrFieldName(callingRef);
        this.opcodeName = instructionOpcode.name();
        this.opcodeSmaliName= instructionOpcode.name;
        this.instructionOpcode = instructionOpcode;
        this.numOfUsage = 1;
    }


    public int getNumOfUsage() {
        return numOfUsage;
    }


    public Opcode getInstructionOpcode() {
        return  this.instructionOpcode;
    }

    public void setNumOfUsage(int numOfUsage) {
        this.numOfUsage = numOfUsage;
    }

    public void increaseNumOfUsage() {
        this.numOfUsage++;
    }




    private String getCallingClassName(String callingRef){

        if(callingRef == null || callingRef.length() == 0){
            return "";
        }

        callingRef = CoreUtils.removeArraySign(callingRef); // remove array symbol here

        return callingRef.split("->")[0];

    }


    private String getCallingMethodOrFieldName(String callingRef){
        callingRef = CoreUtils.removeArraySign(callingRef); // remove array symbol here
        if(callingRef == null || callingRef.length() == 0){
            return "";
        }

        if(!callingRef.contains("->") && callingRef.endsWith(";")){
            if(callingRef.contains(":")){
                return callingRef.split(":")[0];
            }else{
                return callingRef;
            }
        }

        return callingRef.split("->")[1];

    }


    public String getCallingRef() {
        return callingRef;
    }

    public void setCallingRef(String callingRef) {
        this.callingRef = callingRef;
    }

    public String getCallingClass() {
        return callingClass;
    }

    public void setCallingClass(String callingClass) {
        this.callingClass = callingClass;
    }

    public String getCallingMethod() {
        return callingMethod;
    }

    public void setCallingMethod(String callingMethod) {
        this.callingMethod = callingMethod;
    }

    public String getOpcodeSmaliName() {
        return opcodeSmaliName;
    }

    public void setOpcodeSmaliName(String opcodeSmaliName) {
        this.opcodeSmaliName = opcodeSmaliName;
    }

    public String getOpcodeName() {
        return opcodeName;
    }

    public void setOpcodeName(String opcodeName) {
        this.opcodeName = opcodeName;
    }



    @Override
    public int compareTo(ClassRef o) {
        return o.getNumOfUsage() - this.getNumOfUsage();
    }

}
