package com.mftlabs.sfgservices.utils;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XmlParser {
	/*
	 * <Details>
<Node>node1</Node>
<Action>stop</Action>
<AdapterName>AMF_HTTP_SA_MSG_40449</AdapterName>
<AdapterName>AMF_HTTP_SA_SCH_GET_41234</AdapterName>
</Details>
	 * */

	public static HashMap<String,Object> parseXml(String contents) throws Exception {
		DocumentBuilderFactory factory =DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		StringBuilder xmlStringBuilder = new StringBuilder();
		xmlStringBuilder.append(contents);
		ByteArrayInputStream input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
		Document doc = builder.parse(input);
		Element root = doc.getDocumentElement();
		System.out.println(root.getTagName());
		Element  nodeItem = (Element)root.getElementsByTagName("Node").item(0);
		Element actionItem = (Element)root.getElementsByTagName("Action").item(0);
		NodeList adaptersList = root.getElementsByTagName("AdapterName");
		
		System.out.println("Node: "+nodeItem.getTextContent());
		System.out.println("Action: "+actionItem.getTextContent());
		HashMap<String, Object> result = new HashMap<String,Object>();
		result.put("node",nodeItem.getTextContent());
		result.put("action",actionItem.getTextContent());
		ArrayList<String> adapters = new ArrayList<String>();
		for (int i=0;i<adaptersList.getLength();i++) {
			Element service = (Element)adaptersList.item(i);
			//System.out.println("Node: "+service.getTextContent());
			adapters.add(service.getTextContent());
		}
		result.put("adapters",adapters);
		return result;
	}
}
