package com.pqixing.modularization.models
/**
 * Created by pqixing on 17-12-7.
 */

class AndroidConfig extends BaseExtension {

   String buildToolsVersion = '26.0.2'
   String compileSdkVersion = '26'
   String minSdkVersion = '16'
   String targetSdkVersion = '21'
   String versionCode = '1'
   String versionName = "1.0"
   String applicationId = ""
   String support_v4 = "27.0.1"
   String support_v7 = "27.0.1"

   boolean kotlinEnable = false
   String kotlin_version = "1.2.0"

   boolean flavorsEnable = false

}
