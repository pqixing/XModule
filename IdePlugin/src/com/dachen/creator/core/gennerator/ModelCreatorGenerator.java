package com.dachen.creator.core.gennerator;

import com.dachen.creator.core.ModelCodeFactory;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;

public class ModelCreatorGenerator {

    public static void genCode(Project project, PsiClass clazz) {
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        PsiClass[] interfaces = clazz.getInterfaces();
        PsiMethod[] clazzMethods = clazz.getMethods();

        for(PsiClass iface : interfaces){
            PsiMethod[] methods = iface.getMethods();
            for(PsiMethod method : methods){
                if(methodExits(clazzMethods, method)){
                    continue;
                }
                String innerCode = ModelCodeFactory.generatModelCodel(method);
                if(innerCode == null){
                    continue;
                }
                PsiMethod innerMethod = factory.createMethodFromText( innerCode, clazz );
                clazz.add(innerMethod);
            }
        }
    }

    private static boolean methodExits(PsiMethod[] clazzMethods, PsiMethod method) {
        boolean exits = false;
        String[] methodStr = ModelCodeFactory.parseMethodStr(method);
        if(methodStr != null && methodStr.length>1){
            String methodText = methodStr[1].replace("\n", "").replace(" ", "");
            if(methodText == null){
                return false;
            }
            for(PsiMethod cm : clazzMethods){
                String cmText = cm.getText().replace("\n", "").replace(" ", "");
                if(cmText.contains(methodText)){
                    exits = true;
                }
            }
        }
        return exits;
    }

}


