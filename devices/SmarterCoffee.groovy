metadata {
	definition (name: "Smarter Coffee", namespace: "petermajor", author: "Peter Major") {
		capability "Actuator"
		capability "Polling"
		capability "Refresh"
		capability "Switch"
        
		command "refresh"
	}
    
	preferences {
	}

    simulator {
    }

	tiles(scale: 2) {

		standardTile("switch", "device.switch", width: 6, height: 4, canChangeIcon: true, decoration: "flat") {
			state "off", label: 'off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: 'on', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
		}

		valueTile("cups", "device.cups", inactiveLabel: false, width: 2, height: 2) {
			state "cups", label:'${currentValue} cups'
		}

		standardTile("strength", "device.strength", inactiveLabel: false, width: 2, height: 2) {
			state "0", label: 'weak strength' 
			state "1", label: 'medium strength'
			state "2", label: 'strong strength'
		}

		standardTile("isGrind", "device.isGrind", inactiveLabel: false, width: 2, height: 2) {
			state "true", label: 'grind mode' 
			state "false", label: 'filter mode'
		}

		valueTile("waterLevel", "device.waterLevel", inactiveLabel: false, width: 2, height: 2) {
			state "0", label: 'water empty' 
			state "1", label: 'water low'
			state "2", label: 'water half full'
			state "3", label: 'water full'
		}

		valueTile("isHotplateOn", "device.isHotplateOn", inactiveLabel: false, width: 2, height: 2) {
			state "true", label: 'heater on', backgroundColor: "#ff0000"  
			state "false", label: 'heater off', backgroundColor: "#ffffff" 
		}

		valueTile("isCarafeDetected", "device.isCarafeDetected", inactiveLabel: false, width: 2, height: 2) {
			state "true", label: 'carafe', backgroundColor: "#ffffff" 
			state "false", label: 'no carafe', backgroundColor: "#ff0000"
		}

		standardTile("refresh", "device.switch", inactiveLabel: false, height: 2, width: 2, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details (["switch", "cups", "strength", "isGrind", "waterLevel", "isHotplateOn", "isCarafeDetected", "refresh"])
	}
}

def parse(description) {

 	log.debug "SmarterCoffee Parse ${description}"

}

def installed() {
	getStatus();
}

def updated() {
	getStatus()
}

def on() {
 	log.debug "SmarterCoffee On"

	def result = [startBrew()]
	result << delayAction(2000)
	result << getStatus()
	result.flatten()
}

def startBrew() {
	def host = getHostAddress()
	if (!host)
		return 

	//def body = [isGrind: false, cups: 1, strength: 0]
	//def p = [method: "POST", path: getDevicePath("brew/on"), body: body, headers: ["HOST": host]]
	def p = [method: "POST", path: getDevicePath("brew/on"), headers: ["HOST": host]]
	def dni = device.deviceNetworkId
    def action = new physicalgraph.device.HubAction(p, dni)

	log.debug "SmarterCoffee startBrew ${action}"
	return action
}

def off() {
 	log.debug "SmarterCoffee Off"
}

def poll() {
	log.debug "SmarterCoffee Poll"
	getStatus()
}

def refresh() {
	log.debug "SmarterCoffee Refresh"
	getStatus()
}

def getStatus() {
	def host = getHostAddress()
	if (!host)
		return 

	def p = [method: "GET", path: getDevicePath(), headers: ["HOST": host, "Accept": "application/json"]]
	def dni = device.deviceNetworkId
	def o = [callback: getStatusCallback]
    def action = new physicalgraph.device.HubAction(p, dni, o)

	log.debug "SmarterCoffee getStatusAction ${action}"
	return action
}

void getStatusCallback(physicalgraph.device.HubResponse hubResponse) {

	log.debug "getStatusCallback ${hubResponse}"

	// TODO - errors?
	if (hubResponse.status != 200) return

	def body = hubResponse.json

	log.debug "getStatusCallback ${body}"

	sendEvent(name: "switch", value: body?.isBrewing ? "on" : "off")
	sendEvent(name: "cups", value: body.cups)	
	sendEvent(name: "strength", value: body.strength)
	sendEvent(name: "isGrind", value: body.isGrind)
	sendEvent(name: "waterLevel", value: body.waterLevel)
	sendEvent(name: "isHotplateOn", value: body.isHotplateOn)
	sendEvent(name: "isCarafeDetected", value: body.isCarafeDetected)
}

def sync(serverAddress, serverPort, serverMac, deviceId) {
	log.debug "SmarterCoffee Sync $serverAddress $serverPort $serverMac $deviceId"

	def serverAddressOld = getDataValue("serverAddress")
	if (serverAddress && serverAddress != serverAddressOld) {
		updateDataValue("serverAddress", serverAddress)
	}
	def serverPortOld = getDataValue("serverPort")
	if (serverPort && serverPort != serverPortOld) {
		updateDataValue("serverPort", serverPort)
	}
	def serverMacOld = getDataValue("serverMac")
	if (serverMac && serverMac != serverMacOld) {
		updateDataValue("serverMac", serverMac)
	}
	def deviceIdOld = getDataValue("deviceId")
	if (deviceId && deviceId != deviceIdOld) {
		updateDataValue("deviceId", deviceId)
	}
}

def getHostAddress() {
	def serverAddress = getDataValue("serverAddress")
	def serverPort = getDataValue("serverPort")
	return (serverAddress && serverPort) ? "$serverAddress:$serverPort" : null
}

def getDevicePath(path) {
	def deviceId = getDataValue("deviceId")
	def basePath = "/api/device/$deviceId"

	return (path!=null && path.length()>0) ? "$basePath/$path" : basePath
}

def delayAction(long time) {
	return new physicalgraph.device.HubAction("delay $time")
}