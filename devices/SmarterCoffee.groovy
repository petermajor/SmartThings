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

	tiles {

		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${currentValue}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${currentValue}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc"
		}

		standardTile("refresh", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}

        main "switch"
        details (["switch", "refresh"])
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

	if (body?.isBrewing) {
		sendEvent(name: "switch", value: "on")
	} else {
		sendEvent(name: "switch", value: "off")
	}
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
