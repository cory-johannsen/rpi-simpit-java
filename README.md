# rpi-simpit-java

A spring-boot Java application designed to enable a Raspberry Pi to act as a client to the [KerbalSimpit](https://bitbucket.org/pjhardy/kerbalsimpit/wiki/Protocol) Kerbal Space Program plugin. 

This application serves as a replacement client for the [KerbalSimpit Arduino library](https://bitbucket.org/pjhardy/kerbalsimpit-arduino), and borrows heavily from the source code for understanding the wire protocol.

# Status

As of 6/18/2018, the application can SYNC with KerbalSimpit, subcribe to all telemtry data channels, and decode all incoming datagram into their domain specific types.

Upstream commands are currently limited to ECHO and channel subscription, but remaining commands will be implemented next.

# To Run

This application is designed to run from within IDEA using the [Embedded Linux JVM Debugger](https://plugins.jetbrains.com/plugin/7738-embedded-linux-jvm-debugger-raspberry-pi-beaglebone-black-intel-galileo-ii-and-several-other-iot-devices-) plugin, which allows for remote execution and debugging on the Raspberry Pi.

Current testing has been done on a Raspberry Pi Mobel B running Raspbian Sketch.

