package com.pqixing.modularization.utils

import org.gradle.api.Project

/**
 * 拓展类Utils
 */
class ExtensionUtils {
 static <T> T getExtends(Project project,Class<T> tClass){
     try {
         return project."$tClass.name"
     }catch (Exception e){}
     return null
 }
}