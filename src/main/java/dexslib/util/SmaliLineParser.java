package dexslib.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmaliLineParser {



    private StringBuffer smaliLineSB = new StringBuffer();
    private String smaliLineArr[];
    private String globalSmaliLine = "";

    public SmaliLineParser(String smaliLine){
        this.smaliLineSB = this.getRegisterFromSmaliLine(smaliLine);
        this.smaliLineArr = getRegisterFromSmaliLineAsArray();
        this.globalSmaliLine = smaliLine;

    }

    /**
     *
     * When ever we want to parse a line of smali code we have to ensure that it is initialized
     *
     * @param smaliLine this is the line of smali code which has to be parsed
     *
     *
     *
    public static void initSmaliLineParser(String smaliLine){
        SmaliLineParser.smaliLineSB = SmaliLineParser.getRegisterFromSmaliLine(smaliLine);
        SmaliLineParser.smaliLineArr = getRegisterFromSmaliLineAsArray();
        SmaliLineParser.globalSmaliLine = smaliLine;

    }*/


    // "const-string v7, \"guvf vf zl frperpg zrffntr...abobql pna ernq guvf unununun\"";
    public String getOpcodeFromSmaliLine(String smaliLine){
        return smaliLine.trim().split(" ")[0];
    }



    private StringBuffer getRegisterFromSmaliLine(String smaliLine){
        String parsedString =  smaliLine.trim().split(" ",2)[1];
        StringBuffer sb = new StringBuffer();

        final String regex = "(v\\d|p\\d)+";

        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(parsedString);


        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                sb.append(matcher.group(i)+" ");
            }
        }


        return sb;

    }


    private String[] getRegisterFromSmaliLineAsArray(){
        return this.smaliLineSB.toString().split(" ");
    }


    public int numOfRegsFromSmaliLine(){
        return this.smaliLineArr.length;
    }


    public boolean hasRegsFromSmaliLine(){
        if(this.smaliLineSB.length() < 1){
            return false;
        }
        return true;

    }


    /**
     *
     * @param regNumber the register we want to return from this line
     * @return
     */
    public String getRegNumFromSmaliLine(int regNumber){

        if(hasRegsFromSmaliLine() && regNumber > 0 && regNumber <= this.smaliLineArr.length){
            return  this.smaliLineArr[regNumber-1];
        }



        return "-1";
    }

    public int getRegNumIntFromSmaliLine(int regNumber){

        if(hasRegsFromSmaliLine() && regNumber > 0 && regNumber <= this.smaliLineArr.length){
            String intString = this.smaliLineArr[regNumber-1].substring(this.smaliLineArr[regNumber-1].length() - 1);
            return Integer.parseInt(intString);
        }



        return -1;
    }


    public String getRegValueFromSmaliLine(){
        String tmpSmaliLine = this.globalSmaliLine;

        if(this.hasRegsFromSmaliLine() == false){
            return "";
        }

        tmpSmaliLine = tmpSmaliLine.trim();

        if(tmpSmaliLine.lastIndexOf(", \"") > 0){ // this means we have a string-expression
            tmpSmaliLine = tmpSmaliLine.substring(tmpSmaliLine.lastIndexOf(", \"")+2).trim();
            //return tmpSmaliLine.substring(1,tmpSmaliLine.length()-1);
            return tmpSmaliLine;
        }else if(tmpSmaliLine.lastIndexOf("}, ") > 0){
            return tmpSmaliLine.substring(tmpSmaliLine.lastIndexOf("}, ")+2).trim();
        }else if(tmpSmaliLine.lastIndexOf(", 0x") > 0){
            return tmpSmaliLine.substring(tmpSmaliLine.lastIndexOf(", 0x")+2).trim();
        }


        return tmpSmaliLine;
    }
}
