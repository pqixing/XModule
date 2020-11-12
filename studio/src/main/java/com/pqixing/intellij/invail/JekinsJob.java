//package com.pqixing.intellij.invail;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class JekinsJob {
//    public static final String SUCCESS = "SUCCESS";
//    public static final String FAILURE = "FAILURE";
//    public static final String ABORTED = "ABORTED";
//
//    public boolean building;
//    public long timestamp;
//    public String url;
//    public int number;
//    public long duration;
//    public String id;
//    public String result;
//    public String displayName;
//    public List<JAction> actions;
//    private   Map<String, String> params;
//
//    public String getAppName(){
//        return getParams().get("Apk");
//    }
//    public String getBranch(){
//        return getParams().get("BranchName");
//    }
//    public String getType(){
//        return getParams().get("Type");
//    }
//    public String getBuildUser(){
//        return getParams().get("BuildUser");
//    }
//    public String getShowName(){
//        return getParams().get("ShowName");
//    }
//
//    public void setParams(Map<String, String> params) {
//        this.params = params;
//    }
//
//    public Map<String, String> getParams() {
//        if(params==null) {
//            params = new HashMap<>();
//            if (actions != null) for (JAction a : actions) {
//                if (a.parameters != null) for (JParam p : a.parameters) {
//                    params.put(p.name, p.value);
//                }
//            }
//        }
//        return params;
//    }
//
//    public static class JAction {
//        public String _class;
//        public List<JParam> parameters;
//    }
//
//    public static class JParam {
//        public String _class;
//        public String name;
//        public String value;
//    }
//}
