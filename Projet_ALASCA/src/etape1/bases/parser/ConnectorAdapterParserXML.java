package etape1.bases.parser;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;

public class ConnectorAdapterParserXML {

	private static final String NS = "http://www.sorbonne-universite.fr/alasca/control-adapter";

	public static ConnectorAdapterInfo parse(String xmlPath) throws Exception {
		ConnectorAdapterInfo connectorInfos = new ConnectorAdapterInfo();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new File(xmlPath));
		Element root = doc.getDocumentElement();

		connectorInfos.uid = root.getAttribute("uid");
		connectorInfos.offered = root.getAttribute("offered");

		// required
		NodeList reqs = root.getElementsByTagNameNS(NS, "required");
		for (int i = 0; i < reqs.getLength(); i++) {
			connectorInfos.required.add(reqs.item(i).getTextContent().trim());
		}

		// instance-var
		NodeList vars = root.getElementsByTagNameNS(NS, "instance-var");
		for (int i = 0; i < vars.getLength(); i++) {
			Element ve = (Element) vars.item(i);
			ConnectorAdapterInfo.InstanceVar v = new ConnectorAdapterInfo.InstanceVar();
			v.modifiers = ve.getAttribute("modifiers").trim();
			v.type = ve.getAttribute("type").trim();
			v.name = ve.getAttribute("name").trim();
			v.staticInit = ve.getAttribute("static-init").trim();
			connectorInfos.instanceVars.add(v);
		}

		// methods
		NodeList children = root.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node n = children.item(i);
			if (!(n instanceof Element))
				continue;
			Element e = (Element) n;
			String local = e.getLocalName();

			// ignore already parsed tags
			if ("required".equals(local) || "instance-var".equals(local) || "consumption".equals(local))
				continue;

			ConnectorAdapterInfo.MethodInfo mi = new ConnectorAdapterInfo.MethodInfo();
			mi.name = local;

			// if <internal> has attributes
			if ("internal".equals(local)) {
				mi.modifiers = e.getAttribute("modifiers").trim();
				mi.returnType = e.getAttribute("type").trim();
				mi.name = e.getAttribute("name").trim();
			} else {
				mi.modifiers = "public";
				mi.returnType = null;
			}

			// parameters
			NodeList params = e.getElementsByTagNameNS(NS, "parameter");
			for (int j = 0; j < params.getLength(); j++) {
				Element p = (Element) params.item(j);
				String ptype = p.getAttribute("type");
				if (ptype == null || ptype.isEmpty())
					ptype = "int";
				String pname = p.getAttribute("name");
				mi.parameters.add(new ConnectorAdapterInfo.Param(ptype.trim(), pname.trim()));
			}

			// thrown exceptions
			NodeList thrown = e.getElementsByTagNameNS(NS, "thrown");
			for (int j = 0; j < thrown.getLength(); j++) {
				mi.thrown.add(thrown.item(j).getTextContent().trim());
			}

			// body
			NodeList bodies = e.getElementsByTagNameNS(NS, "body");
			if (bodies.getLength() > 0) {
				Element be = (Element) bodies.item(0);
				mi.equipmentRef = be.getAttribute("equipmentRef");
				mi.body = be.getTextContent().trim();
				if (mi.equipmentRef != null && !mi.equipmentRef.isEmpty()) {
					String pattern = "\\b" + mi.equipmentRef + "\\b";
					String replacement = "(((" + connectorInfos.offered + ") this.offering))";
					mi.body = mi.body.replaceAll(pattern, replacement);
				}
			} else {
				mi.body = "";
			}

			// infer return type
			if (mi.returnType == null || mi.returnType.isEmpty()) {
				mi.returnType = inferReturnType(mi);
			}

			connectorInfos.methods.add(mi);
		}

		return connectorInfos;
	}

	private static String inferReturnType(ConnectorAdapterInfo.MethodInfo mi) {
		String b = mi.body == null ? "" : mi.body;
		if (b.contains("return true") || b.contains("return false"))
			return "boolean";
		if (b.contains("return 0.0") || b.contains("return 0.0;"))
			return "double";
		if (b.contains("return MAX_MODE") || b.contains("return this.currentMode"))
			return "int";
		if (mi.name.equals("suspend") || mi.name.equals("upMode") || mi.name.equals("downMode")
				|| (mi.name.equals("suspended") || mi.name.equals("setMode")))
			return "boolean";
		if (mi.name.equals("maxMode") || mi.name.equals("currentMode"))
			return "int";
		if (mi.name.equals("getModeConsumption") || mi.name.equals("emergency"))
			return "double";
		return "void";
	}
}
