package com.mftlabs.sfgservices.bpexec;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.mftlabs.sfgservices.utils.IoUtil;
import com.mftlabs.sfgservices.utils.SfgUtils;
import com.mftlabs.sfgservices.utils.XmlParser;
import com.sterlingcommerce.woodstock.workflow.Document;
import com.sterlingcommerce.woodstock.workflow.WorkFlowContext;

public class SfgAdapterManager {
	 public static String getDoc(InputStream input)  {
		  String text = new BufferedReader(
			      new InputStreamReader(input, StandardCharsets.UTF_8))
				  .lines()
			        .collect(Collectors.joining("\n"));
		 return text;
	 }
	public static void execute(WorkFlowContext wfc) {
		Document doc = new Document();
		Writer writer = null;
		try {
			/*String filename = (String)wfc.getWFContent("Service/path/text()");
			String[] file_details = filename.split("_");
			String env = file_details[0];
	        String dc = file_details[1].toLowerCase();
	        String action = file_details[2];
	        String node = file_details[3];*/
	        String requestInfo = getDoc((InputStream)wfc.getPrimaryDocument().getInputStream());
	        HashMap<String, Object> details = XmlParser.parseXml(requestInfo);
	        String node = (String) details.get("node");
	        String action = (String) details.get("action");
	        ArrayList<String> adapters = (ArrayList<String>) details.get("adapters");
	        ArrayList<HashMap<String,Object>> status = new ArrayList<HashMap<String,Object>>();
	        for (String adapter : adapters) {
	        	if (action.toLowerCase().equalsIgnoreCase("start")) {
		        	status.add(SfgUtils.startAdapter(wfc,action, node, adapter));
		        } else if (action.toLowerCase().equalsIgnoreCase("stop")) {
		        	status.add(SfgUtils.stopAdapter(wfc,action, node, adapter));
		        }
	        }
	        
	        writer = new OutputStreamWriter((OutputStream)doc.getOutputStream());
	        String result = "<Result>\n";
	        writer.write("<Result>\n");
	        for (HashMap<String,Object> info:status) {
	        	/*writer.write("Request:"+(String)info.get("requestInfo")+"\n");
	        	if ((boolean)info.get("success")) {
	        		writer.write("Success: true\n");
	        	} else {
	        		writer.write("Success: false\n");
	        	}
	        	writer.write("Result:\n"+(String)info.get("response"));
	        	writer.write("\n\n");*/
	        	result+="<Adapter>\n";
	        	writer.write("<Adapter>\n");
	        	result += "<Name>"+info.get("adapter")+"</Name>\n";
	        	writer.write("<Name>"+info.get("adapter")+"</Name>\n");
	        	String response = (String) info.get("response");
	        	String actionStatus = "Unknown";
	        	if (response.contains("<activestatus>1</activestatus>")) {
	        		actionStatus="Running";
	        	} else if (response.contains("<activestatus>0</activestatus>")) {
	        		actionStatus="Stopped";
	        	}
	        	result+="<Status>"+actionStatus+"</Status>\n";
	        	writer.write("<Status>"+actionStatus+"</Status>\n");
	        	String success = "";
	        	if ((boolean)info.get("success")) {
	        		success = "true";
	        	} else {
	        		success = "false";
	        	}
	        	writer.write("<Success>"+success+"</Success>\n");
	        	writer.write("<More Details>\n"
	        			+ "<![CDATA["+(String)info.get("response")+"]]\n"
	        					+ "</More Details>\n");
	        	result+="</Adapter>\n";
	        	writer.write("</Adapter>\n");
	        }
	        result += "</Result>";
	        writer.write("</Result>\n");
	        wfc.setWFContent("Service/Output", result);
		} catch(Exception e) {
			StringWriter sw = new StringWriter();
	        e.printStackTrace(new PrintWriter(sw));
	        wfc.setWFContent("Service/Status", "Failed");
	        wfc.setWFContent("Service/StatusText", sw.toString());
			throw new RuntimeException(e);
		} finally {
			IoUtil.close(writer);
		}
		wfc.putPrimaryDocument(doc);
	}
}
