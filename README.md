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

Penguins should be able to detect when they are “lost” and act upon it.  This
obviously contradicts the goal to support any bluetooth device. We want to
support both goals by adding an optional protocol (the Penguard Penguin
Protocol) through which penguins can communicate with the guardians. When the
penguin supports the protocol, it can detect and act upon being lost, otherwise
not.

The major challenges will be

-   Implementing a peer-to-peer protocol for the guardians

-   Detecting bluetooth devices and “tracking” them without making more
    assumptions on them

-   Implementing a protocol for the penguins to talk to the guardians

System Overview
===============

A Penguard group consists of

-   One or more penguins being guarded

-   One or more guardians guarding the penguins

Guardians can be part of at most one group at once. Penguins not
supporting the Penguard Penguin protocol can be part of more than one
group–this cannot be prevented. Penguins supporting the Penguard Penguin
protocol can only be part of one group. It is theoretically possible for
a second group to add it as a penguin not supporting the protocol,
though.

The guardians communicate with each other via Internet using a
peer-to-peer protocol (the Penguard Guardian protocol). They can
discover each other by either using a Penguard Discovery server, or by
broadcasting and receiving discovery packets over the local network. We
want the Penguard Discovery server to be an optional component.

The guardians can detect whether a penguin is in range by using
Bluetooth RSSI. This means that guardians are not required to pair with
penguins.

Penguins can optionally implement the Penguard Penguin protocol, which
is a Bluetooth Low Energy protocol. The guardians act as clients to the
Penguins. The guardians will ping the Penguins regularly, such that the
penguin can detect when it is lost by using a timeout. The penguin should
also advertise what kind of information it requires from the guardians,
which the guardians then must send to it. This information can include
phone numbers, email addresses or similar information and should allow
for the penguin to contact the guardians when it is lost.

The guardians should be able to find out whether any given penguin
supports the Penguard Penguin protocol.

System components
-----------------

Penguard Android Application

:   is an Android app required to use Penguard. The app can act as a
    guardian or as a penguin (guardian mode or penguin mode). When
    acting as a penguin, it does support the Penguard Penguin protocol.
    The user should be able to select what the app should do when the
    penguin is lost. Options include sounding an alarm and sending GPS
    coordinates to the guardians via SMS. When acting as a guardian, it
    provides an user interface that displays the status of each
    monitored penguin. The status includes the signal strength (RSSI),
    which of the other guardians see that specific penguin, and whether
    the penguin supports the Penguard Penguin Protocol.

Guardians

:   monitor the penguins using the Penguard app in Guardian mode. The
    term “Guardian” refers to the Penguard app in guardian mode.

Penguins

:   are bluetooth devices. They are registered with the guardians and
    then monitored. They optionally support the Penguard Penguin
    protocol, allowing them to take actions when they are lost.

Penguard Discovery Server

:   allows guardians to find each other more easily. Guardians can
    register with the Discovery Server. The server keeps a list of all
    Penguard groups. Guardians can request information on a
    specific group. The server will reply with all IP addresses of all
    guardians within that group, such that the new guardian can
    communicate with the other group members. Furthermore, guardians can
    register new groups on the server. When a guardian stops the
    Penguard service, it is deregistered at the server and removed from
    its group. The server will automatically purge empty groups. The
    Penguard Discovery Server should be an optional component. When no
    server is present, guardians exchange the necessary information via
    broadcast packets on the local network.

<!-- TODO: include calibration?-->
Calibration
-----------

The range of RSSI values varies between devices, so it should be
possible for a guardian to calibrate itself. That is done in a guided
process where the penguin is first brought close to the guardian and
then slowly carried away.

Penguard Guardian Protocol
--------------------------

The Guardian Protocol allows guardians to form groups, and it allows
guardians within the same group to communicate.

### Forming groups

Groups can be formed either via a Penguard Discovery Server or using
broadcast packets on the local network. Every group has an unique ID.

One guardian acts as the group’s creator. When a discovery server is used, the
creator will register the group with the server. The server will then notify it
whenever a new member registers with the group. When no dedicated server is
used, the creator will act as the server. It will broadcast the group
information on the local network. Other guardians can pick up that information
and register with the group.

The guardians that do not act as creators will have to register with an
existing group. When a discovery server is present, they ask that server
for information on that specific group by sending the group’s ID. That
ID must hence be known beforehand by the guardian. The server will find
the according group and send the IPs of all registered guardians to the
new guardian.

When no server is present, the guardian will listen for Penguard
discovery packets on the local network. These packets contain the group
ID. When the guardian decides to register with a certain group, it will
ask the group’s creator for the group information, and receive all group
member’s IP addresses from the creator.

### Communicating

When a guardian is registered with a group, it will immediately start
sending its status to the other group members. The status includes
information about which of the penguins currently guarded it sees. It
will also receive similar status updates from other group members.

When a guardian notices that its IP address changes, it will immediately
notify all other guardians.

Guardians should also relay information to other guardians to provide
some redundancy. When a link between two guardians fails but both can
still reach a third guardian, the third guardian should act as a relay
between the two.

Penguard Penguin Protocol
-------------------------

The Penguin protocol allows for penguins to detect when they are lost.

It is a Bluetooth Low Energy protocol.

The penguin should advertise that he supports the Penguin Protocol. It
can be activated and deactivated.

Once activated, the penguin enters its active state. In this state, it
will listen for pings from the guardians and acknowledge them. The
guardians must send these pings regularly. When the penguin does not
receive a ping for long enough, it will consider itself lost and enter
its lost state. Once it receives another ping, it will transition back
to active state.

When deactivated, the penguin will go in its inactive state. In that
state, the penguin will reply to pings, saying that it is inactive.

The penguin can also tell the guardians which information it would like
to receive from the guardians. The guardians will poll for this
information once. They will then send the required information to the
penguin.

The required information should not change.

Requirements
============

During this project, we will need the following hardware:

-   Several Android smartphones

-   Several Bluetooth devices

-   A server (kindly provided by VSOS)

We will need the following software:

-   Android Studio

<!-- TODO: Python? I would maybe add it just in case-->

Work Packages
=============

-   <span>**WP1**</span>: Implement the Penguard Guardian Protocol -
    communication part

-   <span>**WP2**</span>: Implement the Penguard Guardian Protocol -
    group finding part (no discovery server)

-   <span>**WP3**</span>: Write a Penguard service that can handle
    numerous Penguins without Penguin Protocol support

-   <span>**WP4**</span>: Write a Penguard Penguin service that
    implements the Penguard Penguin protocol (as a penguin)

-   <span>**WP5**</span>: Implement calibration functionality for
    guardians

-   <span>**WP6**</span>: Extend the Penguard service so that it can also
    handle Penguins with Penguin Protocol support

-   <span>**WP7**</span>: Write a Penguard Discovery Server supporting
    the Penguard Guardian protocol

-   <span>**WP8**</span>: Implement the Penguard Guardian Protocol -
    group finding part (using discovery server)

-   <span>**WP9**</span>: Design a functional graphical user interface
    for the Penguard app

Milestones
==========

Penguard Guardian Protocol - communication
------------------------------------------

Guardians can communicate with each other, given that their IP addresses
are provided beforehand. Status updates work.

Penguard Guardian Protocol - group finding (local)
--------------------------------------------------

Guardians can create and join groups via discovery on the local network.

Discovery server
----------------

Guardians can create and join groups via a discovery server. The server
correctly handles the groups.

Monitoring a single penguin not supporting the Penguin Protocol
---------------------------------------------------------------

A guardian monitors one penguin and can detect when it is lost.

Monitoring multiple penguins not supporting the Penguin Protocol
----------------------------------------------------------------

A guardian monitors multiple penguins and can detect when either of them
is lost.

Monitoring multiple penguins supporting the Penguin Protocol
------------------------------------------------------------

A guardian correctly uses the Penguard Penguin protocol to detect
whether penguins support the protocol, activate them, detect what
information they require, send it to them, and ping them.

Calibration
-----------

Guardians have the option to calibrate. Calibration is stored locally.

User interface
--------------

The user interface is functional, understandable and polished.

Deliveries
==========

We expect to deliver the following:

-   Code for the Penguard Android application

-   Code for the Penguard Discovery Server

-   Documentation for the Penguard Penguin Protocol

-   Documentation for the Penguard Guardian Protocol


