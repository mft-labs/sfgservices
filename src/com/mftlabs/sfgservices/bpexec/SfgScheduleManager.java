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

public class SfgScheduleManager {
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
	        String requestInfo = getDoc((InputStream)wfc.getPrimaryDocument().getInputStream());
	        HashMap<String, Object> details = XmlParser.parseXmlForSchedules(requestInfo);
	        String action = (String) details.get("action");
	        ArrayList<String> schedules = (ArrayList<String>) details.get("schedules");
	        ArrayList<HashMap<String,Object>> status = new ArrayList<HashMap<String,Object>>();
	        for (String schedule : schedules) {
	        	HashMap<String,Object> result = new HashMap<String, Object>();
	        	result.put("schedule",schedule);
	        	if (action.toLowerCase().equalsIgnoreCase("start")) {
	        		result.put("response",SfgUtils.startSchedule(wfc,action,  schedule));
	        		result.put("status","Enabled");
		        } else if (action.toLowerCase().equalsIgnoreCase("stop")) {
		        	result.put("response",SfgUtils.stopSchedule(wfc,action,  schedule));
		        	result.put("status","Disabled");
		        }
	        	result.put("success",true);
	        	status.add(result);
	        }
	        
	        writer = new OutputStreamWriter((OutputStream)doc.getOutputStream());
	        String result = "<Result>\n";
	        writer.write("<Result>\n");
	        for (HashMap<String,Object> info:status) {
	        
	        	result+="<Schedule>\n";
	        	writer.write("<Schedule>\n");
	        	result += "<Name>"+info.get("schedule")+"</Name>\n";
	        	writer.write("<Name>"+info.get("schedule")+"</Name>\n");
	        	String response = (String) info.get("response");
	        	if(response.contains("\"rowsAffected\": 1")) {
	        		result+="<Status>"+(String) info.get("status")+"</Status>\n";
		        	writer.write("<Status>"+(String) info.get("status")+"</Status>\n");
	        	} else {
	        		result+="<Status>Failed</Status>\n";
		        	writer.write("<Status>Failed</Status>\n");
	        	}
	        	
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
	        	result+="</Schedule>\n";
	        	writer.write("</Schedule>\n");
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
