package dexslib.types;

import org.jf.dexlib2.Opcode;


/**
 * Created by Daniel Baier on 02.12.16.
 */
public class CoreClassRef extends ClassRef {


    private SmaliUnit coreClass;



    public CoreClassRef(String callingRef,Opcode instructionOpcode,SmaliUnit coreClass){
       super(callingRef,instructionOpcode);
        this.coreClass = coreClass;

    }



    public SmaliUnit getCoreClass(){
        return this.coreClass;
    }


}
