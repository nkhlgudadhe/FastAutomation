package com.fastautomation.steps.test;

import com.fastautomation.components.Logger;
import com.fastautomation.components.Step;
import com.fastautomation.components.StepContext;
import com.fastautomation.components.StepParam;
import com.fastautomation.components.WebContext;

@StepContext("Test")
public class TestScenarioSteps {
	private Logger logger = new Logger(getClass());
	@Step(desc = "Custom made scenario step 1", name = "test1")
	public void test(WebContext context, @StepParam("str") String str,  @StepParam("a") int a,  @StepParam("b") float b) {
		logger.info("str {}, a {}, b {}", str,a,b);
		context.getComponent(ComplexComponent.class).run();
		context.setCurrentStepResult(false);
	}
	
	@Step(desc = "Custom made scenario step 2", name = "test2")
	public void test1(WebContext context, @StepParam("str") String str,  @StepParam("a") int a,  @StepParam("b") float b) {
		logger.info("str {}, a {}, b {}", str,a,b);
		context.getComponent(ComplexComponent.class).run();
	}
}
