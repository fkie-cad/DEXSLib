package dexslib.util;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.ReferenceType;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.instruction.formats.*;
import org.jf.util.Hex;

import java.util.ArrayList;

/**
 * This class offers the possibility to get everything from an instruction
 * const-string v1,"refernece/value"
 * opcode  register value

 */

public class SpecialisedInstruction implements Instruction {

    protected Instruction specialInstruction;
    private int numOfRegs;
    private int startReg;
    private int numOfRegsInMethod; // the total number of register's used in the method
    private int numOfParameterRegs = 1; // we use also the p as register  and p0 is used for this

    private int regA =-1; // first register used by instruction
    private int regB =-1; // second register used by instruction
    private int regC =-1; // third register used by instruction
    private int regD =-1; // cont'd
    private int regE =-1;
    private int regF =-1;
    private int regG =-1;

    private  int literal = -1;
    private  short literalShort = -1;
    private  long literalLong = -1;

    private boolean rangedRegisters = false; // every instruction which could have more than one register has brackets {}


    private ArrayList<Integer> regList = new ArrayList<Integer>();
    private ArrayList<Integer> arrayElemntList = new ArrayList<Integer>();
    private int arrayWidth;
    private boolean startByRegC;
    private String formatName = "";
    private boolean isArray;
    private boolean isInt;



    public SpecialisedInstruction(Instruction instruction, int totalNumberOfRegsInMethod, int  numOfParameter) {
        //specialInstruction = instruction;
        this.numOfRegs = 0; // default Value
        this.startReg = -1;
        this.startByRegC = false;
        this.numOfRegsInMethod = totalNumberOfRegsInMethod;
        this.numOfParameterRegs = numOfParameterRegs + numOfParameter;
        this.isArray = false;
        this.isInt = false;

        /*
         This part has to be refactored to used the following classes
         https://github.com/JesusFreke/smali/tree/master/dexlib2/src/main/java/org/jf/dexlib2/iface/instruction

         This means for Instructions which are using only one register we use a method call later something like this
         ((OneRegisterInstruction)instruction).getRegisterA();
         */



        switch (instruction.getOpcode().format) {
            case Format10t:
                specialInstruction = ((Instruction10t) instruction);
                this.formatName = "Format10t";
                break;
            case Format10x:
                specialInstruction = ((Instruction10x) instruction);
                this.formatName = "Format10x";
                break;
            case Format11n:
                specialInstruction = ((Instruction11n) instruction);
                this.numOfRegs = 1;
                this.regA = ((Instruction11n) instruction).getRegisterA();
                this.literal =  ((Instruction11n) instruction).getNarrowLiteral();
                this.literalLong =  ((Instruction11n) instruction).getWideLiteral();
                this.isInt = true;
                this.formatName = "Format11n";
                break;
            case Format11x:
                specialInstruction = ((Instruction11x) instruction);
                this.numOfRegs = 1;
                this.regA = ((Instruction11x) instruction).getRegisterA();
                this.formatName = "Format11x";
                break;
            case Format12x:
                specialInstruction = ((Instruction12x) instruction);
                this.numOfRegs = 2;
                this.regA = ((Instruction12x) instruction).getRegisterA();
                this.regB = ((Instruction12x) instruction).getRegisterB();
                this.formatName = "Format12x";
                break;
            case Format20bc:
                specialInstruction = ((Instruction20bc) instruction);
                this.formatName = "Format20bc";
                break;
            case Format20t:
                specialInstruction = ((Instruction20t) instruction);
                this.formatName = "Format20t";
                break;
            case Format21c:
                specialInstruction = ((Instruction21c) instruction);
                this.numOfRegs = 1;
                this.regA = ((Instruction21c) instruction).getRegisterA();
                this.formatName = "Format21c";
                break;
            case Format21ih:
                specialInstruction = ((Instruction21ih) instruction);
                this.numOfRegs = 1;
                this.regA = ((Instruction21ih) instruction).getRegisterA();
                this.literal =  ((Instruction21ih) instruction).getNarrowLiteral();
                this.literalLong =  ((Instruction21ih) instruction).getWideLiteral();
                this.formatName = "Format21ih";
                break;
            case Format21lh:
                specialInstruction = ((Instruction21lh) instruction);
                this.numOfRegs = 1;
                this.regA = ((Instruction21lh) instruction).getRegisterA();
                this.formatName = "Format21lh";
                break;
            case Format21s:
                specialInstruction = ((Instruction21s) instruction);
                this.numOfRegs = 1;
                this.regA = ((Instruction21s) instruction).getRegisterA();
                this.literal =  ((Instruction21s) instruction).getNarrowLiteral();
                this.literalLong =  ((Instruction21s) instruction).getWideLiteral();
                this.formatName = "Format21s";
                break;
            case Format21t:
                specialInstruction = ((Instruction21t) instruction);
                this.numOfRegs = 1;
                this.regA = ((Instruction21t) instruction).getRegisterA();
                this.formatName = "Format21t";
                break;
            case Format22b:
                specialInstruction = ((Instruction22b) instruction);
                this.regA = ((Instruction22b) instruction).getRegisterA();
                this.regB = ((Instruction22b) instruction).getRegisterB();
                this.literal =  ((Instruction22b) instruction).getNarrowLiteral();
                this.literalLong =  ((Instruction22b) instruction).getWideLiteral();
                this.numOfRegs = 2;
                this.formatName = "Format22b";
                break;
            case Format22c:
                specialInstruction = ((Instruction22c) instruction);
                this.numOfRegs = 2;
                this.regA = ((Instruction22c) instruction).getRegisterA();
                this.regB = ((Instruction22c) instruction).getRegisterB();
                this.formatName = "Format22c";
                break;
            case Format22cs:
                specialInstruction = ((Instruction22cs) instruction);
                this.numOfRegs = 2;
                this.regA = ((Instruction22cs) instruction).getRegisterA();
                this.regB = ((Instruction22cs) instruction).getRegisterB();
                this.formatName = "Format22cs";
                break;
            case Format22s:
                specialInstruction = ((Instruction22s) instruction);
                this.numOfRegs = 2;
                this.regA = ((Instruction22s) instruction).getRegisterA();
                this.regB = ((Instruction22s) instruction).getRegisterB();
                this.literal =  ((Instruction22s) instruction).getNarrowLiteral();
                this.literalLong =  ((Instruction22s) instruction).getWideLiteral();
                this.formatName = "Format22s";
                break;
            case Format22t:
                specialInstruction = ((Instruction22t) instruction);
                this.numOfRegs = 2;
                this.regA = ((Instruction22t) instruction).getRegisterA();
                this.regB = ((Instruction22t) instruction).getRegisterB();
                this.formatName = "Format22t";
                break;
            case Format22x:
                specialInstruction = ((Instruction22x) instruction);
                this.numOfRegs = 2;
                this.regA = ((Instruction22x) instruction).getRegisterA();
                this.regB = ((Instruction22x) instruction).getRegisterB();
                this.formatName = "Format22x";
                break;
            case Format23x:
                specialInstruction = ((Instruction23x) instruction);
                this.numOfRegs = 3;
                this.regA = ((Instruction23x) instruction).getRegisterA();
                this.regB = ((Instruction23x) instruction).getRegisterB();
                this.regC = ((Instruction23x) instruction).getRegisterC();
                this.formatName = "Format23x";
                break;
            case Format30t:
                specialInstruction = ((Instruction30t) instruction);
                this.formatName = "Format30t";
                break;
            case Format31c:
                specialInstruction = ((Instruction31c) instruction);
                this.numOfRegs = 1;
                this.regA = ((Instruction31c) instruction).getRegisterA();
                this.formatName = "Format31c";
                break;
            case Format31i:
                specialInstruction = ((Instruction31i) instruction);
                this.numOfRegs = 1;
                this.regA = ((Instruction31i) instruction).getRegisterA();
                this.literal = ((Instruction31i) instruction).getNarrowLiteral();
                this.literalLong = ((Instruction31i) instruction).getWideLiteral();
                this.isInt = true;
                this.formatName = "Format31i";
                break;
            case Format31t:
                specialInstruction = ((Instruction31t) instruction);
                this.numOfRegs = 1;
                this.regA = ((Instruction31t) instruction).getRegisterA();
                this.formatName = "Format31t";
                break;
            case Format32x:
                specialInstruction = ((Instruction32x) instruction);
                this.numOfRegs = 2;
                this.regA = ((Instruction32x) instruction).getRegisterA();
                this.regB = ((Instruction32x) instruction).getRegisterB();
                this.formatName = "Format32x";
                break;
            case Format35c:
                specialInstruction = ((Instruction35c) instruction);
                this.numOfRegs = ((Instruction35c) instruction).getRegisterCount();
                this.regC = ((Instruction35c) instruction).getRegisterC();
                this.regD = ((Instruction35c) instruction).getRegisterD();
                this.regE = ((Instruction35c) instruction).getRegisterE();
                this.regF = ((Instruction35c) instruction).getRegisterF();
                this.regG = ((Instruction35c) instruction).getRegisterG();
                this.startByRegC = true;
                this.formatName = "Format35c";
                break;
            case Format35mi:
                specialInstruction = ((Instruction35mi) instruction);
                this.numOfRegs = ((Instruction35mi) instruction).getRegisterCount();
                this.regC = ((Instruction35mi) instruction).getRegisterC();
                this.regD = ((Instruction35mi) instruction).getRegisterD();
                this.regE = ((Instruction35mi) instruction).getRegisterE();
                this.regF = ((Instruction35mi) instruction).getRegisterF();
                this.regG = ((Instruction35mi) instruction).getRegisterG();
                this.startByRegC = true;
                this.formatName = "Format35mi";
                break;
            case Format35ms:
                specialInstruction = ((Instruction35ms) instruction);
                this.numOfRegs = ((Instruction35ms) instruction).getRegisterCount();
                this.regC = ((Instruction35ms) instruction).getRegisterC();
                this.regD = ((Instruction35ms) instruction).getRegisterD();
                this.regE = ((Instruction35ms) instruction).getRegisterE();
                this.regF = ((Instruction35ms) instruction).getRegisterF();
                this.regG = ((Instruction35ms) instruction).getRegisterG();
                this.startByRegC = true;
                this.formatName = "Format35ms";
                break;
            case Format3rc:
                specialInstruction = ((Instruction3rc) instruction);
                this.numOfRegs = ((Instruction3rc) instruction).getRegisterCount();
                this.startReg = ((Instruction3rc) instruction).getStartRegister();
                this.formatName = "Format3rc";
                break;
            case Format3rmi:
                specialInstruction = ((Instruction3rmi) instruction);
                this.numOfRegs = ((Instruction3rmi) instruction).getRegisterCount();
                this.startReg = ((Instruction3rmi) instruction).getStartRegister();
                this.formatName = "Format3rmi";

                break;
            case Format3rms:
                specialInstruction = ((Instruction3rms) instruction);
                this.numOfRegs = ((Instruction3rms) instruction).getRegisterCount();
                this.startReg = ((Instruction3rms) instruction).getStartRegister();
                this.formatName = "Format3rms";
                break;
            case Format45cc:
                specialInstruction = ((Instruction45cc) instruction);
                this.numOfRegs = ((Instruction45cc) instruction).getRegisterCount();
                this.regC = ((Instruction45cc) instruction).getRegisterC();
                this.regD = ((Instruction45cc) instruction).getRegisterD();
                this.regE = ((Instruction45cc) instruction).getRegisterE();
                this.regF = ((Instruction45cc) instruction).getRegisterF();
                this.regG = ((Instruction45cc) instruction).getRegisterG();
                this.startByRegC = true;
                this.formatName = "Format45cc";
                break;
            case Format4rcc:
                specialInstruction = ((Instruction4rcc) instruction);
                this.numOfRegs = ((Instruction4rcc) instruction).getRegisterCount();
                this.startReg = ((Instruction4rcc) instruction).getStartRegister();
                this.formatName = "Format4rcc";
                break;
            case Format51l:
                specialInstruction = ((Instruction51l) instruction);
                this.numOfRegs = 1;
                this.regA = ((Instruction51l) instruction).getRegisterA();
                this.formatName = "Format51l";
                break;
            case PackedSwitchPayload:
                specialInstruction = ((PackedSwitchPayload) instruction);
                this.formatName = "PackedSwitchPayload";
                break;
            case SparseSwitchPayload:
                specialInstruction = ((SparseSwitchPayload) instruction);
                this.formatName = "SparseSwitchPayload";
                break;
            case ArrayPayload:
                specialInstruction = ((ArrayPayload) instruction);
                this.isArray = true;


                this.arrayWidth = ((ArrayPayload) instruction).getElementWidth();
                this.formatName = "ArrayPayload";
                break;
            case UnresolvedOdexInstruction:
                this.formatName = "UnresolvedOdexInstruction";
                break;
            default:
                specialInstruction = instruction;
        }
    }



    public int getRegNumbers(){
        return this.numOfRegs;
    }

    public int getStartReg(){
        return this.startReg;
    }

    public String getFormatName(){
        return  this.formatName;
    }


    /**
     *
     * @param reg the register number beginning by 1 for the first register. It has nothing to do with the value itself.
     * @return
     */
    public int getRegister(int reg){
        /*if(reg > numOfRegs){
            return -1;
        }*/

        if(this.startByRegC == true){
            reg = reg +2;
        }

        if(startReg != -1){
                return startReg+reg-1;
        }
        
        switch (reg){
            case 1:
                return this.regA;
            case 2:
                return this.regB;
            case 3:
                return this.regC;
            case 4:
                return this.regD;
            case 5:
                return this.regE;
            case 6:
                return this.regF;
            case 7:
                return this.regG;
            default:
                return -1;
        }
    }

    /**
     *
     * @param nthReg
     * @return the nth register as string; returns null if no register
     */
    public String getNthRegisterAsString(int nthReg){
        return getRegisterWithPrefix(getRegister(nthReg));
    }


    /**
     *
     * @return  start by 1
     */
    public ArrayList<String> registerOfInstructionAsList(){
        ArrayList<String> stringList = new  ArrayList<String>();

        for(int nthReg = 1; nthReg <= this.getRegNumbers(); nthReg++){
            stringList.add(getNthRegisterAsString(nthReg));
        }


        return stringList;
    }


    /**
     *
     * @param nthReg
     * @return the nth register as string; returns null if no register

    public ReferenceType getNthRegisterAsReference(int nthReg){
        return getRegister(nthReg);
    }


    public ArrayList<ReferenceType> registerOfInstructionAsReferenceList(){
        ArrayList<ReferenceType> referenceTypeArrayListList = new  ArrayList<ReferenceType>();

        for(int nthReg = 1; nthReg <= this.getRegNumbers(); nthReg++){

            referenceTypeArrayListList.add(getNthRegisterAsReference(nthReg));
        }


        return referenceTypeArrayListList;
    }*/


    /**
     * This methods determines if we have a local register or a register used for parameters or the global this-operator as p0;
     * @return
     *
     * The register value can start by 0
     */
    private String getRegisterWithPrefix(int regValue){

        int localRegValuesMax = this.numOfRegsInMethod - this.numOfParameterRegs-1; // we start counting by 0
        if(regValue > (localRegValuesMax+1)){
            int pValue = regValue- localRegValuesMax -1;
            return "p"+pValue;
        }

        return "v"+regValue;

    }


    private static final char CONTROL_LIMIT = ' ';
    private static final char PRINTABLE_LIMIT = '\u007e';
    private static final char[] HEX_DIGITS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public static String toPrintableRepresentation(String source) {

        if( source == null ) return null;
        else {

            final StringBuilder sb = new StringBuilder();
            final int limit = source.length();
            char[] hexbuf = null;

            int pointer = 0;

            //sb.append('"');

            while( pointer < limit ) {

                int ch = source.charAt(pointer++);

                switch( ch ) {

                    case '\0': sb.append("\\0"); break;
                    case '\t': sb.append("\\t"); break;
                    case '\n': sb.append("\\n"); break;
                    case '\r': sb.append("\\r"); break;
                    case '\"': sb.append("\\\""); break;
                    case '\'': sb.append("\\\'"); break;
                    case '\\': sb.append("\\\\"); break;

                    default:
                        if( CONTROL_LIMIT <= ch && ch <= PRINTABLE_LIMIT ) sb.append((char)ch);
                        else {

                            sb.append("\\u");

                            if( hexbuf == null )
                                hexbuf = new char[4];

                            for( int offs = 4; offs > 0; ) {

                                hexbuf[--offs] = HEX_DIGITS[ch & 0xf];
                                ch >>>= 4;
                            }

                            sb.append(hexbuf, 0, 4);
                        }
                }
            }

            //return sb.toString();
            String printableString = sb.toString();
            printableString = printableString.replace("\\\"","\""); // for now it is easier to handle this in the patching-script, in a future release all of this will be handled here
            return printableString;
        }
    }



    public String getValue() {

        if(getReferenceType() != ReferenceType.NONE ){

            if(this.getReferenceType() == ReferenceType.STRING){
                //return this.parseQuotedString("\""+((ReferenceInstruction) specialInstruction).getReference().toString()+"\"");
                //return new ImmutableStringReference(this.parseQuotedString("\""+((ReferenceInstruction) specialInstruction).getReference().toString()+"\""));
                //byte[] bytes = ((ReferenceInstruction) specialInstruction).getReference().toString().getBytes();
                //String tmpString = new String (bytes,StandardCharsets.UTF_16);
                //System.out.println("parsed: "+this.parseQuotedString("\""+tmpString+"\""));
                return "\""+toPrintableRepresentation(((ReferenceInstruction) specialInstruction).getReference().toString())+"\"";

            }


            return ((ReferenceInstruction) specialInstruction).getReference().toString();
        }



        if(this.getOpcode().setsRegister() && !this.getOpcode().name.startsWith("move")){

            if(Hex.u8(this.literalLong).substring(0,8).contains("0")){
                return "0x"+Hex.u4(this.literal);
            }else{
                return "0x"+Hex.u8(this.literalLong);
            }

        }

        return "";
    }



    public String getValueWithoutExcapedString() {

        if(getReferenceType() != ReferenceType.NONE){

            if(this.getReferenceType() == ReferenceType.STRING){
                //return this.parseQuotedString("\""+((ReferenceInstruction) specialInstruction).getReference().toString()+"\"");
                //return new ImmutableStringReference(this.parseQuotedString("\""+((ReferenceInstruction) specialInstruction).getReference().toString()+"\""));
                //byte[] bytes = ((ReferenceInstruction) specialInstruction).getReference().toString().getBytes();
                //String tmpString = new String (bytes,StandardCharsets.UTF_16);
                //System.out.println("parsed: "+this.parseQuotedString("\""+tmpString+"\""));
                return toPrintableRepresentation(((ReferenceInstruction) specialInstruction).getReference().toString());

            }


            return ((ReferenceInstruction) specialInstruction).getReference().toString();
        }

        if(this.getOpcode().setsRegister() && !this.getOpcode().name.startsWith("move")){

            if(Hex.u8(this.literalLong).substring(0,8).contains("0")){
                return "0x"+Hex.u4(this.literal);
            }else{
                return "0x"+Hex.u8(this.literalLong);
            }

        }

        return "";
    }


    public String getRegistersAsString(){
        String startBracket = "";
        String endBracket = "";

        if(this.hasRegister() == false){
            return "";
        }

        String registers = "";
        for(int i =1; i <= this.numOfRegs; i++){
            registers = registers+this.getRegisterWithPrefix(this.getRegister(i))+", ";
        }


        /*if(registers.lastIndexOf(",") == registers.length()-1){
            registers = registers.substring(0,registers.length()-1);
        }*/

        if(this.startByRegC){
            startBracket="{";
            endBracket="},";
            registers = registers.substring(0,registers.length()-2);
        }else{
            registers = registers.substring(0,registers.length()-1);
            if(this.getOpcode().name.startsWith("mov")){
                registers = registers.substring(0,registers.length()-1);
            }
        }

        return startBracket+registers+endBracket; // no registers
    }




    public boolean hasValue(){
        if (getReferenceType() != ReferenceType.NONE){
            return true;
        }else{

            if(this.getValue().length() == 0){
                return false;
            }else{
                return true;
            }

        }
    }


    public boolean hasRegister(){
        if(this.numOfRegs == 0){
            return false;
        }
        return true;
    }


    @Override
    public Opcode getOpcode() {
        return specialInstruction.getOpcode();
    }

    @Override
    public int getCodeUnits() {
        return specialInstruction.getCodeUnits();
    }


    public int getReferenceType() {
        return specialInstruction.getOpcode().referenceType;
    }

    @Override
    public String toString(){
       String tmpReturnString = specialInstruction.getOpcode().name+" "+this.getRegistersAsString()+" "+this.getValue();
       return tmpReturnString.trim();
    }


    public boolean isMethodInvocation(){


        if(this.getOpcode().referenceType == ReferenceType.METHOD){
            return true;
        }

        /*
        if(this.getOpcode().name.startsWith("invoke")){
            return true;
        }*/

        return false;
    }


    /**
     *
     * V 	void - can only be used for return types
     * Z 	boolean
     * B 	byte
     * S 	short
     * C 	char
     * I 	int
     * J 	long (64 bits)
     * F 	float
     * D 	double (64 bits)
     *
     * Objects take the form Lpackage/name/ObjectName;
     *
     *  Arrays take the form [I - this would be an array of ints with a single dimension. i.e. int[] in java. For arrays with multiple dimensions,
     *  you simply add more [ characters. [[I = int[][], [[[I = int[][][], etc. (Note: The maximum number of dimensions you can have is 255).
     *
     *
     * @return
     *
    public String getInstructionBaseType(){
        String baseType = "NONE";

        if(this.getOpcode().setsRegister()){

            if(this.getReferenceType() == ReferenceType.STRING){
                return "Ljava/lang/String;";
            }

            if(this.getOpcode().name.startsWith(""))

        }

        return baseType;
    }*/

    /**
     *
     * @return the int value otherwise -1 if its wrong
     */
    public int getIntValue(){
        if(this.hasValue() && this.isInt ) {

            //int decimal = Integer.parseInt(this.literal, 16);
            return this.literal;
        }
        return -1;
    }


    /**
     *  This returns the full method name the same as method.toString();
     * @return e.g. Ljava/lang/String;->equals(Ljava/lang/Object;)Z
     */
    public String getFullMethodName(){
        String methodName = "";
        if(this.isMethodInvocation() == false){
            return null;
        }
        methodName = this.getValue();
        return methodName;
    }


    /**
     *
     * @return only the StringName of the Method for like method.getName() (everything after the ->)
     *
     * e.g. equals(Ljava/lang/Object;)Z
     */
    public String getMethodName(){
        String methodName = "";
        if(this.isMethodInvocation() == false){
            return null;
        }
        methodName = SmaliLayerUtils.getCallingMethodName(this.getValue());
        return methodName;
    }

    public String getMethodReturnType(){
        String methodName = "";
        if(this.isMethodInvocation() == false){
            return null;
        }
        methodName = SmaliLayerUtils.getReturnTypeOfFullMethodName(this.getValue());
        return methodName;
    }


}