package com.mftlabs.sfgservices.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Scanner;

import com.sterlingcommerce.woodstock.workflow.WorkFlowContext;

public class SfgUtils {
	private static HashMap<String, String> config = null;
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
	
	public static String startSchedule(WorkFlowContext wfc,String action,  String schedule) throws Exception {
		return updateScheduleStatus(wfc,"true",schedule);
	}
	
	public static String stopSchedule(WorkFlowContext wfc,String action,  String schedule) throws Exception {
		return updateScheduleStatus(wfc,"false",schedule);
	}
	
	public static String GetScheduleDetails(WorkFlowContext wfc,String schedule) throws Exception {
		HashMap<String, String> config = GetConfig();
		if (config != null) {
			String apiUrl = config.get("SFG_API_BASEURL");
			String apiUser = config.get("SFG_API_USERNAME");
			String apiPasswd = config.get("SFG_API_PASSWORD");
			
			String auth = apiUser + ":" + apiPasswd;
			byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
			String authHeaderValue = "Basic " + new String(encodedAuth);
			System.out.println(authHeaderValue);
			
			URL url = new URL(apiUrl + "/B2BAPIs/svc/schedules/"+schedule);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Authorization", authHeaderValue);
			con.setConnectTimeout(15000);
			con.setDoOutput(true);
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			try {
				return content.toString();
			}catch(Exception e) {
				StringWriter sw = new StringWriter();
		        e.printStackTrace(new PrintWriter(sw));
				wfc.setWFContent("ExceptionRaised/GetSchedule", sw.toString());
			}
			
		}
		return null;
	}
	
	public static String updateScheduleStatus(WorkFlowContext wfc,String enable, String schedule) throws Exception {
		HashMap<String, String> config = GetConfig();
		if (config != null) {
			String apiUrl = config.get("SFG_API_BASEURL");
			String apiUser = config.get("SFG_SI_USERNAME");
			String apiPasswd = config.get("SFG_SI_PASSWORD");
			
			String auth = apiUser + ":" + apiPasswd;
			byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
			String authHeaderValue = "Basic " + new String(encodedAuth);
			System.out.println(authHeaderValue);
			
			URL url = new URL(apiUrl + "/B2BAPIs/svc/schedules/"+schedule);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("PUT");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Authorization", authHeaderValue);
			con.setConnectTimeout(15000);
			con.setDoOutput(true);
			
			String outputStr = "{\n" + 
					"  \"bpNameValuePairs\": null,\n" + 
					"  \"dailySchedule\": null,\n" + 
					"  \"dateExclusions\": null,\n" + 
					"  \"monthlySchedule\": null,\n" + 
					"  \"runAsUser\": \"admin\",\n" + 
					"  \"runAtStartUp\": true,\n" + 
					"  \"scheduleExclusions\": null,\n" + 
					"  \"scheduleStatusEnabled\": "+enable+",\n" + 
					"  \"timerSchedule\": {\n" + 
					"    \"hours\": 0,\n" + 
					"    \"minutes\": 1\n" + 
					"  },\r\n" + 
					"  \"weeklySchedule\": null\n" + 
					"}\r\n" + 
					"";
			byte[] outputBytes = outputStr.getBytes("UTF-8");
			OutputStream os = con.getOutputStream();
			os.write(outputBytes);

			os.close();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer content = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				content.append(inputLine);
			}
			in.close();
			wfc.setWFContent("Schedule/Result",content.toString());
			
			return content.toString();
		}
		return null;
	}
}
