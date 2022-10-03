package dexslib.core.slicing;

import org.jf.dexlib2.iface.MethodParameter;

import java.util.ArrayList;
import java.util.List;

public class MethodParameters {

    private ArrayList<String> parameterValues = new ArrayList<String>();
    private int numOfParameters =0;
    private List<? extends MethodParameter> methodParameterList;

    public MethodParameters(List<? extends MethodParameter> methodParameter){
            this.numOfParameters = methodParameter.size();

            this.methodParameterList = methodParameter;
    }


    public MethodParameters(List<? extends MethodParameter> methodParameter, String firstValue){
        this.numOfParameters = methodParameter.size();
        parameterValues.add(firstValue);
        this.methodParameterList = methodParameter;
    }

    public MethodParameters(List<? extends MethodParameter> methodParameter, ArrayList<String> parameterValues){
        this.numOfParameters = methodParameter.size();
        this.parameterValues = parameterValues;
        this.methodParameterList = methodParameter;
    }

    public String getFirstParamterType(){
        String firstParameterType = "";

        return firstParameterType;
    }

    public MethodParameter getFirstMethodParamter(){
        if(this.methodParameterList.isEmpty()){
            return null;
        }
        return this.methodParameterList.get(0);
    }


    public String getFirstMethodParamterValue(){
        /*if(this.methodParameterList.isEmpty()){
            return null;
        }*/
        return this.parameterValues.get(0);
    }


    public String getNthParameterType(int parameterNumber){
        String nthParameterType = "";

        return nthParameterType;

    }

    public int getNthParameterReferenceType(int parameterNumber){

        return 0;

    }

    /**
     *
     * @param parameterNumber starts by zero
     * @return the first parameter is 0 and the second 1
     */
    public MethodParameter getNthMethodParameter(int parameterNumber){
        if(parameterNumber >= this.methodParameterList.size()){
            return null;
        }
        return this.methodParameterList.get(parameterNumber);

    }

    /**
     *
     * @param parameterNumber starts by zero
     * @return the first parameter is 0 and the second 1
     */
    public String getNthMethodParameterValue(int parameterNumber){
        if(parameterNumber >= this.methodParameterList.size()){
            return null;
        }
        return this.parameterValues.get(parameterNumber);

    }


    public void setNthMethodParameterValue(int parameterNumber,String value){
        this.parameterValues.add(parameterNumber,value);
        //this.parameterValues.add(value);
    }


    public ArrayList<String> getParameterValuesList(){
        return this.parameterValues;
    }


    public int getNumOfParameters(){
        return this.numOfParameters;
    }
}
