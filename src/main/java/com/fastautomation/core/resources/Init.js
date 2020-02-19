
function execute(stepName, json) {
	
	var map = new java.util.HashMap();
	
	if( typeof json != 'undefined' || json != null ) {
		for(var prop in json ) {
			map.put(prop, json[prop]+"");
		}
	}
	
	scenario.execute(stepName, map);
}

function description(desc) {
	scenario.setScenarioDescription(desc);
}

function getEnv(envVar) {
	return java.lang.System.getenv(envVar);
}

function getProperty(prop) {
	return java.lang.System.getProperty(prop);
}

function switchToNewBrowser(name) {
	scenario.switchToNewBrowser(name);
}

function switchToBrowser(name) {
	scenario.switchToBrowser(name);
}

function setCurrentBrowser(name) {
	scenario.setCurrentBrowser(name);
}

function closeBrowser(name) {
	scenario.closeBrowser(name);
}

function close() {
	scenario.close();
}