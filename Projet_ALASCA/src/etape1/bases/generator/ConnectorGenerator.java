package etape1.bases.generator;

import etape1.bases.parser.ConnectorAdapterInfo;
import javassist.*;


public class ConnectorGenerator {

	private static final String ABSTRACT_CONNECTOR_PACKAGE = "fr.sorbonne_u.components.connectors.AbstractConnector";
	private static final String ADJUSTABLE_CI_PACKAGE = "etape1.bases.AdjustableCI";
	
	
    
    public static Class<?>  generate(ConnectorAdapterInfo info,
                                String className) throws Exception {

        ClassPool pool = ClassPool.getDefault();

        String fullName = className;

        // Check if the class was already loaded in JVM (e.g., re-registration after turnOff/turnOn)
        try {
            Class<?> alreadyLoaded = Class.forName(fullName);
            System.out.println("Classe deja generee, reutilisation : " + fullName);
            return alreadyLoaded;
        } catch (ClassNotFoundException e) {
            // Class doesn't exist yet, proceed with generation
        }

        CtClass cc = pool.makeClass(fullName);

        cc.setSuperclass(pool.get(ABSTRACT_CONNECTOR_PACKAGE));
        cc.addInterface(pool.get(ADJUSTABLE_CI_PACKAGE));

        // Fields
        for (ConnectorAdapterInfo.InstanceVar v : info.instanceVars) {
            // build declaration string
            String decl = v.modifiers + " " + v.type + " " + v.name;
            if (v.staticInit != null && !v.staticInit.isEmpty()) {
                decl += " = " + v.staticInit;
            }
            decl += ";";
            CtField f = CtField.make(decl, cc);
            cc.addField(f);
        }

        // Default constructor
        StringBuilder ctorSrc = new StringBuilder();
        ctorSrc.append("public ").append(className).append("() {\n");
        ctorSrc.append("  super();\n");
        // initialize non-static instance vars using static-init if present & not static
        for (ConnectorAdapterInfo.InstanceVar v : info.instanceVars) {
            if (v.modifiers != null && v.modifiers.contains("static")) continue;
            if (v.staticInit != null && !v.staticInit.isEmpty()) {
                ctorSrc.append("  this.").append(v.name).append(" = ").append(v.staticInit).append(";\n");
            }
        }
        ctorSrc.append("}\n");
        cc.addConstructor(CtNewConstructor.make(ctorSrc.toString(), cc));

        // Methods
        for (ConnectorAdapterInfo.MethodInfo m : info.methods) {
            StringBuilder method = new StringBuilder();
            // modifiers
            method.append(m.modifiers != null ? m.modifiers : "public").append(" ");
            // return type
            method.append(m.returnType != null ? m.returnType : "void").append(" ");
            // name + signature
            method.append(m.name).append("(");
            for (int i = 0; i < m.parameters.size(); i++) {
                ConnectorAdapterInfo.Param p = m.parameters.get(i);
                method.append(p.type).append(" ").append(p.name);
                if (i + 1 < m.parameters.size()) method.append(", ");
            }
            method.append(")");
            // throws
            if (!m.thrown.isEmpty()) {
                method.append(" throws ");
                for (int i = 0; i < m.thrown.size(); i++) {
                    method.append(m.thrown.get(i));
                    if (i + 1 < m.thrown.size()) method.append(", ");
                }
            } else {
                method.append(" throws Exception");
            }

            method.append(" { \n");
           
            if (m.body != null && !m.body.trim().isEmpty()) {
                method.append(m.body).append("\n");
            } else {
               
                if ("boolean".equals(m.returnType)) method.append("return false;\n");
                else if ("int".equals(m.returnType)) method.append("return 0;\n");
                else if ("double".equals(m.returnType)) method.append("return 0.0;\n");
            }
            method.append("}\n");

            try {
                
                CtMethod cm = CtNewMethod.make(method.toString(), cc);
                cc.addMethod(cm);
            } catch (CannotCompileException cce) {
                String fallback = "public " + (m.returnType==null?"void":m.returnType) + " " + m.name + "() throws Exception { throw new RuntimeException(\"method generation failed: " + m.name + "\"); }";
                cc.addMethod(CtNewMethod.make(fallback, cc));
            }
        }
        
        Class<?> connectorGenerated = cc.toClass();
        cc.detach();
        System.out.println("Classe générée : " + fullName);
        return connectorGenerated;

    }
}