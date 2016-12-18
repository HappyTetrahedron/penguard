# Penguard

Penguard is an application that allows you to guard a number of Bluetooth
devices and informs you when they are lost (i.e. out of range). You can even
guard your devices as a team!

# Installation

Penguard can be compiled in Android Studio. Please note that, sine Penguard
uses Google Protocol Buffers, the `protoc` Protocol Compiler is required in
order to install it. On most Linux distribution, the package `protobuf` or
`protobuf-compiler` can be installed via the package manager.

# Protocol

Detailed documentation of the Penguard Guardian Protocol (PGP) can be found in
protocol/protocol-specifications.md. 

The Penguard Penguin Protocol (PPP) has not been implemented and hence also has
no documentation. We were unable to implement it because we discovered that the
Google Nexus 5 does not support BLE advertising, which meant that we had no
devices available on which we could have tested the protocol.
