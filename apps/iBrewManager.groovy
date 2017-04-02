/**
 *  Generic UPnP Service Manager
 *
 *  Copyright 2016 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
import groovy.json.JsonSlurper

definition(
		name: "SmarterManager",
		namespace: "petermajor",
		author: "Peter Major",
		description: "Smarter Service Manager - discovers Smarter devices on your network",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page(name: "searchTargetSelection", title: "iBrew Server", nextPage: "deviceDiscovery") {
		section("Search Target") {
			input "searchTarget", "string", title: "Network Name / Address", defaultValue: "192.168.1.14:2080", required: true
		}
	}
	page(name: "deviceDiscovery", title: "Device Discovery", content: "deviceDiscovery")
}

def deviceDiscovery() {

	discoverDevices()

	def options = [:]
	def devices = getDevices()

	devices.each {
		def value = it.value.ip
		def key = it.key
		options["${key}"] = value
	}

	return dynamicPage(name: "deviceDiscovery", title: "Discovery Started!", nextPage: "", refreshInterval: 5, install: true, uninstall: true) {
		section("Please wait while we discover your iBrew server. Discovery can take five minutes or more, so sit back and relax! Select your device below once discovered.") {
			input "selectedDevices", "enum", required: false, title: "Select Devices (${options.size() ?: 0} found)", multiple: true, options: options
		}
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	//initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	//unsubscribe()
	//initialize()
}

def initialize() {
	//unsubscribe()
	//unschedule()

	//ssdpSubscribe()

	//if (selectedDevices) {
	//	addDevices()
	//}

	//runEvery5Minutes("ssdpDiscover")
}

// void ssdpDiscover() {
// 	sendHubCommand(new physicalgraph.device.HubAction("lan discovery ${searchTarget}", physicalgraph.device.Protocol.LAN))
// }

// void ssdpSubscribe() {
// 	subscribe(location, "ssdpTerm.${searchTarget}", ssdpHandler)
// }



// Map verifiedDevices() {
// 	def devices = getVerifiedDevices()
// 	def map = [:]
// 	devices.each {
// 		def value = it.value.name ?: "UPnP Device ${it.value.ssdpUSN.split(':')[1][-3..-1]}"
// 		def key = it.value.mac
// 		map["${key}"] = value
// 	}
// 	map
// }

// void verifyDevices() {
// 	def devices = getDevices().findAll { it?.value?.verified != true }
// 	devices.each {
// 		int port = convertHexToInt(it.value.deviceAddress)
// 		String ip = convertHexToIP(it.value.networkAddress)
// 		String host = "${ip}:${port}"
// 		sendHubCommand(new physicalgraph.device.HubAction("""GET ${it.value.ssdpPath} HTTP/1.1\r\nHOST: $host\r\n\r\n""", physicalgraph.device.Protocol.LAN, host, [callback: deviceDescriptionHandler]))
// 	}
// }

// def getVerifiedDevices() {
// 	getDevices().findAll{ it.value.verified == true }
// }

def getDevices() {
	if (!state.devices) {
		state.devices = [:]
	}
	state.devices
}

def discoverDevices() {

 	log.debug "discoverDevices"

	try {
		def action = new physicalgraph.device.HubAction("""GET /api/devices HTTP/1.1\r\nHOST: $searchTarget\r\n\r\n""", physicalgraph.device.Protocol.LAN, searchTarget, [callback: discoverDevicesCallback]);
 		log.debug "action {$action}"
		sendHubCommand(action)
	}
	catch (Exception e) {
		log.debug "Hit Exception $e on $action"
	}
}

void discoverDevicesCallback(physicalgraph.device.HubResponse hubResponse) {

 	log.debug "discoverDevicesCallback {$hubResponse}"

	def body = hubResponse.json
 	log.debug "body {$hubResponse.json}"
	def remoteDevices = body.devices

	def devices = getDevices()

	remoteDevices.each {
		if (!devices."${it?.ip}")
			devices << ["${it?.ip}": it]
	}

 	log.debug "discoverDevicesCallback {$devices}"
}


// def addDevices() {
// 	def devices = getDevices()

// 	selectedDevices.each { dni ->
// 		def selectedDevice = devices.find { it.value.mac == dni }
// 		def d
// 		if (selectedDevice) {
// 			d = getChildDevices()?.find {
// 				it.deviceNetworkId == selectedDevice.value.mac
// 			}
// 		}

// 		if (!d) {
// 			log.debug "Creating Generic UPnP Device with dni: ${selectedDevice.value.mac}"
// 			addChildDevice("smartthings", "Generic UPnP Device", selectedDevice.value.mac, selectedDevice?.value.hub, [
// 				"label": selectedDevice?.value?.name ?: "Generic UPnP Device",
// 				"data": [
// 					"mac": selectedDevice.value.mac,
// 					"ip": selectedDevice.value.networkAddress,
// 					"port": selectedDevice.value.deviceAddress
// 				]
// 			])
// 		}
// 	}
// }

// def ssdpHandler(evt) {
// 	def description = evt.description
// 	def hub = evt?.hubId

// 	def parsedEvent = parseLanMessage(description)
// 	parsedEvent << ["hub":hub]

// 	def devices = getDevices()
// 	String ssdpUSN = parsedEvent.ssdpUSN.toString()
// 	if (devices."${ssdpUSN}") {
// 		def d = devices."${ssdpUSN}"
// 		if (d.networkAddress != parsedEvent.networkAddress || d.deviceAddress != parsedEvent.deviceAddress) {
// 			d.networkAddress = parsedEvent.networkAddress
// 			d.deviceAddress = parsedEvent.deviceAddress
// 			def child = getChildDevice(parsedEvent.mac)
// 			if (child) {
// 				child.sync(parsedEvent.networkAddress, parsedEvent.deviceAddress)
// 			}
// 		}
// 	} else {
// 		devices << ["${ssdpUSN}": parsedEvent]
// 	}
// }

// void deviceDescriptionHandler(physicalgraph.device.HubResponse hubResponse) {
// 	def body = hubResponse.xml
// 	def devices = getDevices()
// 	def device = devices.find { it?.key?.contains(body?.device?.UDN?.text()) }
// 	if (device) {
// 		device.value << [name: body?.device?.roomName?.text(), model:body?.device?.modelName?.text(), serialNumber:body?.device?.serialNum?.text(), verified: true]
// 	}
// }

// private Integer convertHexToInt(hex) {
// 	Integer.parseInt(hex,16)
// }

// private String convertHexToIP(hex) {
// 	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
// }
