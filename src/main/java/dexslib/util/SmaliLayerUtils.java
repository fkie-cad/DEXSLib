package dexslib.util;

import dexslib.types.SmaliUnit;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.instruction.Instruction;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Daniel Baier on 03.01.17.
 */
public class SmaliLayerUtils {

    // this variable contains the most common package name in order to remove later static linked libraries in the DEX
    private static String coreClassPackage = "";



    public static void setCoreClassPackage(ClassDef classDef){
        for(Method m : classDef.getMethods()){
            SmaliLayerUtils.coreClassPackage = SmaliLayerUtils.getCallingClassName(m.toString());
            break;
        }
    }


    public static String getLib(String smaliMethodOrInstance){
        String resultString = "";
        String tmpResultString[];
        if(smaliMethodOrInstance.startsWith("L")){
            tmpResultString = smaliMethodOrInstance.split("/");

            if(tmpResultString.length >= 2){
                resultString = tmpResultString[0] +"/" +tmpResultString[1];
                return resultString;
            }else {
                return "";
            }



        }
        return "";
    }

    /**
     *
     * @param classDefType is the name of the for instance Lcom/squareup/okhttp/HttpUrl$Builder;
     * @return
     */
    public static boolean isInnerClass(String classDefType){
        //int beginnClassName = classDefType.lastIndexOf("/");


        return classDefType.contains("$");
    }


    public static List<String> getFullMethodAsList(String fullMethodString){
        List<String> returnList = new ArrayList<String>();
        try {

            // it might happen that we encounter an smali disassembling error in which the function might look like
            // [B->clone()Ljava/lang/Object;
            if(fullMethodString.startsWith("[")){
                String instance = fullMethodString.substring(fullMethodString.indexOf("["), fullMethodString.indexOf("->"));
                returnList.add(instance);

            }else {


                String instance = fullMethodString.substring(fullMethodString.indexOf("L"), fullMethodString.indexOf("->"));
                returnList.add(instance);

            }

            String parameter = fullMethodString.substring(fullMethodString.indexOf("(") + 1, fullMethodString.indexOf(")"));
            returnList.add(parameter);
            String returnValue = fullMethodString.substring(fullMethodString.indexOf(")") + 1);
            returnList.add(returnValue);
        }catch (Exception e){
            System.err.println("Parsing error with:"+fullMethodString);
            System.err.println("Parsing error with:"+e.getMessage());
            System.err.println("Printing stack trace: ");
            e.printStackTrace();
            return  returnList;
        }

        return  returnList;
    }


    public static String getCoreClassPackage(){
        return coreClassPackage;
    }

    public static String getClassNameWithoutPackage(String classRef){
        String classRefWithoutPackage = "";
        if(classRef.contains("/")){
            classRefWithoutPackage = classRef.substring(classRef.lastIndexOf('/') + 1);
        }else if(classRef.startsWith("L")){
            classRefWithoutPackage = classRef.replace("L","");
        }else{
            classRefWithoutPackage = classRef;
        }


        return classRefWithoutPackage;
    }


    public static String getClassNameWithoutPackageFullVersion(String classRef){
        String classRefWithoutPackage = "";
        String tmp = classRef.split("->")[0];
        classRefWithoutPackage = tmp.substring(tmp.lastIndexOf('/') + 1);

        return classRefWithoutPackage;
    }


    public static String getCallingClassName(String callingRef){

        if(callingRef == null || callingRef.length() == 0){
            return "";
        }


        //return CoreUtils.removeArraySign(callingRef.split("->")[0]);
        return callingRef.split("->")[0];

    }


    /**
     *
     * @param callingRef is the full Methodname L{@literal <}class{@literal >}-{@literal >}methodname({@literal <}params{@literal >})ReturnType
     * @return
     */
    public static String getReturnTypeOfFullMethodName(String callingRef){

        if(callingRef == null || callingRef.length() == 0){
            return "";
        }


        //return CoreUtils.removeArraySign(callingRef.split("->")[0]);
        return callingRef.split("\\)")[1];

    }

    public static boolean hasClassName(String callRef){


        return callRef.contains("->");
    }


    public static String getClassInstanceName(String callingRef){

        if(callingRef == null || callingRef.length() == 0 || !callingRef.contains(":")){
            return "";
        }

        return callingRef.split(":")[1];

    }


    public static String getClassInstanceNameWithoutArray(String callingRef){

        if(callingRef == null || callingRef.length() == 0 || !callingRef.contains(":")){
            return "";
        }

        if(callingRef.contains("[")){
            callingRef = callingRef.replace("[","");
        }

        return callingRef.split(":")[1];

    }


    public static String getFieldName(String callingRef){

        if(callingRef == null || callingRef.length() == 0 || !callingRef.contains(":")){
            return "";
        }

        /*
        if(callingRef.contains("[")){
            callingRef = CoreUtils.removeArraySign(callingRef);
        }*/

        if(callingRef.contains("->")){
            String tmpCallingReg = callingRef.split("->")[1];
            return tmpCallingReg.split(":")[0];
        }else{
            return callingRef.split(":")[0];
        }



    }


    /**
     *
     * @param callingRef
     * @return the the type of a field without the array symbol
     */
    public static  String getFieldType(String callingRef){
        if(callingRef == null || callingRef.length() == 0 || !callingRef.contains(":") || !callingRef.contains(";")){
            return "";
        }
        String getFieldType = "";

        if(callingRef.contains("->")){
            String tmpCallingReg = callingRef.split("->")[1];
            getFieldType = tmpCallingReg.split(":")[1];
        }else{
            getFieldType = callingRef.split(":")[1];
        }

        if(!getFieldType.contains(";")){
            return "";
        }


        /*
        if(getFieldType.contains("[")){
            int arrDim = CoreUtils.countMatches(getFieldType,"[");

            if(getFieldType.indexOf(";") <= 0){
                System.out.println("ahah: "+ callingRef);
            }
            return getFieldType.substring(arrDim,getFieldType.indexOf(";")+1);

        }else{
            return getFieldType;
        }*/

        return getFieldType;
    }


    /**
     *
     * @param callingRef
     * @return  only the StringName of the Method for like method.getName() (everything after the ->)
     *
     */
    public static String getCallingMethodName(String callingRef){

        if(callingRef == null || callingRef.length() == 0){
            return "";
        }

        /*
        if(callingRef.contains("[")){
            callingRef = CoreUtils.removeArraySign(callingRef);
        }*/

        if(!callingRef.contains("->") && callingRef.endsWith(";")){
            // this is for the case, we want to check for a new-instance appearance and want the default constructor of it
            return callingRef+"-><init>()V";
        }

        return callingRef.split("->")[1];

    }


    /**
     * This method analyze the static fields of a Class for certain types which could indicate the usage of literals.
     *
     * @param classDef: the Class definition (org.jf.dexlib2.iface.ClassDef) of the current analyzed Class with a CLINIT.
     * @return true if some suspected static literals are used or false if not
     */
    public static boolean hasStaticLiteral(ClassDef classDef){

        for(Field f : classDef.getStaticFields()){

            if(f.getType().contains("Ljava/lang/String") || f.getType().endsWith("[C") || f.getType().endsWith("[B") || f.getType().endsWith("[I")  || f.getType().endsWith("[S")){
                return true;
            }


        }



        return false;
    }



    /**
     * get an Array of an Package for instance from Ljava/lang/String;
     * it will return ["java","lang","String"]
     *
     *
     * @param callingRef: the smali-Reference to an invocaton to another class (doesn't matter if instance, reading, or writing)
     * @return an array of all package elemtens
     */
    public static String[] getClassPackageArray(String callingRef){
        // regex: (^L[a-zA-Z_0-9]+|/[a-zA-Z_0-9]+)
        if(callingRef.startsWith("L")){

            int posOfPackageDefEnd = callingRef.indexOf(";");
            String callingRefPrepared = callingRef.substring(1,posOfPackageDefEnd);

            return callingRefPrepared.split("/");
        }else{
            return null;
        }

    }


    /**
     * getCallingMethodParameter(callingRef) returns the used method-parameters on an method-invocation
     *
     * @param callingRef
     * @return the parameters of an method as String
     */
    public static String getCallingMethodParameter(String callingRef){

        if(callingRef == null || callingRef.length() == 0 || !callingRef.contains("->")){
            return "";
        }


        String smaliMethodCall = callingRef.split("->")[1];

        Pattern p = Pattern.compile("\\(.*\\)");
        Matcher m = p.matcher(smaliMethodCall);


        String smaliMethodParameter = "";
        if(m.find()){
            smaliMethodParameter = m.group(0).substring(1,m.group(0).length()-1);
        }

        return smaliMethodParameter;

    }









    /*
    *
    *  old methods which are deleted in future releases
    *
    * */




    /**
     * old version of sorting with array
     */
    private void sortClinitListSize(){
        List<SmaliUnit> clinitClassesList =  new ArrayList<SmaliUnit>();

        for(SmaliUnit sm : clinitClassesList){

            MethodImplementation methodImplementation = sm.getCoreMethodDefinition().getImplementation();

            int instCount = 0;
            for (Instruction instruction : methodImplementation.getInstructions()) {
                instCount++;
            }
            sm.setNumberOfInst(instCount);


            //remainingCoresMap.put(sm,sm.getClassDefinition().getType());
        }
    }





    /**
     * Old version to get the biggest clinit if we only have on "core"-Clinit-Class
     * @param sortedSmaliUnit array from type SmaliUnit
     * @return the first element of an sorted Array of SmaliUnits, which is after sorting the biggest one
     */
    private SmaliUnit getBiggestClinit(SmaliUnit[] sortedSmaliUnit){
        return  sortedSmaliUnit[0];    }
}
