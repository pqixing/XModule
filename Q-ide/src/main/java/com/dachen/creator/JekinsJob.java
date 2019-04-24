package com.dachen.creator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JekinsJob {
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";
    public static final String ABORTED = "ABORTED";

    public boolean building;
    public long timestamp;
    public String url;
    public int number;
    public String id;
    public String result;
    public String displayName;
    public List<JAction> actions;

    public Map<String, String> getParams() {
        HashMap<String, String> params = new HashMap<>();
        if (actions != null) for (JAction a : actions) {
            if (a.parameters != null) for (JParam p : a.parameters) {
                params.put(p.name, p.value);
            }
        }
        return params;
    }

    public static class JAction {
        public String _class;
        public List<JParam> parameters;
    }

    public static class JParam {
        public String _class;
        public String name;
        public String value;
    }
}
