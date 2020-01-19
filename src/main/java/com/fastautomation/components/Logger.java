package com.fastautomation.components;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Logger {
	
	private String name;
	
	private final static Pattern pattern = Pattern.compile("\\{\\s*\\}");
	
	private static PrintStream mainPageOut ;
	private static PrintStream currentStepOut ;
	
	public Logger(Class<?> cl) {
		this.name = cl.getCanonicalName();
	}
	
	public Logger(String name) {
		this.name = name;
	}
	
	private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public void info (String str, Object... args) {
		print("INFO", str, args);
	}
	
	public void error (String str, Object... args) {
		print("ERROR", str, args);
	}
	
	public void debug (String str, Object... args) {
		print("DEBUG", str, args);
	}
	
	public void warning (String str, Object... args) {
		print("WARNING", str, args);
	}
	
	void startScenarioStep(StepInfo stepInfo) throws FileNotFoundException {
		stepInfo.getLogFile().getParentFile().mkdirs();
		Logger.currentStepOut = new PrintStream(stepInfo.getLogFile());
		title("Scenario Step : {}", stepInfo.getDescription());		
	}
	
	void endScenarioStep(StepInfo stepInfo) {
		title("{} : {}", stepInfo.isPassed() ? "PASSED" : "FAILED" , stepInfo.getDescription());
		Logger.currentStepOut = null;
	}
	
	
	void printSummary(WebContext context) {
		print();
		title("Summary : {} : {}", context.getScenarioDescription(), context.isPassed() ? "PASSED" : "FAILED");
		
		context.getStepInfoList().stream().filter(s -> s.getDescription() != null).forEach(step -> {
			title("{} : {}", step.getDescription(), step.isPassed() ? "PASSED" : "FAILED");
		});
	}
	
	private void title(String str, Object... args) {
		String title = resoveString(str, args);
		StringBuilder strBuilder = new StringBuilder();
		IntStream.range(0, title.length() + 2).forEach(a -> strBuilder.append("-"));
		String dottedLine = "."+strBuilder.toString()+".";
		print(dottedLine + "\r\n| "+ title + " |\r\n" + dottedLine);
	}
	
	private String resoveString(String str, Object[] args) {
		StringBuilder strBuilder = new StringBuilder();		
		int count =0;
		int start = 0;
		Matcher matcher = pattern.matcher(str);
		while(matcher.find()) {
			
			strBuilder.append(str.substring(start, matcher.start()));
			strBuilder.append(args.length <= count - 1 ? null : args[count++]);
			start = matcher.end();
		}
		
		if(start < str.length()) {
			strBuilder.append(str.substring(start));
		}
		
		return strBuilder.toString();
	}
	
	private void print(String logLevel, String str, Object... args) {		
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(format.format(new Date())).append(" ").append(name).append(" <").append(logLevel).append("> ");
		strBuilder.append(resoveString(str, args));
		String log = strBuilder.toString();
		print(log);
	}
	
	private void print() {
		print("");
	}
	
	private void print(String log) {
		System.out.println(log);
		if(mainPageOut != null) mainPageOut.println(log);
		if(currentStepOut != null) currentStepOut.println(log);
	}

	void createHTMLReport(WebContext webContext) {
		try {
			
			String text = Utilities.getResourceText("Index.html")
					.replace("${SCENARIO_DESC}", webContext.getScenarioDescription())
					.replace("${SCENARIO_RESULT}", webContext.isPassed() ? "PASSED" : "FAILED")
					.replace("${SCENARIO_STATUS_COLOR}", webContext.isPassed() ? "green" : "red");
			
			StringBuilder rowsBuilder = new StringBuilder();
			boolean withImageCol = !webContext.getFailedStepInfoList().isEmpty();
			int stepIdx = 1;
			for(StepInfo step : webContext.getStepInfoList()) {
				if(step.getDescription() == null) continue;
				rowsBuilder.append(getStepInfoRow(stepIdx++, step,withImageCol));
			}
			
			StringBuilder headerBuilder = new StringBuilder();
			headerBuilder.append("<tr><th>Sr. No.</th><th>Step</th><th>Status</th>");
			
			if(withImageCol) {
				headerBuilder.append("<th>Snap</th>");
				
				File imagesDir = new File(webContext.getOutputDirPath() , "images");
				imagesDir.mkdirs();
				Utilities.copyResourceTo("camera.png", imagesDir.getAbsolutePath()+File.separator+"camera.png");
			}
			
			headerBuilder.append("</tr>");
			
			
			StringBuilder envBuilder = new StringBuilder();
			
			webContext.getEnvironment().entrySet().stream().map(e -> {
				return "<tr><td>"+e.getKey()+"</td><td>"+e.getValue()+"</td></tr>";
			}).forEach(envBuilder :: append);
			
			
			
			text = text.replace("${STEP_HEADER}", headerBuilder.toString())
					.replace("${STEP_ROWS}", rowsBuilder.toString())
					.replace("${ENV_ROWS}", envBuilder.toString());
			
			File file = new File(webContext.getOutputDirPath(),  "Index.html");
			file.getParentFile().mkdirs();
			Files.write(file.toPath(), text.getBytes(), StandardOpenOption.CREATE);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getStepInfoRow(int stepIdx, StepInfo stepInfo, boolean withImageCol) {
		StringBuilder stringBuilder = new StringBuilder()
		.append("<tr>")
		.append("<td>").append(stepIdx).append("</td>")
		.append("<td>").append(stepInfo.getDescription()).append("</td>")
		.append("<td>").append("<a target='_blank' style=\"text-decoration: none;\" href=\"./logs/").append(stepInfo.getLogFile().getName()).append("\">")
		.append("<span style='color:"+(stepInfo.isPassed() ? "green" : "red")+"'>").append(stepInfo.isPassed() ? "PASSED" : "FAILED").append("</span>")
		.append("</a>").append("</td>") ;
		
		if(withImageCol) {
			stringBuilder.append("<td>");
			
			if(!stepInfo.isPassed() && stepInfo.getImageFile() != null) {
				stringBuilder.append("<a target='_blank' style=\"text-decoration: none;\" href=\"./images/").append(stepInfo.getImageFile().getName()).append("\"><img src='./images/camera.png' width=30 height=25 /></a>");
			}
			stringBuilder.append("</td>");
		}
		
		stringBuilder.append("</tr>");
		
		return stringBuilder.toString();
	}

	static void setMainPageOut(WebContext context) throws FileNotFoundException {
		File file = new File(context.getOutputDirPath() + File.separator + "logs", "complete.log");
		file.getParentFile().mkdirs();
		mainPageOut = new PrintStream(file);
	}

	

}
