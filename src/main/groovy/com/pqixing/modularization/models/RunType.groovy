package com.pqixing.modularization.models

import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.NormalUtils
import com.pqixing.modularization.utils.Print
import org.gradle.api.Project

/**
 * Created by pqixing on 17-12-7.
 */

class RunType extends BaseContainerExtension {
    boolean asApp = true
    String launchActivity
    String app_icon
    String app_name
    String app_theme
    String packageName
    boolean afterLogin = true


    RunType(String name) {
        super(name)
    }

    @Override
    void onCreate(Project project) {
        super.onCreate(project)
        app_name = project.name
    }

    @Override
    public String toString() {
        return "RunType{" +
                "asApp=" + asApp +
                ", launchActivity='" + launchActivity + '\'' +
                ", app_icon='" + app_icon + '\'' +
                ", app_name='" + app_name + '\'' +
                ", app_theme='" + app_theme + '\'' +
                '}';
    }
    void updateType(){
        RunType defType = project.moduleConfig.runTypes."DEFAULT"
        if (NormalUtils.isEmpty(app_icon)) app_icon = defType.app_icon
        if (NormalUtils.isEmpty(app_name)) app_name = defType.app_name
        if (NormalUtils.isEmpty(app_theme)) app_theme = defType.app_theme
        if (NormalUtils.isEmpty(packageName)) app_theme = defType.packageName
        if(afterLogin) afterLogin = defType.afterLogin
        if(asApp) asApp = defType.asApp
    }
    @Override
    LinkedList<String> generatorFiles() {
        updateType()
        if ("application" == project.moduleConfig.pluginType || !asApp) return []//不独立运行，不生产缓存类文件

        BuildConfig buildConfig = project.buildConfig
        AndroidConfig androidConfig = project.moduleConfig.androidConfig
        def maps = properties
        maps.putAll(buildConfig.properties)
        if(!NormalUtils.isEmpty(packageName)) maps.put("res_packageName",packageName)
        else maps.put("res_packageName",buildConfig.packageName)
        maps.put("versionCode", NormalUtils.isEmpty(androidConfig.versionCode) ? "1" : androidConfig.versionCode)
        maps.put("versionName", NormalUtils.isEmpty(androidConfig.versionName) ? "1.0" : androidConfig.versionName)

        if(afterLogin) maps+= ["hideRouterCode":"N"]
        //输出Application类
        def applicationFile = new File(FileUtils.appendUrls(buildConfig.cacheDir, "java"
                , "auto",buildConfig.packageName.replace('.', File.separator), "DefaultAppCation.java"))
        FileUtils.write(applicationFile, NormalUtils.parseString(applicationTxt, maps))

        //输出Activity类
        def activityFile = new File(FileUtils.appendUrls(buildConfig.cacheDir, "java"
                , "auto",buildConfig.packageName.replace('.', File.separator), "DefaultActivity.java"))
        FileUtils.write(activityFile, NormalUtils.parseString(activityTxt, maps))

        //输出Activity类
        def callBackFile = new File(FileUtils.appendUrls(buildConfig.cacheDir, "java"
                , "auto",buildConfig.packageName.replace('.', File.separator), "LoginCallBack.java"))
        FileUtils.write(callBackFile, NormalUtils.parseString(loginCallBackTxt, maps))

        //输出临时清单文件
        def inputManifest = new File(FileUtils.appendUrls(project.projectDir.path, "src", "main"), "AndroidManifest.xml").text
        inputManifest = inputManifest.replaceFirst("<manifest(?s).*?>", NormalUtils.parseString(manifestMetaTxt, maps))
                .replaceFirst("<application(?s).*?>", NormalUtils.parseString(manifestAppTxt, maps))
        if (NormalUtils.isEmpty(inputManifest.find("<application(?s).*?>"))) inputManifest = inputManifest.replace("</manifest>" ,"") + NormalUtils.parseString(manifestAppTxt, maps) + "\n     </application> \n</manifest>"
        Print.ln("hasmatches: ${inputManifest.matches("<application(?s).*?>")} inputManifest : $inputManifest")
        FileUtils.write(new File(buildConfig.cacheDir, "AndroidManifest.xml"), inputManifest)

        //输出source的gradle配置
        return [FileUtils.write(new File(buildConfig.cacheDir, "sourceSets.gradle"), NormalUtils.parseString(sourceSetTxt, ["cacheDir": buildConfig.cacheDir]))]
    }

    String getLoginCallBackTxt(){
        return '''
package auto.#{packageName};

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.app.Application;

/**
 * Created by pqixing on 18-1-11.
 */
@com.alibaba.android.arouter.facade.annotation.Route(path = com.dachen.router.dcrouter.proxy.RoutePaths.LoginRouterModel.THIS) //#1{hideRouterCode}
public  class LoginCallBack
        implements com.dachen.router.dcrouter.services.RouterMainLoginService //#1{hideRouterCode}
{
    public void routerLoginSuccess(String jsonStr, String phone, String password) {
        DefaultActivity.activity.startActivity(new Intent(DefaultActivity.activity,DefaultActivity.class));
    }

    public void routerLoginFail() {
        Toast.makeText( DefaultActivity.activity,"登录失败",Toast.LENGTH_LONG).show();
    }

    public void logout() {

    }

    public void init(Context context) {

    }
}

'''
    }

    String getManifestMetaTxt() {
        return '''<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="#{res_packageName}"
    android:versionCode="#{versionCode}"
    android:versionName="#{versionName}"
    >
'''
    }

    String getManifestAppTxt() {
        return '''
<application
        android:allowBackup="true"
        tools:replace="android:label"
        android:name="auto.#{packageName}.DefaultAppCation"
        android:icon="#1{app_icon}"
        android:label="#{app_name}"
        android:theme="#1{app_theme}"
        >
        <activity android:name="auto.#{packageName}.DefaultActivity"
           android:launchMode="singleTask"
           >
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
'''
    }

    String getSourceSetTxt() {
        return '''
android{
 sourceSets {
        //在main目录中
        main {
            java.srcDirs += "#{cacheDir}/java"
            manifest.srcFile '#{cacheDir}/AndroidManifest.xml'
        }
    }
}
'''
    }

    String getApplicationTxt() {
        return '''
package auto.#{packageName};
import android.app.Application;
import com.dachen.router.dcrouter.services.IApplicationLike;
/**
 * Created by pqixing on 17-11-24.
 */

public class DefaultAppCation extends com.dachen.router.DcApplication {

    @Override
     protected void afterAppLikeInit(){
      com.dachen.router.DcRouter.openDebug();
        for (IApplicationLike like : getModules().values()) {
            like.onVirtualCreate(this);
        }
    }
    
    @Override
    public void onVirtualCreate(Application application) {
        
    }

    @Override
    public void onCreateOnUI(Application application) {

    }

    @Override
    public void onCreateOnThread(Application application) {

    }

    @Override
    public boolean onUrlDispath(String s) {
        return false;
    }
       @Override
    public boolean onEventDispath(Object o) {
        return false;
    }

      @Override
    public String getModuleName() {
        return "Virtual";
    }
}'''
    }

    String getActivityTxt() {
        return '''
package auto.#{packageName};

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DefaultActivity extends Activity {
public static Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        initView();
        toActivity(#{afterLogin});
    }
    
    public static final void toActivity(boolean afterLogin){
        if(afterLogin){
            try {  com.dachen.router.mdclogin.proxy.MdcLoginPaths.LoginActivity.create().start(activity); }catch (Exception e){  } //#1{hideRouterCode}
        }
    }
    
      @Override
    protected void onDestroy() {
        activity = null;
        super.onDestroy();
    }
    
    void initView(){
    
     FrameLayout contentView = new FrameLayout(this);
        ListView listView = new ListView(this);
        contentView.addView(listView,new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(contentView);

        final List<String> names = new ArrayList<>();
        final List<Class> values = new ArrayList<>();
        try {
            for (Field f : Class.forName("auto.#{packageName}.#{projectName}Launch").getDeclaredFields()){
                  try {
                    values.add(Class.forName(String.valueOf(f.get(null))));
                    names.add(f.getName());
                }catch (Exception e){}
            }
        } catch (Exception e) {
        }
        listView.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,names));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getApplicationContext(),values.get(position)));
            }
        });
    
    }
    
}
'''
    }
}
