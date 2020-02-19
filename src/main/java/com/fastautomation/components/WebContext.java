package com.fastautomation.components;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import com.fastautomation.core.Browser;

public class WebContext {
	
	private Map<String, Browser> browsers = new HashMap<String, Browser>();
	private String currentBrowserName;
	private Map<String, Class<?>> contextMap ;
	private Map<String, Object> contextObjectMap = new HashMap<>();
	private Logger logger = new Logger("WebContext");
	private List<StepInfo> stepInfoList = new LinkedList<StepInfo>();
	private String scenarioDescription;
	private String outputDirPath;
	private static int imageCounter ;
	private static int logCounter ;
	private StepInfo currentStepInfo;
	private Map<String, String> environment = new LinkedHashMap<>();
	
	
	
	public WebContext(Browser browser, Map<String, Class<?>> contextMap, String outputDirPath) {
		browsers.put(currentBrowserName = "HOME", browser);
		this.contextMap = contextMap;
		this.outputDirPath = outputDirPath;
	}
	
	
	
	List<StepInfo> getStepInfoList() {
		return stepInfoList;
	}
	
	List<StepInfo> getFailedStepInfoList() {
		return getStepInfoList().stream().filter(step -> !step.isPassed()).collect(Collectors.toList());
	}
	
	boolean isPassed() {
		return getFailedStepInfoList().isEmpty();
	}
	
	public void setCurrentBrowser(String name) {
		browsers.put(name, browsers.remove(currentBrowserName));
		currentBrowserName = name;
	}
	
	Browser getCurrentBrowser() {
		return browsers.get(currentBrowserName);
	}
	
	public void switchToBrowser(String name) {
		if(!browsers.containsKey(name)) {
			logger.error("{} Browser not found", name);
			throw new RuntimeException("{} Browser not found");
		}
		
		browsers.put(name, browsers.get(name));
		currentBrowserName = name;
	}
	
	public void switchToNewBrowser(String name) {
		browsers.put(name, browsers.get(currentBrowserName).attachAndGetNewWindow());
		currentBrowserName = name;
	}
	
	public void closeBrowser(String name) {
		browsers.get(name).close();
		browsers.remove(name);
	}
	
	public void close() {
		closeBrowser(currentBrowserName);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends WebComponent> T getComponent(Class<T> cl) {
		return (T) WebComponent.loadComponent(cl, getCurrentBrowser(), null, null);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends WebComponent> T getComponent(Class<T> cl,  Object[] args, Class<?>[] types) {
		return (T) WebComponent.loadComponent(cl, getCurrentBrowser(), args, types);
	}
	
	public StepInfo execute(String stepName, Map<String, String> map) {
		
		StepInfo stepResult = new StepInfo();
		stepInfoList.add(stepResult);
		stepResult.setName(stepName);
		stepResult.setLogFile(new File(getOutputDirPath()+File.separator+"logs", "StepLog."+(++logCounter)+".log" ));
		
		String[] parts = stepName.trim().split("\\s*\\.\\s*");
		if(parts.length != 2) {
			logger.error("Invalid step name {}", stepName);
			stepResult.setPassed(false);
			return stepResult;
		}
		
		Object contextObj = createOrGetContextObject(parts[0]);
		
		if(contextObj == null) {
			stepResult.setPassed(false);
			return stepResult;
		}
		
		List<Method> methods = Arrays.stream(contextObj.getClass().getMethods()).filter(m->{
			Step step = m.getAnnotation(Step.class);
			if(step == null) return false;
			return step.name().equals(parts[1]);
		}).collect(Collectors.toList());
		
		if(methods.isEmpty()) {
			logger.error("Unable to find step {} in context {}", parts[1], parts[0]);
			stepResult.setPassed(false);
			return stepResult;
		}
		
		if(methods.size() > 1) {
			logger.error("Found multiple method with step {} in context {}", parts[1], parts[0]);
			stepResult.setPassed(false);
			return stepResult;
		}
		
		Method method = methods.get(0);
		
		stepResult.setDescription(method.getAnnotation(Step.class).desc());
		
		Object[] args = getArgumentsForParameter(method, map, getCurrentBrowser());
		
		

		try {
			currentStepInfo = stepResult;
			logger.startScenarioStep(stepResult);
			stepResult.setPassed(true);
			Object data = method.invoke(contextObj, args);
			stepResult.setData(data);

		} catch (Throwable ex) {
			ex.printStackTrace();
			stepResult.setPassed(false);
			
		} finally {
			if(!stepResult.isPassed()) stepResult.setImageFile(captureScreen());
			logger.endScenarioStep(stepResult);
			currentStepInfo = null;
		}



		
		
		return stepResult;
	}
	
	
	
	private File captureScreen() {
		File file = new File(getOutputDirPath() + File.separator + "images", "screenshot."+(++imageCounter)+".png");
		file.getParentFile().mkdirs();
		try {
			TakesScreenshot screenShot = (TakesScreenshot) getCurrentBrowser().getWebDriver();
			Files.write(file.toPath(), screenShot.getScreenshotAs(OutputType.BYTES), StandardOpenOption.CREATE) ;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return file;
	}
	
	private Object[] getArgumentsForParameter(Method method, Map<String, String> map, Browser browser) {
		Parameter[] parameters = method.getParameters();
		if(parameters == null) return null;
		Object[] args = new Object[parameters.length];
		
		for(int i=0; i < parameters.length ; i++) {
			if(parameters[i].getType().equals(Browser.class)) {
				args[i] = browser;
				continue;
			} 
			
			if(parameters[i].getType().equals(WebContext.class)) {
				args[i] = this;
				continue;
			} 
			
			StepParam stepParam = parameters[i].getAnnotation(StepParam.class);
			if(stepParam == null) {
				args[i] = getArgValue(parameters[i], null);
				continue;
			}
			
			String paramName = stepParam.value();
			
			String value = map.get(paramName);
			
			args[i] = getArgValue(parameters[i], value);			
			
		}
		return args;
	}
	
	private Object getArgValue(Parameter parameter, String value) {
		
		if(parameter.getType().equals(Integer.class) || parameter.getType().equals(int.class)) {
			return value == null ? 0 : Integer.parseInt(value);
		} else if(parameter.getType().equals(Long.class) || parameter.getType().equals(long.class)) {
			return value == null ? 0L : Long.parseLong(value);
		} else if(parameter.getType().equals(Float.class) || parameter.getType().equals(float.class)) {
			return value == null ? 0.0f : Float.parseFloat(value);
		} else if(parameter.getType().equals(Double.class) || parameter.getType().equals(double.class)) {
			return value == null ? 0.0d : Double.parseDouble(value);
		} else if(parameter.getType().equals(Character.class) || parameter.getType().equals(char.class)) {
			return value == null ? '\0' : value.charAt(0);
		} else if(parameter.getType().equals(String.class)) {
			return value;
		} else {
			return null;
		}		
		
	}
	
	
	private Object createOrGetContextObject(String contextName) {
		
		Object contextObj = contextObjectMap.get(contextName);
		
		if(contextObj != null) return contextObj;
		
		Class<?> cl = contextMap.get(contextName);
		
		if(cl == null) {
			logger.error("Unable to find class for step context {}", contextName);
			return null;
		}
		
		try {
			contextObj = cl.newInstance();
		} catch(Throwable ex) {
			ex.printStackTrace();
			logger.error("Unable to initialize context {}", contextName);
			return null;
		}
		
		
		contextObjectMap.put(contextName, contextObj);
		
		return contextObj;
	}



	public String getScenarioDescription() {
		return scenarioDescription;
	}



	public void setScenarioDescription(String scenarioDescription) {
		this.scenarioDescription = scenarioDescription;
	}



	public String getOutputDirPath() {
		return outputDirPath;
	}
	
	
	public void setCurrentStepResult(boolean passed) {
		currentStepInfo.setPassed(passed);
	}



	public Map<String, String> getEnvironment() {
		return environment;
	}



	public void addEnvironment(String key, String value) {
		environment.put(key, value);
	}
	

	

}
