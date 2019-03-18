package com.dachen.creator.core;

import com.dachen.creator.utils.StringUtils;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouterCodeFactory {
    public static String generatRouterProxy(PsiClass clazz){
        PsiFile file = clazz.getContainingFile();
        String fileText = file.getText();
        List<String> importList = parseImportCode(fileText);
        String fullClassName = clazz.getQualifiedName();
        String pkg = fullClassName.substring(0, fullClassName.lastIndexOf("."));
        String className = fullClassName.substring(fullClassName.lastIndexOf(".")+1, fullClassName.length());
        StringBuilder code = new StringBuilder();
        code.append(StringUtils.formatSingleLine(0, "package " + pkg + ".proxy;") +
                "\n" ) ;
        for(String importStr : importList){
            code.append(StringUtils.formatSingleLine(0, importStr));
        }
        code.append("\n" +StringUtils.formatSingleLine(0, "public final class "+className+" {") +
                "\n");
        PsiField[] fields = clazz.getAllFields();
        for (PsiField field : fields) {
            if (fieldFilter(field)) {
                continue;
            }
            // 用拼接的代码生成innerClass
            String innerCode = RouterCodeFactory.generatInnerClass(field);
            code.append("\n"+innerCode + "\n");
        }
        code.append("}");

        return code.toString();
    }

    private static boolean fieldFilter(PsiField field) {
        return !(field.hasModifierProperty("public") ||
                field.hasModifierProperty("static") ||
                field.hasModifierProperty("final"));
    }

    private static List<String> parseImportCode(String fileText){
        List<String> importList = new ArrayList<>();
        String[] textArr = fileText.split(";");
        for(String text : textArr){
            if(text==null || text.trim().length()<=0){
                continue;
            }
            String importStr = text.replace("\n", "").replace("\"", "");
            if(!importStr.startsWith("import")){
                continue;
            }
            String subFlag = "import ";
            String className = importStr.substring(importStr.indexOf(subFlag)+subFlag.length(), importStr.length()).replace(" ", "");

            if("com.dachen.regester.DcPath".equals(className) ||
                    "com.dachen.regester.DcServices".equals(className) ||
                    "com.dachen.regester.Key".equals(className) ||
                    "com.dachen.regester.Type".equals(className) ||
                    "com.dachen.regester.RouterPath".equals(className)){
                continue;
            }
            importList.add(importStr + ";");
        }
        return importList;
    }

    public static String generatInnerClass(PsiField field) {
        String fieldName = field.getName();
        PsiDocComment doc = null;
        for(PsiElement fe : field.getChildren()){
            if(fe instanceof PsiDocComment){
                doc = (PsiDocComment) fe;
            }
        }
        if(fieldName==null || fieldName.trim().length()<=0){
            return "";
        }
        if(!field.hasInitializer()){
            return "";
        }

        String fieldCode = fieldName.hashCode() + "";
        if(fieldCode.startsWith("-")){
            fieldCode = fieldCode.replace("-", "_");
        }
        String fieldVal = "/" + fieldName.toLowerCase()+(fieldCode+field.getInitializer().getText()).replace("\"", "");
        StringBuilder codeSb = new StringBuilder();
        if(doc != null) {
            codeSb.append(StringUtils.formatSingleLine(2, "\t\t"+doc.getText()));
        }
        codeSb.append(StringUtils.formatSingleLine(2, "public static final class "+fieldName+" {")
                + "\n");
        codeSb.append(StringUtils.formatSingleLine(4, "public static final String THIS = \""+fieldVal+"\";")
                + "\n");
        codeSb.append(StringUtils.formatSingleLine(4, "public static final String THIS2 = THIS+\"2\";")
                + "\n");
        codeSb.append(StringUtils.formatSingleLine(4, "private android.os.Bundle bundle = null;")
                + "\n");

        String fieldText = field.getText()
                .replace("\n", "")
                .replace("\"", "")
                .replace(" ", "");
        if(fieldText.contains("@DcPath")){
            handleDcPath(fieldName, codeSb, parseDcPath(fieldText));
        }else if(fieldText.contains("@DcServices")){
            handleDcService(codeSb, parseDcService(fieldText));
        }
        codeSb.append("\t\t}");
        return codeSb.toString().trim();
    }

    private static void handleDcService(StringBuilder codeSb, String className) {
        if(className==null || className.trim().length()<=0){
            return;
        }
        codeSb.append(StringUtils.formatSingleLine(4,
                "public static "+className+" navigation() {\n" +
                        "\t\t\t\t\t\treturn ("+className+") com.dachen.router.DcRouter.build(THIS).navigation();\n" +
                        "\t\t\t\t}")
                + "\n");
        codeSb.append(StringUtils.formatSingleLine(4,
                "public static Class targetClass() {\n" +
                        "\t\t\t\t\t\treturn "+className+".class;\n" +
                        "\t\t\t\t}")
                + "\n");
    }

    private static void handleDcPath(String fieldName, StringBuilder codeSb, Map<String, String> keyVals) {
        codeSb.append(StringUtils.formatSingleLine(4,
                "private "+fieldName+"(android.os.Bundle bundle) {\n" +
                "\t\t\t\t\t\tthis.bundle = bundle==null? new android.os.Bundle():bundle;\n" +
                "\t\t\t\t}")
                + "\n");
        codeSb.append(StringUtils.formatSingleLine(4,
                "public static "+fieldName+" create() {\n" +
                "\t\t\t\t\t\treturn new "+fieldName+"(null) ;\n" +
                "\t\t\t\t}")
                + "\n");
        codeSb.append(StringUtils.formatSingleLine(4,
                "public static "+fieldName+" with(android.os.Bundle bundle) {\n" +
                "\t\t\t\t\t\treturn new "+fieldName+"(bundle) ;\n" +
                "\t\t\t\t}")
                + "\n");
        codeSb.append(StringUtils.formatSingleLine(4,
                "public static "+fieldName+" with(android.content.Intent intent) {\n" +
                        "\t\t\t\t\t\treturn with (intent==null?null:intent.getExtras()) ;\n" +
                        "\t\t\t\t}")
                + "\n");
        codeSb.append(StringUtils.formatSingleLine(4,
                "public static "+fieldName+" with(android.app.Activity activity) {\n" +
                        "\t\t\t\t\t\treturn with (activity==null?null:activity.getIntent()) ;\n" +
                        "\t\t\t\t}")
                + "\n");

        codeSb.append(StringUtils.formatSingleLine(4,
                "public final com.alibaba.android.arouter.facade.Postcard build() {\n" +
                "\t\t\t\t\t\treturn com.dachen.router.DcRouter.build(THIS).with(bundle);\n" +
                "\t\t\t\t}")
                + "\n");
        codeSb.append(StringUtils.formatSingleLine(4,
                "public final java.lang.Object start(android.content.Context context) {\n" +
                "\t\t\t\t\t\treturn com.dachen.router.DcRouter.build(THIS).with(bundle).navigation(context);\n" +
                "\t\t\t\t}")
                + "\n");
        codeSb.append(StringUtils.formatSingleLine(4,
                "public final void startForResult(android.app.Activity activity, int requestCode) {\n" +
                "\t\t\t\t\t\tcom.dachen.router.DcRouter.build(THIS).with(bundle).navigation(activity,requestCode);\n" +
                "\t\t\t\t}")
                + "\n");

        if(keyVals == null || keyVals.isEmpty()){
            return;
        }
        for(Map.Entry<String, String> me : keyVals.entrySet()){
            String key = me.getKey();
            String uKey = key.substring(0, 1).toUpperCase() + key.substring(1);
            String typeStr = me.getValue();
            String[] typeArr = typeStr.split("#");
            String type = typeArr[0];
            String put = typeArr[1];
            String get = typeArr[2];
            codeSb.append(StringUtils.formatSingleLine(4,
                    "public static final String "+key.toUpperCase()+" = \""+key+"\";")
                    + "\n");
            codeSb.append(StringUtils.formatSingleLine(4,
                    "public final "+fieldName+" set"+uKey+"("+type+" value) {\n" +
                            "\t\t\t\t\t\t"+put+"("+key.toUpperCase()+",value) ;\n" +
                            "\t\t\t\t\t\treturn this ;\n" +
                            "\t\t\t\t\t\t}"));

            codeSb.append(StringUtils.formatSingleLine(4,
                    "public final "+type+" get"+uKey+"() {\n" +
                            "\t\t\t\t\t\treturn  "+get+"("+key.toUpperCase()+") ;\n" +
                            "\t\t\t\t}"));
        }
    }

    private static String parseDcService(String fieldText){
        if(!fieldText.contains("@DcServices")){
            return null;
        }
        String startFlag = "@DcServices(";
        String endFlag = ")";
        String content = fieldText.substring(fieldText.indexOf(startFlag)+startFlag.length(), fieldText.indexOf(endFlag));
        if(content.trim().length() <= 0){
            return null;
        }
        String[] services = content.split("=");
        if(services.length > 1){
            String clazz = services[1];
            return clazz.substring(0, clazz.lastIndexOf("."));
        }
        return null;
    }

    private static Map<String, String> parseDcPath(String fieldText){
        if(!fieldText.contains("@Key")){
            return null;
        }
        String startFlag = "@DcPath(params={@Key(";
        String endFlag = ")})publicstaticfinal";
        String content = fieldText.substring(fieldText.indexOf(startFlag)+startFlag.length(), fieldText.indexOf(endFlag));
        if(content.trim().length() <= 0){
            return null;
        }
        content = content.replace("@Key","");
        content = content.replace("(","");
        content = content.replace(")","");
        String[] keys = content.split(",");
        Map<String, String> keyValMap = new HashMap<>();
        List<String> keyList = new ArrayList<>();
        List<String> typeList = new ArrayList<>();
        for(String key : keys){
            String[] keyVals = key.split("=");
            if(keyVals.length <= 0){
                continue;
            }
            String keyName = keyVals[0];
            String keyVal = keyVals[1];
            if("key".equals(keyName)){
                keyList.add(keyVal);
            }
            if("type".equals(keyName)){
                typeList.add(returnType(keyVal));
            }
        }
        for(int i=0; i<keyList.size(); i++){
            keyValMap.put(keyList.get(i), typeList.get(i));
        }
        return keyValMap;
    }

    private static String returnType(String type){
        String rType = null;
        if("Type.STRING".equalsIgnoreCase(type)){
            rType = "String#bundle.putString#bundle.getString";
        }else if("Type.INT".equalsIgnoreCase(type)){
            rType = "int#bundle.putInt#bundle.getInt";
        }else if("Type.PARCELABLE".equalsIgnoreCase(type)){
            rType = "android.os.Parcelable#bundle.putParcelable#bundle.getParcelable";
        }else if("Type.BOOLEAN".equalsIgnoreCase(type)){
            rType = "boolean#bundle.putBoolean#bundle.getBoolean";
        }else if("Type.FLOAT".equalsIgnoreCase(type)){
            rType = "float#bundle.putFloat#bundle.getFloat";
        }else if("Type.LONG".equalsIgnoreCase(type)){
            rType = "long#bundle.putLong#bundle.getLong";
        }else if("Type.SHORT".equalsIgnoreCase(type)){
            rType = "short#bundle.putShort#bundle.getShort";
        }else if("Type.SERIALIZABLE".equalsIgnoreCase(type)){
            rType = "java.io.Serializable#bundle.putSerializable#bundle.getSerializable";
        }else if("Type.BUNDLE".equalsIgnoreCase(type)){
            rType = "android.os.Bundle#bundle.putBundle#bundle.getBundle";
        }else if("Type.DOUBLE".equalsIgnoreCase(type)){
            rType = "double#bundle.putDouble#bundle.getDouble";
        }
        return rType;
    }
}
