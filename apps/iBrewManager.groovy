/**
 *  Generic UPnP Service Manager
 *
 *  Copyright 2016 Peter Major
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

	def devices = getDevices()
	def options = [:]

	if (!devices) {
		discoverDevices()

		return dynamicPage(name: "deviceDiscovery", title: "Discovery Started...", nextPage: "", refreshInterval: 5, install: true, uninstall: true) {
		}
	
	} else {

		devices.each {
			def value = it.value.ip
			def key = it.key
			options["${key}"] = value
		}

		return dynamicPage(name: "deviceDiscovery", title: "Discovery Finished", nextPage: "", install: true, uninstall: true) {
			section("Found ${options.size() ?: 0} Smarter devices on the network") {
				input "selectedDevices", "enum", required: false, title: "Select Devices", multiple: true, options: options
			}
		}
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
}

def updated() {
	log.debug "Updated with settings: ${settings}"
}

def initialize() {
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