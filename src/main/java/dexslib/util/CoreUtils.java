package dexslib.util;

import dexslib.ClassInitializerFilter;
import org.jf.dexlib2.iface.Field;

import java.util.List;

/**
 * Created by Daniel Baier on 03.01.17.
 */
public class CoreUtils {

    /**
     * isImplemented(callingRef) tries to check if a method reference is from a lib (Java-Framework, Android-Framework, etc.).
     * It returns true if the method reference is implemented as method in this dex
     * callingRef is the name of the var which holds the method-Reference
     *
     * @param callingRef
     * @return true if the callingRef (smali reference to another class) is from the package of the DEX and false if not
     */
    public static boolean isImplemented(String callingRef){
        if(callingRef == null || callingRef.length() == 0 || SmaliLayerUtils.getCoreClassPackage().length() < 1){
            return false;
        }

        String className = CoreUtils.removeArraySign(SmaliLayerUtils.getCallingClassName(callingRef));


        return ClassInitializerFilter.getNamesListOfFilteredClinitClasses().contains(className);
    }


    public static boolean isFieldTypeImplemented(String callingRef){
        if(callingRef == null || callingRef.length() == 0 || !callingRef.contains(";")){
            return false;
        }

        String classNameOfFieldType = CoreUtils.removeArraySign(SmaliLayerUtils.getFieldType(callingRef));

        if(classNameOfFieldType.isEmpty()){
            return false;
        }

        return ClassInitializerFilter.getNamesListOfFilteredClinitClasses().contains(classNameOfFieldType);
    }


    public static String removeArraySign(String callingRef){
        return callingRef.replace("[","");
    }

    public static String removeArraySignOld(String callingRef){

        if(callingRef.contains("[")) {

            if(callingRef.startsWith("[")){
                int arrDim = CoreUtils.countMatches(callingRef, "[");
                return callingRef.substring(arrDim,callingRef.length());
            }


            String className = "";
            String methodOrfieldName = "";
            String name = "";
            if(callingRef.contains("->")){
                className = callingRef.split("->")[0];
                className = className+"->";
                methodOrfieldName = callingRef.split("->")[1];
                name = methodOrfieldName.split(":")[0];
                name = name +":";
            }

            if(callingRef.contains(":")){
                if(methodOrfieldName.isEmpty()){
                    name = callingRef.split(":")[0];
                    name = name +":";}
                    methodOrfieldName = callingRef.split(":")[1];
            }

            int arrDim = CoreUtils.countMatches(methodOrfieldName, "[");
            return className+name+methodOrfieldName.substring(arrDim,methodOrfieldName.length());
        }else{
            return callingRef;
        }
    }



    public static boolean isImplementedV1(String callingRef){

        if(callingRef == null || callingRef.length() == 0 || SmaliLayerUtils.getCoreClassPackage().length() < 1){
            return false;
        }

        String arrayOfCallingRef[] = SmaliLayerUtils.getClassPackageArray(callingRef);

        String packagePrefix = "";
        if(arrayOfCallingRef != null && arrayOfCallingRef.length > 1){
            packagePrefix = "L"+arrayOfCallingRef[0]+"/"+arrayOfCallingRef[1];

        }else if(arrayOfCallingRef != null && arrayOfCallingRef.length > 0){
            packagePrefix = "L"+arrayOfCallingRef[0];
        }else{
            return false;
        }
    /*
        System.out.println("--- "+ SmaliLayerUtils.getCoreClassPackage() +" ---");
        System.out.println("--- length: ---");
        System.out.println("--- "+ SmaliLayerUtils.getCoreClassPackage().length() +" ---");
        System.out.println("--- "+ packagePrefix +" ---");
        System.out.println("--- length2: ---");
        System.out.println("--- "+ packagePrefix.length() +" ---");*/

        if(packagePrefix.length() > SmaliLayerUtils.getCoreClassPackage().length()){
            if(packagePrefix.substring(0,SmaliLayerUtils.getCoreClassPackage().length()).equals(SmaliLayerUtils.getCoreClassPackage())){

                return true;
            }else{
                return false;
            }
        }else{
            if(SmaliLayerUtils.getCoreClassPackage().substring(0,packagePrefix.length()).equals(packagePrefix)){

                return true;
            }else{

                return false;
            }
        }




    }

    public static boolean isImplementedV2(String callingRef, String coreClassInstanceName){
        if(callingRef == null || callingRef.length() == 0 || coreClassInstanceName.length() < 1){
            return false;
        }

        String arrayOfCallingRef[] = SmaliLayerUtils.getClassPackageArray(callingRef);

        String packagePrefix = "";
        if(arrayOfCallingRef != null && arrayOfCallingRef.length > 0){
            packagePrefix = "L"+arrayOfCallingRef[0];

        }else{
            return false;
        }
    /*
        System.out.println("--- "+ SmaliLayerUtils.getCoreClassPackage() +" ---");
        System.out.println("--- length: ---");
        System.out.println("--- "+ SmaliLayerUtils.getCoreClassPackage().length() +" ---");
        System.out.println("--- "+ packagePrefix +" ---");
        System.out.println("--- length2: ---");
        System.out.println("--- "+ packagePrefix.length() +" ---");*/

        if(packagePrefix.length() > coreClassInstanceName.length()){
            if(packagePrefix.substring(0,coreClassInstanceName.length()).equals(coreClassInstanceName)){
                return true;
            }else{
                return false;
            }
        }else {
            if (coreClassInstanceName.substring(0, packagePrefix.length()).equals(packagePrefix)) {
                return true;
            } else {
                return false;
            }
        }

    }

    /**
     *
     * @param string2Analyze the String where we want to count the occurrences of an character
     * @param coutingCharacter a character passed as a String which we want to count
     * @return num of occurrences
     */
    public static int countMatches(String string2Analyze,String coutingCharacter){
        return  string2Analyze.length() - string2Analyze.replace(coutingCharacter, "").length();
    }



    public static String addArrayDimension(String stringWithoutArraySign,int arrayDimension){
        String arraysSigns = new String(new char[arrayDimension]).replace("\0", "[");
        String stringWithArraySign = arraysSigns + stringWithoutArraySign;
        return stringWithArraySign;
    }


    public static String getArrayDimension(int arrayDimension){
        String arraysSigns = new String(new char[arrayDimension]).replace("\0", "[");
        return arraysSigns;
    }


    public static boolean isNativeCall(String instruction2Check, List<String> nativeMethodNamesOfCurrentCoreInstance){
        if(instruction2Check.startsWith("Ljava/lang/System;->loadLibrary") || nativeMethodNamesOfCurrentCoreInstance.contains(SmaliLayerUtils.getCallingMethodName(instruction2Check))){
            return true;
        }
        return false;
    }


    public static boolean isBasicType(String classDefRef){
        if(classDefRef.startsWith("L") || classDefRef.contains("L")){
            return false;
        }



        return true;
    }



    public static String constructFieldRef(Field field2Construct){
        return field2Construct.toString();
    }
}
