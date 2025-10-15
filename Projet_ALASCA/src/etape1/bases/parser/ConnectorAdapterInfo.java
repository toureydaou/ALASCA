package etape1.bases.parser;

import java.util.*;

public class ConnectorAdapterInfo {
    public String uid;
    public String offered;
    public List<String> required = new ArrayList<>();

    public static class InstanceVar {
        public String modifiers; 
        public String type;      
        public String name;     
        public String staticInit; 
        @Override public String toString() {
            return modifiers + " " + type + " " + name + (staticInit!=null && !staticInit.isEmpty() ? " = " + staticInit : "");
        }
    }

    public static class MethodInfo {
        public String name;
        public String modifiers; 
        public String returnType; 
        public List<Param> parameters = new ArrayList<>();
        public List<String> thrown = new ArrayList<>();
        public String body;
        public String equipmentRef;
        @Override public String toString() {
            return modifiers + " " + returnType + " " + name + "(" + parameters + ") { " + body + " }";
        }
    }

    public static class Param {
        public String type;
        public String name;
        public Param(String t, String n){ this.type = t; this.name = n;}
        @Override public String toString(){ return type + " " + name; }
    }

    public List<InstanceVar> instanceVars = new ArrayList<>();
    public List<MethodInfo> methods = new ArrayList<>();
}
