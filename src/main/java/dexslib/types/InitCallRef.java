package dexslib.types;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel baier on 26.01.17.
 *
 * This class stores all calls to constructors ({@literal <}init{@literal >}(...) of classes from the current analyzed clinit-method
 */
public class InitCallRef {


    private Iterable<? extends Instruction> instructionsOfClinit; // the clinit instructions
    private String initCallRefName; // the name of the initCall
    private Opcode instructionOpcode; // the opcode of the instruction which is calling the <init>
    private Map<String,Opcode> coreInstanceInitCallsMap= new  HashMap<String,Opcode>();

    public InitCallRef(Iterable<? extends Instruction> passedInstructionsOfClinit,String passedInitCallRefName,Opcode passedOnstructionOpcode){
        this.instructionsOfClinit = passedInstructionsOfClinit;
        if(passedInitCallRefName != null || passedOnstructionOpcode != null){


        this.initCallRefName = passedInitCallRefName;
        this.instructionOpcode = passedOnstructionOpcode;
        this.coreInstanceInitCallsMap.put(passedInitCallRefName,passedOnstructionOpcode);
        }
    }


    public Iterable<? extends Instruction> getInstructionsOfClinit() {
        return instructionsOfClinit;
    }

    public void setInstructionsOfClinit(Iterable<? extends Instruction> instructionsOfClinit) {
        this.instructionsOfClinit = instructionsOfClinit;
    }

    public String getInitCallRefName() {
        return initCallRefName;
    }

    public void setInitCallRefName(String initCallRefName) {
        this.initCallRefName = initCallRefName;
    }

    public Opcode getInstructionOpcode() {
        return instructionOpcode;
    }

    public void setInstructionOpcode(Opcode instructionOpcode) {
        this.instructionOpcode = instructionOpcode;
    }

    public Map<String, Opcode> getCoreInstanceInitCallsMap() {
        return coreInstanceInitCallsMap;
    }

    public void setCoreInstanceInitCallsMap(Map<String, Opcode> coreInstanceInitCallsMap) {
        this.coreInstanceInitCallsMap = coreInstanceInitCallsMap;
    }


    public int numOfInitCalls(){
        return this.coreInstanceInitCallsMap.size();
    }
}
