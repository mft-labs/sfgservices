package com.mftlabs.sfgservices.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Scanner;

import com.sterlingcommerce.woodstock.workflow.WorkFlowContext;

public class SfgUtils {
	public static HashMap<String, String> GetConfig() {
		String sfgHome = System.getenv().get("AMF_SFG_HOME");
		File file = new File(sfgHome + "/properties/sfgutils.properties");
		HashMap<String, String> dict = new HashMap<String, String>();
		Scanner sc = null;
		try {
			sc = new Scanner(file);
			while (sc.hasNextLine()) {
				String text = sc.nextLine();
				text = text.trim();
				if (text.startsWith("#") || text.length()==0) {
					continue;
				}
				String[] arr = text.split("=");
				String key = arr[0].trim();
				String[] arr2 = arr[1].split("#");
				String value = arr2[0].trim();
				dict.put(key, value);
			}
		} catch (FileNotFoundException e) {
			return null;
		} finally {
			if(sc!=null) {
				sc.close();
			}
		}
		return dict;

	}
	
	public static HashMap<String,Object> runOpsCommand(WorkFlowContext wfc,String node, String method, String adapter) {
		HashMap<String,Object> result = new HashMap<String,Object>();
		result.put("requestInfo",node+"_"+method+"_"+adapter);
		result.put("adapter",adapter);
		
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.redirectErrorStream(true);
		String sfgHome = System.getenv().get("AMF_SFG_HOME");
		
        processBuilder.command(sfgHome+"/bin/opscmd.sh", "-n"+node, "-c"+method,"-pid="+adapter);
        String cmdDetails = processBuilder.command().toString();
        String details = wfc.getWFDContent("RunningCommand");
        if (details == null) {
        	details = cmdDetails;
        } else {
        	details = details +"\n"+cmdDetails;
        }
        wfc.setWFContent("RunningCommand", details);
        try {

    		Process process = processBuilder.start();

    		StringBuilder output = new StringBuilder();

    		BufferedReader reader = new BufferedReader(
    				new InputStreamReader(process.getInputStream()));

    		String line;
    		while ((line = reader.readLine()) != null) {
    			output.append(line + "\n");
    		}

    		int exitVal = process.waitFor();
    		if (exitVal == 0) {
    			result.put("success",true);
    		} else {
    			result.put("success",false);
    		}
    		result.put("response",output.toString());

    	} catch (IOException e) {
    		result.put("success",false);
    		StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            result.put("response",sw.toString());
    	} catch (InterruptedException e) {
    		result.put("success",false);
    		StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
    		result.put("response",sw.toString());
    	}
        return result;
	}
	
	public static HashMap<String,Object> startAdapter(WorkFlowContext wfc,String action, String node, String adapter) {
		return runOpsCommand(wfc,node,"STARTADAPTER",adapter);
	}
	
	public static HashMap<String,Object> stopAdapter(WorkFlowContext wfc,String action, String node, String adapter) {
		return runOpsCommand(wfc,node,"STOPADAPTER",adapter);
	}
}
