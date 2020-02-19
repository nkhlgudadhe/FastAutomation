package com.fastautomation.components;

import com.fastautomation.core.Browser;

public abstract class WebComponent {

	private Browser browser;
	
	final void setBrowser(Browser browser) {
		this.browser = browser;
	}
	
	protected final Browser getBrowser() {
		return browser;
	}
	
	public abstract void waitUntilLoaded();

	@SuppressWarnings("unchecked")
	public <T extends WebComponent> T getComponent(Class<T> cl) {
		return (T) loadComponent(cl, browser, null, null);
	}
	
	@SuppressWarnings("unchecked")
	public <T extends WebComponent> T getComponent(Class<T> cl,  Object[] args, Class<?>[] types) {
		return (T) loadComponent(cl, browser, args, types);
	}
	
	/*public WebComponent getComponent(Class<? extends WebComponent> cl) {
		return loadComponent(cl, browser);
	}*/
	
	final static WebComponent loadComponent(Class<? extends WebComponent> cl, Browser browser, Object[] args, Class<?>[] types) {
		String componentName = cl.getCanonicalName();
		try {
			WebComponent webComponent = null;
			
			if(args == null || args.length == 0) {
				webComponent = cl.newInstance();
			} else {				
				webComponent = cl.getConstructor(types).newInstance(args);
			}
			
			webComponent.setBrowser(browser);
			webComponent.waitUntilLoaded();			
			return webComponent;
		} catch (Exception ex) {
			throw new RuntimeException("Component not loaded : "+componentName, ex);
		}

	}
}
