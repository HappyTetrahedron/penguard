#PGP specification

## Parties

* **Penguard Guardian Service**, PGS
* **Penguard Liaison Server**, PLS

## Message structure

Each PGPMessage contains a type and the name of the sender. The latter can be unset. Each PGPMessage further contains a 'content', which can be one of several types, specified below.

## Message types

* **SG_ACK**: Sent by PLS to PGS to acknowledge a registration or deregistration.
* **SG_ERR**: Sent by PLS to PGS to indicate that registration failed.
* **SG_MERGE_REQ**: Sent by PLS to PGS to inform that another group wants to merge with the PGS' group.
* **GS_REGISTER**: Sent by PGS to PLS to register a new or existing user
* **GS_DEREGISTER**: Sent by PGS to PLS to deregister an existing user
* **GS_PING**: Sent by PGS to PLS such that the PLS can detect the IP and port of that user.
* **GS_GROUP_REQ**: Sent by PGS to PLS to request joining another group.
* **GG_STATUS**: Sent by PGS to PGS to inform of the current status (group members and penguins)
* **GG_ACK**: Sent by PGS to PGS to acknowledge receiving a GG_STATUS message.
* **GG_GRP_CHANGE**: Sent by PGS to PGS to initiate a change in the group (penguins or guardians). Acts as a request-to-commit.
* **GG_COMMIT**: Sent by PGS to PGS to conclude atomic commitment and commit
* **GG_ABORT**: Sent by PGS to PGS to conclude atomic commitment and abort
* **GG_VOTE_YES**: Sent by PGS to PGS during atomic commitment to vote for commit
* **GG_VOTE_NO**: Sent by PGS to PGS during atomic commitment to vote for abort
* **GG_GRP_INFO**: Sent by PGS to PGS when allowing another group to join.

## Content types

* **Ack**: used for SG_ACK, contains UUID and username of the recipient
* **Error**: used for SG_ERR, contains error message string
* **Ping**: used for GS_PING, contains UUID of the sender
* **MergeReq**: used for SG_MERGE_REQ, contains name, ip and port of the guardian requesting the merge
* **GroupReq**: used for GS_GROUP_REQ, contains name of a member of the group the sender wants to join
* **Group**: used for GG_STATUS, GG_GRP_CHANGE, GG_GRP_INFO. Contains a list of
  guardians (each with name, IP and port), a list of penguins (each with name,
  MAC address, and a boolean indicating whether the sender sees that penguin at
  the moment), as well as a sequence number that is increased on every group
  change and can be used to find out which of two differing group stati is the
  newest.


## Scenarios

### Registering a new user

Jake launches the Penguard app and selects guardian mode. Since Jake runs the
app for the first time, he is prompted for an username. The application
displays a spinner and sends a register message to the PLS containing the
desired username. The PLS looks up the username in his lookup table, but does
not find it. The PLS generates a fresh UUID to associate with the new user and
stores username, UUID, IP and Port in his lookup table. The PLS sends an ACK
message containing the new username and the newly associated UUID back to the
app. The app transitions to the guarding activity.


### Failed registration

Josh launches the Penguard app and selects guardian mode. Since Josh runs the
app for the first time, he is prompted for an username. He enters his desired
username. The application displays a spinner and sends a register message to
the PLS containing the desired username. The PLS looks up that username and
finds that it is already stored. The Register message did not contain an UUID,
so the PLS does not update the existing entry. The PLS sends an Error message
to the application. The application displays an error message, informing Josh
that the username is taken, and prompts him for a new one.

### Re-registration

Nico launches the Penguard app and selects guardian mode. Since Nico has used
the app before, it reuses his old username and UUID. The app displays a spinner
and sends a register message to the PLS containing the username and UUID. The
PLS looks up the username in his table and compares the associated UUID to the
sent one. They match, so the PLS sends an ACK message back to the app
containing the username and the same UUID. The app transitions to the guarding
activity.

### Group join

Nico would like to join the group Jake and Nico have already formed. He selects
the 'join' option within the Penguard app and is prompted for an username of a
member of the group he'd like to join. He enters Josh's username and taps
submit. The PGS sends a GROUP_REQ message to the PLS containing Josh's username
that Nico entered. The PLS looks up Josh in his table and finds the associated
IP and Port. The PLS sends a MERGE_REQ message to Josh containing Nico's
username, IP and port. Josh's phone buzzes, informing Josh of the join request.
Josh looks at his phone and sees a notification informing him that someone
wants to join his Penguard group. He opens the Penguard app. The app shows him
the username of the person requesting the join and prompts him to accept or
deny the request. Josh chooses to accept. Josh's PGS sends a GROUP_REQ message
to Nico's PGS. Nico's PGS replies with a GROUP_INFO message containing a list of all
guardians of his group (containing only Nico) and all penguins of his group
(which is empty). Josh's PGS now has all the information on all the new group
members. It assembles a new, merged group status, and gives it a higher
sequence number than both old stati. It then initiates a group change by
sending a GRP_CHANGE message to all guardians of both his own and the joining
group, i.e. to Jake and Nico. Jake's and Nico's PGS both accept the change and
reply with a VOTE_YES message. Upon receiving both VOTE_YES messages, Josh's
PGS sends out a COMMIT message to all other parties and updates his group list.
Upon receiving the COMMIT, both Jake's and Nico's PGS update their group list
as well.

### Quit

Jake would like to leave the group he has formed with Josh and Nico. He stops
the Penguard service and closes the app. Josh's and Nico's PGS continue to send
STATUS messages to Jake, which are all lost. Once Jake's Last-Seen-Time, which
Josh and Nick keep track of, is older than a set time, the application
interface will indicate that Jake is no longer reachable. Josh's and Nico's PGS
will clear the "sees" list of Jack, of which they kept track individually. 

### Kick

Josh and Nico would like to remove Jake from their Penguard group, since he has
left. Josh opens the Penguard app and opens the Guardian list. He selects the
"remove" option next to Jake's name. Josh's PGS creates a new GRP_CHANGE
message with Jake removed from the group's guardians and an increased sequence
number. Josh's PGS sends the GRP_CHANGE message to all group members except
Jake (i.e. only Nico). Nico's PGS, upon receiving the GRP_CHANGE, replies with
a VOTE_YES. Josh's PGS, upon receiving the VOTE_YES, updates his guardian list
and sends a COMMIT message to Nico. Nico's PGS, upon receiving the COMMIT,
updates his guardian list.

### Simultaneous Kick

Nico, Josh, Jake and Phil form a Penguard group. Josh decides to remove Jake
from the group while simultaneously, Nico wants to remove Phil. Hence, Nico
sends a GRP_CHANGE message to Josh and Jake while at the same time, Josh sends
one to Nico and Phil.

Josh receives Nico's GRP_CHANGE after he has sent his own. Because Josh is
already in the middle of the atomic commitment protocol while a new run of the
protocol is initiated, he replies with VOTE_NO. Simultaneously, Nico answers
Josh's GRP_CHANGE with VOTE_NO for the same reason. Jake and Phil each got only
one GRP_CHANGE, to which they answer VOTE_YES to Josh and Nico, respectively.

Josh got a VOTE_NO reply from Nico, so he does not update his group list and
sends an ABORT message to both Nico and Phil. Josh's Penguard application
displays an error message saying that removing Jake from the group failed, and
prompts him to retry. The same thing happens to Nico.


