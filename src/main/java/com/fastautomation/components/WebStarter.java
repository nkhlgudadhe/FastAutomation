package com.fastautomation.components;

import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.fastautomation.core.Browser;
import com.fastautomation.core.BrowserType;

public class WebStarter {
	
	Logger logger = new Logger("WebStarter");
	
	public static void main(String[] args) {
		
		WebStarter starter = new WebStarter();
		starter.run(args);
	}
	
	public int run(String[] args)  {
		
		CommandLine cmd = getCommandLine(args);
		
		if(cmd == null) {
			return 1;
		}

        String scenarioFile = cmd.getOptionValue("scenario");
        String url = cmd.getOptionValue("url");
        String browserStr = cmd.getOptionValue("browser").trim();
        String outputDirPath = cmd.getOptionValue("output").trim();
        
        String outputDir = outputDirPath + File.separator + "Scenario_Run_" +  new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        
        if(cmd.hasOption("max_wait")) {
        	
        	String max_time= cmd.getOptionValue("max_time");
        	
        	if(max_time.matches("^[0-9]+$")) {
        		logger.error("Invalid option value -max_time {}", max_time);
        		return 1;
        	}
        	
        	Browser.setTimeoutInSeconds(Integer.parseInt(max_time));
        }

        Browser browser = null; 

        switch (browserStr.toUpperCase()) {
	        case "CHROME": browser = new Browser(BrowserType.Chrome, url); break;
	        case "FIREFOX": browser = new Browser(BrowserType.FireFox, url); break;
	        case "EDGE": browser = new Browser(BrowserType.EDGE, url); break;
        }
        
        if(browser == null) {
        	logger.error("invalid browser name {}", browserStr);
        	return 2;
        }
        
        Map<String, List<Class<?>>> contextMap = getContextMap();
        
        Map<String, Class<?>> filteredContextMap = contextMap.entrySet().stream().filter(e -> e.getValue().size() == 1).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().get(0)));
        
        filteredContextMap.keySet().forEach(k -> contextMap.remove(k));
        
        if(!contextMap.isEmpty()) {
        	logger.error("found multiple classes with same context name");
        	contextMap.entrySet().forEach(e -> {
        		logger.error("context {}, classes {}", e.getKey(), e.getValue());
        	});
        	
        	return 3;
        }
        
        
        
        WebContext webContext = new WebContext(browser, filteredContextMap, outputDir);
        webContext.addEnvironment("Browser", browser.getBrowserType().toString());
        webContext.addEnvironment("Url", url);
        
        if(Utilities.getMachineName() != null) {
        	webContext.addEnvironment("Machine", Utilities.getMachineName());
        }        
        
        
        try {
			Logger.setMainPageOut(webContext);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return 1;
		}
        
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        
        engine.put("scenario", webContext);
        
        try {
        	String text = Utilities.getResourceText("Init.js");
			engine.eval(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        try (FileReader reader = new FileReader(scenarioFile)) {
        	engine.eval(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        logger.printSummary(webContext);
        
        logger.createHTMLReport(webContext);
        
        return webContext.isPassed() ? 0 : 4;
	}
	
	
	

	private static Map<String, List<Class<?>>> getContextMap() {
		
		String basePath = "com.fastautomation.steps";
		
		Map<String, List<Class<?>>> map = new HashMap<>();
		
		List<String> classes = Utilities.getClassesFromPakage(basePath);
		for(String clName : classes) {
			Class<?> cl = null;
			try {
				cl = Class.forName(basePath+"."+clName);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				continue;
			}
			StepContext context = cl.getAnnotation(StepContext.class);
			if(context == null) continue;			
			List<Class<?>> cls =  map.get(context.value());			
			if(cls == null) {
				map.put(context.value(), cls = new LinkedList<Class<?>>());
			}			
			cls.add(cl);
		}
		return map;
	}

	

	private CommandLine getCommandLine(String[] args) {
		Options options = new Options();

		Arrays.stream(new Option[] { 
				new Option("scenario",  true, "Scenario.js file path"),
				new Option("url", true, "starting url"),
				new Option("browser", true, "target browser among (Chrome, FireFox, Edge)"),
				new Option("output", true, "output folder for html logs")
		}).forEach(o -> {
			o.setRequired(true);
			options.addOption(o);
		});
		
		options.addOption("max_wait", true, "maximum waiting time in sec for element to appear");

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("WebAutomation -browser browser -scenario scenario -url url [-max_wait max_wait]", options);
			return null;
		}

		return cmd;
	}

}
