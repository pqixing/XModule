package com.pqixing.modularization.tasks

import com.pqixing.modularization.Default
import com.pqixing.modularization.utils.FileUtils
import com.pqixing.modularization.utils.NormalUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by pqixing on 17-12-22.
 */

public class UpdateLog extends DefaultTask {
    List<String> modules
    Map<String, String> envs
    String compileGroup

    UpdateLog(){
        group = Default.taskGroup
    }
    @TaskAction
    void run() {
        modules = findModules(project.file("readme"))
        envs.each { map -> generatorLogFile(map.key, map.value) }

    }
    /**
     * 查找当前存在的模块
     * @return
     */
   static List<String> findModules(File dir) {
        def mds =[]
        if(dir==null||!dir.exists()) return
        dir.listFiles(new FilenameFilter() {
            @Override
            boolean accept(File file, String s) {
                return file.isDirectory()
            }
        }).each {mds+= it.name}
        return mds
    }
    /**
     * 生成md文件
     * @param env
     * @param urls
     */
    void generatorLogFile(String env, String urls) {
        StringBuilder sb = new StringBuilder("##  大辰组件更新日志   \n").append("仓库地址:　$urls   \n")
        modules.each { moduleName ->
            String metaUrl = NormalUtils.getMetaUrl(urls, compileGroup, moduleName)
            String xmlTxt = NormalUtils.request(metaUrl)
            if (NormalUtils.isEmpty(xmlTxt)) return
            sb.append("###   [$moduleName](${metaUrl})    \n")
                    .append("").append("最新版本:　${NormalUtils.parseXmlByKey(xmlTxt, 'release')}")
                    .append("　　　　　").append("最后更新时间:　").append(NormalUtils.parseXmlByKey(xmlTxt, "lastUpdated"))
            File detailFile = new File(project.buildDir, "${env}/${moduleName}.md")

            if (detailFile.exists()) sb.append("　　　　[详细日志](${"../build/${env}/${moduleName}.md"})")
            sb.append("   \n")

            List<String> versions = NormalUtils.parseListXmlByKey(xmlTxt, "version")
            sb.append(">   ")
            def size = versions.size()
            for (int i = size - 1; i >= Math.max(0, size - 30); i--) {
                sb.append("[${versions[i]}](${NormalUtils.getPomUrl(urls, compileGroup, moduleName, versions[i])})").append("　　")
            }
            sb.append("\n\n >  依赖方式:implementation 'com.dachen.android:router:+'")
            sb.append("\n  --- \n")
        }

        FileUtils.write(new File(project.projectDir, "updatelog/${env}.md"), sb.toString())
    }
}
