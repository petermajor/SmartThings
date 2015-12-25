/**
 *  Copyright 2015 SmartThings
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
        capability "Configuration"
        capability "Zw Multichannel"

        fingerprint inClusters: "0x60"
        fingerprint inClusters: "0x60, 0x25"
        fingerprint inClusters: "0x60, 0x26"
        fingerprint inClusters: "0x5E, 0x59, 0x60, 0x8E"
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
        def cmd = zwave.parse(description, [0x20: 1, 0x84: 1, 0x98: 1, 0x56: 1, 0x60: 3, 0x8E: 2])
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
    } else {
        [ createEvent(name: "switch", value: "on"), createEvent(name: "switchLevel", value: cmd.value) ]
    }
}

private List loadEndpointInfo() {
    if (state.endpointInfo) {
        state.endpointInfo
    } else if (device.currentValue("epInfo")) {
        fromJson(device.currentValue("epInfo"))
    } else {
        []
    }
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelEndPointReport cmd) {
    log.debug "MCO2-zwaveEvent-MultiChannelEndPointReport {$cmd}"
    updateDataValue("endpoints", cmd.endPoints.toString())
    if (!state.endpointInfo) {
        state.endpointInfo = loadEndpointInfo()
    }
    if (state.endpointInfo.size() > cmd.endPoints) {
        cmd.endpointInfo
    }
    state.endpointInfo = [null] * cmd.endPoints
    //response(zwave.associationV2.associationGroupingsGet())
    [ createEvent(name: "epInfo", value: util.toJson(state.endpointInfo), displayed: false, descriptionText:""),
      response(zwave.multiChannelV3.multiChannelCapabilityGet(endPoint: 1)) ]
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCapabilityReport cmd) {
    log.debug "MCO2-zwaveEvent-MultiChannelCapabilityReport {$cmd}"
    def result = []
    def cmds = []
    if(!state.endpointInfo) state.endpointInfo = []
    state.endpointInfo[cmd.endPoint - 1] = cmd.format()[6..-1]
    if (cmd.endPoint < getDataValue("endpoints").toInteger()) {
        cmds = zwave.multiChannelV3.multiChannelCapabilityGet(endPoint: cmd.endPoint + 1).format()
    } else {
        log.debug "endpointInfo: ${state.endpointInfo.inspect()}"
    }
    result << createEvent(name: "epInfo", value: util.toJson(state.endpointInfo), displayed: false, descriptionText:"")
    if(cmds) result << response(cmds)
    result
}

def zwaveEvent(physicalgraph.zwave.commands.associationv2.AssociationGroupingsReport cmd) {
    log.debug "MCO2-zwaveEvent-AssociationGroupingsReport {$cmd}"
    state.groups = cmd.supportedGroupings
    if (cmd.supportedGroupings > 1) {
        [response(zwave.associationGrpInfoV1.associationGroupInfoGet(groupingIdentifier:2, listMode:1))]
    }
}

def zwaveEvent(physicalgraph.zwave.commands.associationgrpinfov1.AssociationGroupInfoReport cmd) {
    log.debug "MCO2-zwaveEvent-AssociationGroupInfoReport {$cmd}"
    def cmds = []
    /*for (def i = 0; i < cmd.groupCount; i++) {
        def prof = cmd.payload[5 + (i * 7)]
        def num = cmd.payload[3 + (i * 7)]
        if (prof == 0x20 || prof == 0x31 || prof == 0x71) {
            updateDataValue("agi$num", String.format("%02X%02X", *(cmd.payload[(7*i+5)..(7*i+6)])))
            cmds << response(zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:num, nodeId:zwaveHubNodeId))
        }
    }*/
    for (def i = 2; i <= state.groups; i++) {
        cmds << response(zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:i, nodeId:zwaveHubNodeId))
    }
    cmds
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
    log.debug "MCO2-on endpoint $endpoint"
    encap(zwave.basicV1.basicSet(value: 0xFF), endpoint)
}

def off() {
    log.debug "MCO2-off"
    commands([zwave.basicV1.basicSet(value: 0x00), zwave.basicV1.basicGet()])
}

def off(endpoint) {
    log.debug "MCO2-off endpoint $endpoint"
    encap(zwave.basicV1.basicSet(value: 0x00), endpoint)
}

def refresh() {
    log.debug "MCO2-refresh"
    command(zwave.basicV1.basicGet())
}

def configure() {
    log.debug "MCO2-configure"
    enableEpEvents("1,2")
    commands([
        //zwave.multiChannelAssociationV2.multiChannelAssociationRemove(groupingIdentifier:1, nodeId:[1]),
        zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier:1),
        zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier:2),
        //zwave.multiChannelAssociationV2.multiChannelAssociationSet(groupingIdentifier:3, nodeId:[1]),
        zwave.multiChannelAssociationV2.multiChannelAssociationGet(groupingIdentifier:3),
        //zwave.multiChannelAssociationV2.multiChannelAssociationGroupingsGet()
        zwave.multiChannelV3.multiChannelEndPointGet()
    ], 800)
}

def epCmd(Integer ep, String cmds) {
    log.debug "MCO2-epCmd {$ep} {$cmds}"
    def result
    if (cmds) {
        def header = state.sec ? "988100600D00" : "600D00"
        result = cmds.split(",").collect { cmd -> (cmd.startsWith("delay")) ? cmd : String.format("%s%02X%s", header, ep, cmd) }
    }
    result
}

def enableEpEvents(enabledEndpoints) {
    log.debug "MCO2-enabledEndpoints"
    state.enabledEndpoints = enabledEndpoints.split(",").findAll()*.toInteger()
    null
}

private command(physicalgraph.zwave.Command cmd) {
    if (state.sec) {
        zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
    } else {
        cmd.format()
    }
}

private commands(commands, delay=200) {
    delayBetween(commands.collect{ command(it) }, delay)
}

private encap(cmd, endpoint) {
    log.debug "MCO2-encap {$endpoint} {$cmd}"
    if (endpoint) {
        command(zwave.multiChannelV3.multiChannelCmdEncap(destinationEndPoint:endpoint).encapsulate(cmd))
    } else {
        command(cmd)
    }
}

private encapWithDelay(commands, endpoint, delay=200) {
    delayBetween(commands.collect{ encap(it, endpoint) }, delay)
}
