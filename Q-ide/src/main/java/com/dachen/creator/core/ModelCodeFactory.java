package com.dachen.creator.core;

import com.dachen.creator.utils.StringUtils;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.javadoc.PsiDocComment;

public class ModelCodeFactory {
    public static String generatModelCodel(PsiMethod method){
        String[] codeArr = parseMethodStr(method);
        if(codeArr==null || codeArr.length<=0){
            return null;
        }
        String[] annotationCode = parseAnnotation(codeArr[0]);
        String url = annotationCode[0];
        String reqMethod = annotationCode[1];
        String isAnsy = annotationCode[2]==null ? "true": annotationCode[2];
        if(url==null || url.trim().length()<=0){
            Messages.showWarningDialog("url must not be null", "CreateModelTemplateCode");
            return null;
        }
        StringBuilder code = new StringBuilder();

        code.append(StringUtils.formatSingleLine(0, "@Override"));
        code.append(StringUtils.formatSingleLine(0, "public " + codeArr[1])+" {" +
        "\n");
        code.append(StringUtils.formatSingleLine(1, "RequestBean.Builder req = RequestBean.builder()"));
        PsiParameterList parameterList =  method.getParameterList();
        PsiParameter[] parameter = parameterList.getParameters();
        String callBackParam = null;
        for(PsiParameter param : parameter){
            if(param == null){
                continue;
            }
            String type = param.getType().getCanonicalText();
            String pt = param.getName();
            if(type.startsWith("com.dachen.net.response.ResponseCallBack") ||
                    type.startsWith("com.dachen.net.bean.RequestBean.TypeAdapter")){
                callBackParam = pt;
                continue;
            }

            if(type.startsWith("com.dachen.net.bean.RequestParams.Builder")){
                code.append(StringUtils.formatSingleLine(2, ".putParams("+pt+")"));
                continue;
            }

//            if(type.startsWith("com.example.suzhan.plugintest.net.ResponseCallBack") ||
//                    type.startsWith("com.example.suzhan.plugintest.net.RequestBean.TypeAdapter")){
//                callBackParam = pt;
//                continue;
//            }
//            if(type.startsWith("com.example.suzhan.plugintest.net.RequestParams.Builder")){
//                code.append(StringUtils.formatSingleLine(2, ".putParams("+pt+")"));
//                continue;
//            }

            if (type.startsWith("iterface.io.File") || type.endsWith("<iterface.io.File>") ) {
                code.append(StringUtils.formatSingleLine(2, ".putParam(" + pt + ")"));
                continue;
            }
            if(reqMethod!=null) {
                if ("iterface.lang.String[]".equals(type) || reqMethod.contains("STRING")) {
                    code.append(StringUtils.formatSingleLine(2, ".putParam(" + pt + ")"));
                    continue;
                }
            }
            code.append(StringUtils.formatSingleLine(2, ".putParam(\""+pt+"\", "+pt+")"));
        }
        if(reqMethod!=null && reqMethod.trim().length()>0){
            code.append(StringUtils.formatSingleLine(2, ".setMethod("+reqMethod+")"));
        }
        code.append(StringUtils.formatSingleLine(2, ".setUrl("+url+");") + "\n");
        if("true".equals(isAnsy)) {
            code.append(StringUtils.formatSingleLine(1, "reqLife.doAsynRequest(req, " + callBackParam + ");"));
        }else{
            code.append(StringUtils.formatSingleLine(1, "return reqLife.doSynRequest(req, " + callBackParam + ");"));
        }
        code.append("}");
       return code.toString();
    }

    public static String[] parseAnnotation(String annotation){
        String anStr = annotation.substring(annotation.indexOf("(")+1, annotation.indexOf(")"));
        String[] anArr = anStr.split(",");
        String[] anCode = new String[3];
        for(String an : anArr){
            an = an.replace("\n", "").trim().replace(" ", "");
            if(an.startsWith("url")){
                String[] urlArr = an.split("=");
                anCode[0] = urlArr[1];
            }
            if(an.startsWith("method")){
                String[] methodArr = an.split("=");
                anCode[1] = methodArr[1];
            }
            if(an.startsWith("isAsyn")){
                String[] isAsynArr = an.split("=");
                anCode[2] = isAsynArr[1];
            }
        }
        return anCode;
    }

    /**
     * 返回值 数组 index0 是注解信息
     * index 1是方法信息
     * @param method
     * @return
     */
    public static String[] parseMethodStr(PsiMethod method){
        String[] code = new String[2];
        PsiDocComment doc = method.getDocComment();
        String methodText = method.getText();
        if(doc != null){
            String docText = doc.getText().replace("\"", "");
            methodText = methodText.substring(methodText.indexOf(docText)+docText.length(), methodText.indexOf(";"));
        }else {
            methodText = methodText.replace(";", "");
        }
        methodText = methodText.trim();
        if(!methodText.contains("@DcRequest")){
            return null;
        }
        String annotiaon = methodText.substring(methodText.indexOf("@"), methodText.indexOf(")")+1);
        if(annotiaon.trim().length()<=0){
            return null;
        }
        String methodStr = methodText.substring(methodText.indexOf(annotiaon)+annotiaon.length(), methodText.length())
                .replaceFirst("\n", "").trim();
        code[0] = annotiaon;
        code[1] = methodStr;
        return code;
    }

}
