Introduction
============

Penguard is an application that allows users to monitor objects and
informs them when these objects get too far away from them. This can be
used as a theft alarm or as an aid to prevent forgetting objects.

While this principle is widely applicable, the idea stems from the need
to guard a plush penguin mascot at an event and prevent it from being
stolen. In the style of this particular use case, the objects to be
monitored will henceforth be called “penguins”, while the people
monitoring them will be called “guardians”.

Penguins can be guarded by several guardians at once, in which case it
is sufficient for the penguin to be “seen” by one of them. Also,
guardians can monitor several penguins at once. The guardians should
know at all times which other guardians see which penguins.

We want to be able to use any kind of bluetooth device as a penguin. The
only requirements should be that it supports bluetooth, and that it
doesn’t automatically turn off bluetooth after a while of inactivity,
even when no device is paired to it.

As an extension, penguins should be able to detect when they are “lost”
and act upon it. This obviously contradicts the goal to support any
bluetooth device. We could support both options by adding an optional
protocol, the Penguard Penguin Protocol through which penguins can
communicate with the guardians. When the penguin supports the protocol,
it can detect and act upon being lost, otherwise not. We want to keep
this option in mind and implement it if we have enough time.

The major challenges will be

-   Implementing a peer-to-peer protocol for the guardians

-   Detecting bluetooth devices and “tracking” them without making more
    assumptions on them

-   (optional) Implementing a protocol for the penguins to talk to the
    guardians

System Overview
===============

A Penguard group consists of

-   One or more penguins being guarded

-   One or more guardians guarding the penguins

Guardians can be part of at most one group at once. Penguins not
supporting the Penguard Penguin Protocol can be part of more than one
group–this cannot be prevented. Penguins supporting the Penguard Penguin
Protocol can only be part of one group. It is theoretically possible for
a second group to add it as a penguin not supporting the protocol,
though.

The guardians communicate with each other via Internet using a
peer-to-peer protocol (the Penguard Guardian Protocol). They can
discover each other by using a Penguard Discovery server. The Discovery
server stores the guardians’ IP addresses and ports and is used to poke
holes when the guardians are behind a NAT.

The guardians can detect whether a penguin is in range by using
Bluetooth RSSI. That means that guardians are not required to pair with
penguins.

Penguins can optionally implement the Penguard Penguin Protocol, which
is a Bluetooth Low Energy protocol. The guardians act as clients to the
Penguins. The guardians will ping the Penguins regularly, such that the
penguin can detect when it is by lost using a timeout. The penguin
should also advertise what kind of information it requires from the
guardians, which the guardians then must send to it. This information
can include phone numbers, email addresses or similar information and
should allow for the penguin to contact the guardians when it is lost.

The guardians should be able to find out whether any given penguin
supports the Penguard Penguin Protocol.

System components
-----------------

Penguard Android Application

:   is an Android app required to use Penguard. The app can act as a
    guardian or as a penguin (guardian mode or penguin mode). When
    acting as a penguin, it does support the Penguard Penguin Protocol.
    The user should be able to select what the app should do when the
    penguin is lost. Options include sounding an alarm and sending GPS
    coordinates to the guardians via SMS. When acting as a guardian, it
    provides an user interface that displays the status of each
    monitored penguin. The status includes the signal strength (RSSI),
    which of the other guardians see that specific penguin, and whether
    the penguin supports the Penguard Penguin Protocol.

Guardians

:   monitor the penguins using the Penguard app in guardian mode. The
    term “guardian” refers to the Penguard app in guardian mode.

Penguins

:   are bluetooth devices. They are registered with the guardians and
    then monitored. They optionally support the Penguard Penguin
    Protocol, allowing them to take actions when they are lost.

Penguard Discovery Server

:   allows guardians to find each other easily. Guardians register with
    the Penguard Discovery Server (henceforth referred to as PDS). The
    server keeps a list of all currently active Penguard groups,
    including information on the group’s guardians and penguins.
    Guardians can request information on a specific group. The server
    will reply with all IP addresses and ports of all guardians within
    that group, such that the new guardian can communicate with the
    other group members. Furthermore, the server will send information
    on the penguins being guarded by the group. Guardians can register
    new groups on the server. When a guardian stops the Penguard
    service, it is deregistered at the server and removed from
    its group. The server will automatically purge empty groups.

Calibration
-----------

The range of RSSI values varies between devices, so it should be
possible for a guardian to calibrate itself. That is done in a guided
process where the penguin is first brought close to the guardian and
then slowly carried away.

Penguard Guardian Protocol
--------------------------

The Penguard Guardian Protocol (henceforth referred to as PGP) allows
guardians to form groups, and it allows guardians within the same group
to communicate.

### Forming groups

Groups can be formed via a PDS.

One guardian acts as the group’s creator. The creator will register the
group with the server and add information on the penguins guarded by
that group. The server will then notify it whenever a new member
registers with the group.

The guardians that do not act as creators will have to register with an
existing group. They ask the Discovery server for information on a
specific group by sending the group’s ID. That ID must hence be known
beforehand by the guardian. The server will find the according group and
send the IPs of all registered guardians to the new guardian, as well as
information on the penguins being guarded.

### Communicating

When a guardian is registered with a group, it will immediately start
sending its status to the other group members. The status includes
information about which of the penguins currently guarded it sees. It
will also receive similar status updates from other group members.

Penguard Penguin Protocol (optional)
------------------------------------

The Penguard Penguin Protocol (henceforth referred to as PPP) allows for
penguins to detect when they are lost.

It is a Bluetooth Low Energy protocol.

The penguin should advertise that he supports the PPP. It can be
activated and deactivated.

Once activated, the penguin enters its active state. In this state, it
will listen for pings from the guardians and acknowledge them. The
guardians must send these pings regularly. When the penguin does not
receive a ping for long enough, it will consider itself lost and enter
its lost state. Once it receives another ping, it will transition back
to active state.

When deactivated, the penguin will go in its inactive state. In that
state, the penguin will reply to pings saying that it is inactive.

The penguin can also tell the guardians which information it would like
to receive from the guardians. The guardians will poll for this
information once. They will then send the required information to the
penguin. The required information should not change.

The PPP and all components using it are considered optional, meaning
that this will be the first thing we strip of the project when time is
not sufficient.

Requirements
============

During this project, we will need the following hardware:

-   Several Android smartphones

-   Several Bluetooth devices

-   A server (kindly provided by VSOS)

We will rely on Java for development of the Android app and the
Discovery Server.

We will need the following software:

-   Android Studio

Work Packages
=============

-   <span>**WP1**</span>: PGP - communication part

-   <span>**WP2**</span>: PGP - group finding part

-   <span>**WP3**</span>: Penguard service (part of the application that
    acts as guardian)

-   <span>**WP4**</span>: PDS

-   <span>**WP5**</span>: Functional graphical user interface for the
    Penguard app

-   <span>**WP6**</span>: (optional) calibration functionality for
    guardians

-   <span>**WP7**</span>: (optional) Write a Penguard Penguin service
    that implements the PPP (as a penguin)

-   <span>**WP8**</span>: (optional) Extend the Penguard service so that
    it can also handle Penguins with PPP support

Milestones
==========

-   **Phase 1**: Define goals and work plan

-   **Phase 2**: Finish design, discuss scenarios and use cases to
    identify weaknesses

-   **Phase 3**: Create work, distribute work groups

-   **Phase 4**: Implement work packages

-   **Phase 5**: Implement application as a whole

-   **Phase 6**: Test application

Deliveries
==========

We expect to deliver the following:

-   Code for the Penguard Android application

-   Code for the PDS

-   Documentation for the PGP

-   (optional) Documentation for the PPP


