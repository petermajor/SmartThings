metadata {
	definition (name: "SmarterCoffee", namespace: "petermajor", author: "Peter Major") {
		capability "Actuator"
		capability "Polling"
		capability "Refresh"
		capability "Switch"
    //	capability 	"Sensor"
    //	capability 	"Temperature Measurement"
        
	command "refresh"
    //	command "subscribe"

	//	attribute "network","string"
	//	attribute "bin","string"
	}
    
	preferences {
	//	input("ibrewhost", "text", title: "iBrew Host", description: "IP address or host name of the iBrew server", required: true, displayDuringSetup: true)
	//	input("ibrewport", "number", title: "iBrew Port", description: "Port number that the iBrew server listens on (Default:2080)", defaultValue: "2080", required: true, displayDuringSetup: true)
    //	input("mac", "text", title: "MAC Address", description: "MAC address of server")
    //	input("kettleip", "text", title: "Kettle IP address", description: "IP address of the kettle")
	}

    simulator {
    //    status "on":  '{"command": "success"}'
    //    status "off": "command: 2003, payload: 00"
    }

	tiles {

		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#79b821", nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"
			state "turningOn", label:'${name}', action:"switch.off", icon:"st.Home.home30", backgroundColor:"#79b821", nextState:"turningOff"
			state "turningOff", label:'${name}', action:"switch.on", icon:"st.Home.home30", backgroundColor:"#ffffff", nextState:"turningOn"
		//	state "offline", label:'${name}', icon:"st.Home.home30", backgroundColor:"#ff0000"
		}

		standardTile("refresh", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        
    //	standardTile("subscribe", "device.switch", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
    //		state "default", label:"", action:"subscribe", icon:"st.Appliances.appliances3"
    //	}
        
    //	standardTile("temperature", "device.temperature", inactiveLabel: false, height: 1, width: 1, decoration: "flat") {
    //		state "default", label:'${currentValue}°', unit:"C", icon:"st.Weather.weather2"
    //	}

        main "switch"
        details (["switch", "refresh"])
	}
}

def parse(description) {

 	log.debug "SmarterCoffee Parse ${description}"

// 	def map
// 	def headerString
// 	def bodyString
// 	def slurper
// 	def result
   
   
// 	map = stringToMap(description)
  	
// 	headerString = new String(map.headers.decodeBase64())

// 	if (headerString.contains("200 OK")) {
    
//     	try {
// 			bodyString = new String(map.body.decodeBase64())
// 		} catch (Exception e) {
// 			// Keep this log for debugging StringIndexOutOfBoundsException issue
// 			log.error("Exception decoding bytes in response")
// 			throw e
// 		}
    
		
// 		slurper = new JsonSlurper()
// 		result = slurper.parseText(bodyString)
	
// 		switch (result.status) {
// 			case "ready":
// 				sendEvent(name: 'switch', value: "off" as String)
// 			break;
// 			case "heating":
// 				sendEvent(name: 'switch', value: "on" as String)
				
// 			}
            
//          log.debug "temp = " + result.sensors.temperature.raw.celsius
//          sendEvent(name: "temperature", value: result.sensors.temperature.raw.celsius, unit: "C")
			
// 		}
// 	else {
//         //processes callbacks
//    		bodyString = new String(map.body.decodeBase64())
// 		slurper = new JsonSlurper()
// 		result = slurper.parseText(bodyString)
//         log.debug result
        
//         log.debug result.kettleheater
//         if (result.kettleheater.toString() == "true") {
//             log.debug "The kettle is on"
//         	sendEvent(name: 'switch', value: "on" as String)
//         }
        
//         log.debug result.kettlebusy
//         if (result.kettlebusy.toString() == "false") {
//             log.debug "The kettle is off"
//         	sendEvent(name: 'switch', value: "off" as String)
//         }
        
//         sendEvent(name: "temperature", value: result.temperature, unit: "C")   
//         log.debug "temp = " + result.temperature
// 	}
// 	parse
}

// handle commands

def installed() {
	// if (!device.deviceNetworkId)
	// 	device.deviceNetworkId = "testing123"

	// sync("192.168.1.14", "2080", null, "192.168.1.15")
	// log.debug "Installed: ${device.deviceNetworkId}"
	//initialize()
}

def updated() {
	// if (!device.deviceNetworkId)
	// 	device.deviceNetworkId = "testing123"

	// sync("192.168.1.14", "2080", null, "192.168.1.15")
	// log.debug "Update: ${device.deviceNetworkId}"
	//initialize()
}

// def initialize() {
// 	log.info "iBrew ${textVersion()} ${textCopyright()}"
// 	ipSetup()
// 	poll()
// }

def on() {
 	log.debug "SmarterCoffee On"
// 	ipSetup()
// 	api('on')
}

def off() {
 	log.debug "SmarterCoffee Off"
// 	api('off')
}

def poll() {
	log.debug "SmarterCoffee Poll"
	getStatus()
    
// 	if (device.deviceNetworkId != null) {
// 		api('refresh')
// 	}
// 	else {
// 		sendEvent(name: 'status', value: "error" as String)
// 		sendEvent(name: 'network', value: "Not Connected" as String)
// 		log.debug "DNI: Not set"
// 	}
}

def refresh() {
	log.debug "SmarterCoffee Refresh"
	getStatus()
}

def getStatus() {
	def dni = device.deviceNetworkId
	def path = getDevicePath("status")
	def host = getHostAddress()
	def action = new physicalgraph.device.HubAction(
		"""GET $path HTTP/1.1\r\nHOST: $host\r\n\r\n""", 
		physicalgraph.device.Protocol.LAN, 
		"$dni", 
		[callback: getStatusCallback]);
	// def action = new physicalgraph.device.HubAction(
	// 	method: "GET",
	// 	path: getDevicePath("status"),
	// 	headers: [
	// 		HOST: getHostAddress(), 
	// 		Accept: "application/json"
	// 	]
	// )
	// def p = [
	// 	method: "GET",
	// 	path: getDevicePath("status"),
	// 	headers: [
	// 		HOST: getHostAddress(), 
	// 		Accept: "application/json"
	// 	]
	// ]
	// def action = new physicalgraph.device.HubAction(
	// 	params: p,
	// 	dni: device.deviceNetworkId
	// )
	log.debug "SmarterCoffee getStatusAction ${action}"
	return action
}

void getStatusCallback(physicalgraph.device.HubResponse hubResponse) {

	log.debug "getStatusCallback {$hubResponse}"

	if (hubResponse.status != 200) return

	def body = hubResponse.json

	if (body?.status?.working) {
		sendEvent(name: "switch", value: "on", displayed: false)
	} else {
		sendEvent(name: "switch", value: "off", displayed: false)
	}
}

def sync(ibrewAddress, ibrewPort, ibrewMac, deviceAddress) {
	log.debug "SmarterCoffee Sync $ibrewAddress $ibrewPort $ibrewMac $deviceAddress"

	def ibrewAddressOld = getDataValue("ibrewAddress")
	if (ibrewAddress && ibrewAddress != ibrewAddressOld) {
		updateDataValue("ibrewAddress", ibrewAddress)
	}
	def ibrewPortOld = getDataValue("ibrewPort")
	if (ibrewPort && ibrewPort != ibrewPortOld) {
		updateDataValue("ibrewPort", ibrewPort)
	}
	def ibrewMacOld = getDataValue("ibrewMac")
	if (ibrewMac && ibrewMac != ibrewMacOld) {
		updateDataValue("ibrewMac", ibrewMac)
	}
	def deviceAddressOld = getDataValue("deviceAddress")
	if (deviceAddress && deviceAddress != deviceAddressOld) {
		updateDataValue("deviceAddress", deviceAddress)
	}
}

private String getHostAddress() {
	def ibrewAddress = getDataValue("ibrewAddress")
	def ibrewPort = getDataValue("ibrewPort")
	return "$ibrewAddress:$ibrewPort"
}

private String getDevicePath(path) {
	def deviceAddress = getDataValue("deviceAddress")
	return "/api/$deviceAddress/$path"
}

// def subscribe (){
// 	log.debug "Executing 'subscribe'"
// 	ipSetup()
// 	subscribeAction()
// }

// def api(String APICommand, success = {}) {
// 	def APIPath
// 	def hubAction

// 	switch (APICommand) {
// 		case "on":
// 			APIPath = "/api/" + settings.kettleip + "/start"
// 			log.debug "The start command was sent"
// 		break;
// 		case "off":
// 			APIPath = "/api/" + settings.kettleip + "/stop"
// 			log.debug "The stop command was sent"
// 		break;
// 		case "refresh":
// 			APIPath = "/api/" + settings.kettleip + "/status"
// 			log.debug "The Status Command was sent"
// 		break;
// 	}
  
// 	switch (APICommand) {
// 		case "refresh":
// 			try {
// 				hubAction = new physicalgraph.device.HubAction(
// 				method: "GET",
// 				path: APIPath,
// 				headers: [HOST: "${settings.ip}:${settings.port}", Accept: "application/json"])
// 			}
// 			catch (Exception e) {
// 				log.debug "Hit Exception $e on $hubAction"
// 			}
// 			break;
// 		default:
// 			try {
// 				hubAction = [new physicalgraph.device.HubAction(
// 				method: "GET",
// 				path: APIPath,
// 				headers: [HOST: "${settings.ip}:${settings.port}", Accept: "application/json"]
// 				), delayAction(1000), api('refresh')]
// 			}
// 			catch (Exception e) {
// 				log.debug "Hit Exception $e on $hubAction"
// 			}
// 			break;
// 	}
// 	return hubAction
// }

// private subscribeAction(callbackPath="") {
//     def address = device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")

//     def result = new physicalgraph.device.HubAction(
//         method: "SUBSCRIBE",
//         path: "/api/" + settings.kettleip + "/smartthings",
//         headers: [
//             HOST: "${settings.ip}:${settings.port}",
//             CALLBACK: "<http://${address}/notify$callbackPath>",
//             TIMEOUT: "Second-3600"])
//     sendHubCommand(result)
// }


// def ipSetup() {
// 	def hosthex
// 	def porthex
// 	if (settings.ip) {
// 		hosthex = convertIPtoHex(settings.ip)
// 	}
// 	if (settings.port) {
// 		porthex = convertPortToHex(settings.port)
// 	}
// 	if (settings.mac) {
// 		device.deviceNetworkId = settings.mac
// 	}
// }

// private String convertIPtoHex(ip) { 
// 	String hexip = ip.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
// 	return hexip
// }
// private String convertPortToHex(port) {
// 	String hexport = port.toString().format( '%04x', port.toInteger() )
// 	return hexport
// }
// private delayAction(long time) {
// 	new physicalgraph.device.HubAction("delay $time")
// }



// private def textVersion() {
// 	def text = "Version 0.2"
// }

// private def textCopyright() {
// 	def text = "Copyright © 2016 JB"
// }