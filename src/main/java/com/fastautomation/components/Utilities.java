package com.fastautomation.components;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

class Utilities {
	
	
	static String getResourceText(String resourcePath) throws IOException {
		try (InputStream stream = Utilities.class.getResourceAsStream("/com/fastautomation/core/resources/"+resourcePath)) {
			byte[] data = new byte[1000];			
			int len;
			StringBuilder strBuilder = new StringBuilder();
			while((len = stream.read(data)) != -1) {
				strBuilder.append(new String(data, 0, len));
			}
			return strBuilder.toString();
		} 
		
	}
	
	static void copyResourceTo(String resourcePath, String destPath) throws IOException {
		
		try (InputStream stream = Utilities.class.getResourceAsStream("/com/fastautomation/core/resources/"+resourcePath); OutputStream ostream = new FileOutputStream(destPath)) {
			byte[] data = new byte[1000];			
			int len;
			while((len = stream.read(data)) != -1) {
				ostream.write(data, 0, len);
			}
		} 
		
	}
	
	static List<String> getClassesFromPakage(String pakage) {

		String pakagePath = pakage.replaceAll("\\.", "/")+"/";

		URL url = Thread.currentThread().getContextClassLoader().getResource(pakagePath);

		List<String> classes = new LinkedList<String>();

		if(url == null) {
			return null;
		}

		try {

			BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream) url.getContent()));

			List<String> directories = new LinkedList<>();

			String line = null;
			while ((line = reader.readLine()) != null) {
				if(line.endsWith(".class") && !line.contains("$")) {
					String className = line.substring(line.lastIndexOf("/")+1).replace(".class", "");
					classes.add(className);
				} else if(line.matches("^[a-zA-Z0-9_]+$")) {
					directories.add(line);
				}
			}

			directories.forEach(a -> {
				getClassesFromPakage(pakagePath+"."+a).forEach(c -> classes.add(a+"."+c));
			});

		} catch (Exception ex) {
			return classes;
		}

		return classes;
	}
	
	static String getMachineName() {

		try
		{
		    return InetAddress.getLocalHost().getHostName();
		}
		catch (Exception ex)
		{
		    return null;
		}
		
	}
}
