/**
 *  Copyright 2015 Peter Major
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
metadata {
    definition (name: "MCO 2 Button Switch", namespace: "petermajor", author: "Peter Major") {
        capability "Actuator"
        capability "Switch"
        capability "Refresh"
        capability "Polling"
        capability "Configuration"
        capability "Zw Multichannel"

        fingerprint deviceId:"0x1001", inClusters: "0x20 0x25 0x27 0x60 0x72 0x85 0x86 0x8E 0xEF"
    }

    simulator {
        status "on":  "command: 2003, payload: FF"
        status "off": "command: 2003, payload: 00"
    }

    tiles {
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "on", label: '${name}', action: "switch.off", icon: "st.unknown.zwave.device", backgroundColor: "#79b821"
            state "off", label: '${name}', action: "switch.on", icon: "st.unknown.zwave.device", backgroundColor: "#ffffff"
        }
        standardTile("switchOn", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "on", label:'on', action:"switch.on", icon:"st.switches.switch.on"
        }
        standardTile("switchOff", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "off", label:'off', action:"switch.off", icon:"st.switches.switch.off"
        }
        standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
            state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        main "switch"
        details (["switch", "switchOn", "switchOff", "refresh"])
    }
}

def parse(String description) {
    log.debug "MCO2-parse {$description}"
    def result = null
    if (description.startsWith("Err")) {
        result = createEvent(descriptionText:description, isStateChange:true)
    } else if (description != "updated") {
        def cmd = zwave.parse(description, [0x20: 1, 0x25: 1, 0x60: 3, 0x8E: 2])
        if (cmd) {
            result = zwaveEvent(cmd)
        }
    }
    log.debug("'$description' parsed to $result")
    return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
    log.debug "MCO2-zwaveEvent-BasicReport {$cmd}"
    if (cmd.value == 0) {
        createEvent(name: "switch", value: "off")
    } else if (cmd.value == 255) {
        createEvent(name: "switch", value: "on")
    }
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
    log.debug "MCO2-zwaveEvent-MultiChannelCmdEncap {$cmd}"
    def encapsulatedCommand = cmd.encapsulatedCommand([0x25: 1, 0x20: 1])
    if (encapsulatedCommand) {
        if (state.enabledEndpoints.find { it == cmd.sourceEndPoint }) {
            def formatCmd = ([cmd.commandClass, cmd.command] + cmd.parameter).collect{ String.format("%02X", it) }.join()
            createEvent(name: "epEvent", value: "$cmd.sourceEndPoint:$formatCmd", isStateChange: true, displayed: false, descriptionText: "(fwd to ep $cmd.sourceEndPoint)")
        } else {
            zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint as Integer)
        }
    }
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    log.debug "MCO2-zwaveEvent-Command {$cmd}"
    createEvent(descriptionText: "$device.displayName: $cmd", isStateChange: true)
}

def on() {
    log.debug "MCO2-on"
    commands([zwave.basicV1.basicSet(value: 0xFF), zwave.basicV1.basicGet()])
}

def on(endpoint) {
    log.debug "MCO2-on $endpoint"
    encap(zwave.basicV1.basicSet(value: 0xFF), endpoint)
}

def off() {
    log.debug "MCO2-off"
    commands([zwave.basicV1.basicSet(value: 0x00), zwave.basicV1.basicGet()])
}

def off(endpoint) {
    log.debug "MCO2-off $endpoint"
    encap(zwave.basicV1.basicSet(value: 0x00), endpoint)
}

def refresh() {
    log.debug "MCO2-refresh"
    commands([
        encap(zwave.basicV1.basicGet(), 1),
        encap(zwave.basicV1.basicGet(), 2),
        zwave.basicV1.basicGet()
    ])
}

def poll(){
    refresh()
}

def configure() {
    log.debug "MCO2-configure"
    enableEpEvents("1,2")
    commands([
        //zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier:3),
        zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:3, nodeId:zwaveHubNodeId)
    ], 800)
}

def enableEpEvents(enabledEndpoints) {
    log.debug "MCO2-enabledEndpoints"
    state.enabledEndpoints = enabledEndpoints.split(",").findAll()*.toInteger()
    null
}

private command(physicalgraph.zwave.Command cmd) {
    cmd.format()
}

private commands(commands, delay=200) {
    delayBetween(commands.collect{ command(it) }, delay)
}

private encap(cmd, endpoint) {
    log.debug "MCO2-encap $endpoint {$cmd}"
    if (endpoint) {
        command(zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd))
    } else {
        command(cmd)
    }
}
