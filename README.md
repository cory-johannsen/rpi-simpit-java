# rpi-simpit-java

A spring-boot Java application designed to enable a Raspberry Pi to act as a client to the [KerbalSimpit](https://bitbucket.org/pjhardy/kerbalsimpit/wiki/Protocol) Kerbal Space Program plugin. 

This application serves as a replacement client for the [KerbalSimpit Arduino library](https://bitbucket.org/pjhardy/kerbalsimpit-arduino), and borrows heavily from the source code for understanding the wire protocol.

# Status

As of 6/18/2018, the application can SYNC with KerbalSimpit, subcribe to all telemetry data channels, and decode all incoming datagram into their domain specific types.

Upstream commands are currently limited to ECHO, channel subscription, and action group control (all standard groups and all custom).  

GPIO has been enabled, and can now be used to trigger action groups.  Support for enable switches is also included, as one of the primary goals was the construction of a stage-enable switch that prevents accidental staging.

Ship control commands will be implemented next.

There's shockingly little test coverage.  I'll be adding that as I go.

# To Run

This application is designed to run from within IDEA using the [Embedded Linux JVM Debugger](https://plugins.jetbrains.com/plugin/7738-embedded-linux-jvm-debugger-raspberry-pi-beaglebone-black-intel-galileo-ii-and-several-other-iot-devices-) plugin, which allows for remote execution and debugging on the Raspberry Pi.

Current testing has been done on a Raspberry Pi Mobel B running Raspbian Sketch.

