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

	public WebComponent getComponent(Class<? extends WebComponent> cl) {
		return loadComponent(cl, browser);
	}
	
	final static WebComponent loadComponent(Class<? extends WebComponent> cl, Browser browser) {
		String componentName = cl.getCanonicalName();
		try {
			WebComponent webComponent = cl.newInstance();
			webComponent.setBrowser(browser);
			webComponent.waitUntilLoaded();			
			return webComponent;
		} catch (Exception ex) {
			throw new RuntimeException("Component not loaded : "+componentName, ex);
		}

	}
}
