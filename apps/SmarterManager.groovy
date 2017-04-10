/**
 *  SmarterManager
 *
 *  Copyright 2017 Peter Major
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

definition(
		name: "SmarterManager",
		namespace: "petermajor",
		author: "Peter Major",
		description: "Smarter Service Manager - discovers Smarter devices on your network",
		iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
		iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
		iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page(name: "ibrew", title: "iBrew Server", nextPage: "deviceDiscovery") {
		section("iBrew") {
			input "ibrewAddress", "string", title: "Address", defaultValue: "192.168.1.14", required: true
			input "ibrewPort", "string", title: "Port", defaultValue: "2080", required: true
		}
	}
	page(name: "deviceDiscovery", title: "Device Discovery", content: "deviceDiscovery")
}

def deviceDiscovery() {

	def devices = getDevices()
	def options = [:]

	if (!devices) {
		discoverDevices()

		return dynamicPage(name: "deviceDiscovery", title: "Discovery Started...", nextPage: "", refreshInterval: 5, install: true, uninstall: true) {
		}
	
	} else {

		devices.each {
			def value = it.value.type?.description
			def key = formatMac(it.value.mac)
			options["$key"] = value
		}

		return dynamicPage(name: "deviceDiscovery", title: "Discovery Finished", nextPage: "", install: true, uninstall: true) {
			section("Found ${options.size() ?: 0} Smarter " + (options.size() == 1 ? "device" : "devices") + " on the network") {
				input "selectedDevices", "enum", required: false, title: "Select Devices", multiple: true, options: options
			}
		}
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	initialize()
}

def initialize() {

	unschedule()

	if (selectedDevices) {
		addDevices()
	}
	// look for device ip address change
	runEvery5Minutes("discoverDevices")
}

def getDevices() {
	if (!state.devices) {
		state.devices = [:]
	}
	state.devices
}

def discoverDevices() {

 	log.debug "discoverDevices"

	try {
		def action = new physicalgraph.device.HubAction("""GET /api/appliances HTTP/1.1\r\nHOST: $ibrewAddress:$ibrewPort\r\n\r\n""", physicalgraph.device.Protocol.LAN, "$ibrewAddress:$ibrewPort", [callback: discoverDevicesCallback]);
 		log.debug "action {$action}"
		sendHubCommand(action)
	}
	catch (Exception e) {
		log.debug "Hit Exception $e on $action"
	}
}

void discoverDevicesCallback(physicalgraph.device.HubResponse hubResponse) {

 	log.debug "discoverDevicesCallback {$hubResponse}"

 	if (hubResponse.status != 200) return

	def body = hubResponse.json
 	log.debug "body {$hubResponse.json}"

	state.ibrewMac = hubResponse.mac

	def devices = getDevices()

	body.values().each {
		def mac = formatMac(it.mac)
		if (devices["$mac"]) {
 			def child = getChildDevice(mac)
			if (child) {
				child.sync(ibrewAddress, ibrewPort, state.ibrewMac, it.ip)
			}

		} else {
 			log.debug "device not known $mac, adding..."
			devices << ["$mac" : it]
		}
	}

 	log.debug "discoverDevicesCallback {$devices}"
}

def addDevices() {
	def devices = getDevices()

	selectedDevices.each { dni ->
		def selectedDevice = devices["$dni"]
		def d
		if (selectedDevice) {
			d = getChildDevices()?.find {
				it.deviceNetworkId == dni
			}
		}

		// TODO
		// iKettle?
		if (!d && selectedDevice.type?.id == 2) {
			def hubId = getHubId()
			log.debug "Creating device with dni: $dni in hub: $hubId"
			addChildDevice("petermajor", "SmarterCoffee", dni, hubId, [
				"name": selectedDevice.type?.description,
				"label": selectedDevice.type?.description,
				"data": [
					"ibrewAddress": ibrewAddress,
					"ibrewPort": ibrewPort,
					"ibrewMac": state.ibrewMac,
					"deviceAddress": selectedDevice.ip
				]
			])
		}
	}
}

def formatMac(mac) {
	return mac.replace(":", "").toUpperCase()
}

// TODO support multiple hubs
// https://community.smartthings.com/t/how-to-get-smartthings-hubs-id/21195/42
def getHubId() {
	def hubs = location.hubs.findAll{ it.type == physicalgraph.device.HubType.PHYSICAL } 
    //log.debug "hub count: ${hubs.size()}"
	return hubs[0].id 
}