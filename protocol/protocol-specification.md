#tPGP specification

## Parties

* **Penguard Guardian Service**, PGS
* **Penguard Liaison Server**, PLS

*There is an official PLS at: penguard.vsos.ethz.ch*

## Message structure

Each PGPMessage contains a type and the name of the sender as well as port and
IP of the recipient. All but the first can be unset. Each PGPMessage further
contains a 'content', which can be one of several types, specified below.

## Message types

* **SG_ACK**: Sent by PLS to PGS to acknowledge a registration or deregistration. Content: Ack
* **SG_ERR**: Sent by PLS to PGS to indicate that registration group request failed. Content: Error
* **SG_MERGE_REQ**: Sent by PLS to PGS to inform that another group wants to merge with the PGS' group. Content: MergeReq
* **GS_REGISTER**: Sent by PGS to PLS to register a new user. Content: none.
* **GS_DEREGISTER**: Sent by PGS to PLS to deregister an existing user. Content: GoodBye
* **GS_PING**: Sent by PGS to PLS such that the PLS can detect the IP and port of that user. Content: Ping
* **GS_GROUP_REQ**: Sent by PGS to PLS to request joining another group. Content: GroupReq
* **GG_STATUS_UPDATE**: Sent by PGS to PGS to inform of the current status (group members and penguins). Content: Group
* **GG_ACK**: Sent by PGS to PGS to acknowledge receiving a GG_STATUS message. Content: none
* **GG_GRP_CHANGE**: Sent by PGS to PGS to initiate a change in the group (penguins or guardians). Acts as a request-to-commit. Content: Group
* **GG_COMMIT**: Sent by PGS to PGS to conclude atomic commitment and commit. Content: SeqNo
* **GG_ABORT**: Sent by PGS to PGS to conclude atomic commitment and abort. Content: SeqNo
* **GG_VOTE_YES**: Sent by PGS to PGS during atomic commitment to vote for commit. Content: SeqNo
* **GG_VOTE_NO**: Sent by PGS to PGS during atomic commitment to vote for abort. Content: SeqNo
* **GG_GRP_INFO**: Sent by PGS to PGS when allowing another group to join. Content: Group
* **GG_KICK**: Sent by PGS to PGS to inform the recipient that he has been kicked from the group. Content: none

## Content types

* **Ack**: contains UUID, username, public IP and port of the recipient
* **Error**: contains error message string
* **Ping**: contains UUID of the sender
* **SeqNo**: contains sequence number to identify the specific status that is to be voted on, committed or aborted
* **MergeReq**: contains name, ip and port of the guardian requesting the merge
* **GroupReq**: contains name of a member of the group the sender wants to join
* **Group**: Contains a list of
  guardians (each with name, bad NAT status, IP and port), a list of penguins (each with name,
  MAC address, and a boolean indicating whether the sender sees that penguin at
  the moment), as well as a sequence number that is increased on every group
  change and can be used to find out which of two differing group stati is the
  newest.
* **SeqNo**: contains a sequence number that is associated with a group
* **GoodBye**: contains the PGS' UUID.

## Guardian Behaviour

The guardian keeps an internal representation of the current group. The
representation includes a list of guardians in the group, a list of penguins in
the group and a sequence number that is increased with every change made to the
group. On top of that, information regarding server registration, two-phase
commitment, and group joining is also kept.

For each penguin, the guardian keeps track of which other guardians currently
see this penguin.

For each other guardian, the guardian remembers his IP and port, his "bad NAT"
status and a timestamp denoting the last time a message of this particular
guardian was received.

For the atomic commitment protocol, the guardian keeps 

* a state, which is one of IDLE, COMMIT_REQ_SENT, or VOTED_YES
* The initiant of the ongoing commit
* The "Group" on which is being voted
* a boolean VoteYesReceived, that is true when any other guardian has voted 'yes'
* a boolean VoteNoReceived, which is true when any other guardian has voted 'no'
* a callback function.

For registration, the guardian keeps 

* a state, which is one of UNREGISTERED, REGISTRATION_IN_PROGRESS, or REGISTERED
* The username and UUID that are currently registered, if any. 
* a callback function.

For group joining, the guardian keeps 

* a state, which is one of IDLE, JOIN_REQ_SENT or JOIN_INPROGRESS
* a callback function.

###The guardian responds in the following way to messages:

* When a guardian receives a GG_ACK, he simply updates the sender's last-seen
    timestamp.

* When a guardian receives a GG_GRP_CHANGE, he checks his commitment state. If it
  is IDLE, he sets it to VOTED_YES and sends a GG_VOTE_YES message to the
  sender.  He also remembers the Group that was sent with the GG_GRP_CHANGE, as
  well as the sender's name. If the commitment state is not IDLE, he sends a GG_VOTE_NO to the sender.

* When a guardian receives a GG_STATUS_UPDATE, he checks whether the sender is
  a member of his group. If not, he does nothing. If yes, he first checks the
  sequence number of the Group contained with the message. If it is higher than
  his own, he will update his group representation to match the message's
  Group. Finally, and independently of the sequence number, he updates the
  seenBy-Status of all penguins with respect to the sender, according to the
  Group contained in the message. 

* When a guardian receives a GG_VOTE_YES, and his commitment state is COMMIT_REQ_SENT, and he
  is the initiant, and the sequence number of the message equals the sequence
  number of the Group he remembered, he sets his voteYesReceived to true.
  Otherwise, this message is ignored.

* When a guardian receives a GG_VOTE_NO, and his commitment state is COMMIT_REQ_SENT, and he is
  the initiant, and the sequence number of the message matches the remembered Group's
  sequence number, he sets his VoteNoReceived to true. Otherwise the message is ignored.

* When a guardian receives a GG_COMMIT, and the commitment state is VOTED_YES, and the
  initiant is the sender, and the sequence number matches that of the remembered Group
  , he updates his internal representation according to the previously
  remembered Group and resets the commitment state. Otherwise the message is ignored.

* When a guardian receives a GG_ABORT, and the commitment state is VOTED_YES, and the
  initiant is the sender, and the sequence number matches that of the remembered Group
  , he resets the commitment state. Otherwise the message is ignored.

* When a guardian receives a GG_GRP_INFO, he calls the join state's
  onAccepted() callback. Then, he creates a new Group which is a
  merged version of both his internal representation and the list within the
  message. He then initializes a two phase commit with the new, merged group.
  The commitment state's onAbort() callback will call the join state's onFail()
  callback. The commitment state's onCommit() callback will call the join
  state's onSuccess() callback. On top of that, he sets his join state to
  JOIN_INPROGRESS.

* When a guardian receives a GG_KICK, he first checks if the sender is a member
  of his group. If so, he empties his group, i.e. removes all penguins and all
  guardians except himself.

* When a guardian receives an SG_ACK, he updates his own IP and port according
  to that message. If his registration state is REGISTRATION_IN_PROGRESS, he
  also stores the message's contained UUID and name in his registration state,
  and sets the state to REGISTERED, and calls the registration state's
  onSuccess() callback.

* When a guardian receives an SG_ERR, he has two options:
    * If his registration state is REGISTRATION_IN_PROGRESS, the registration state's callback's onFail() function is called.
    * If his join state is JOIN_REQ_SENT, the join state's callback's onFail() function is called.

* When a guardian receives an SG_MERGE_REQ, he prompts the user whether he
  wants to accept the merge request. If no, no action is performed. If yes,
  then a GG_GROUP_INFO message is sent to the PLS as well as to the guardian
  initiating the merge request (whose IP and port are contained in the
  SG_MERGE_REQ message).

###The guardian can perform the following actions:

* **Join a group**: The guardian sets his join state to JOIN_REQ_SENT and sends
  a GS_GROUP_REQ message to the PLS containing the name of one member of the
  group he wants to join.

* **Initiate a two phase commit**: The guardian sets his commitment state to
  COMMIT_REQ_SENT, sets the initiant to himself, and sets the commitment
  state's Group to a new Group with an increased sequence number. He then sends
  a GG_GRP_CHANGE message to all guardians that are a member of the new group.
  The guardian then waits for a certain amount of time. After that time, he
  checks his commitment state. If voteYesReceived is true, but voteNoReceived
  is false, he sends a GG_COMMIT message with the same sequence number to all
  members of the new group, and updates his penguin list, guardian list and
  sequence number to match the new group, and calls the commitment state's
  callback's onCommit() method. Otherwise, he sends a GG_ABORT message to all
  members of the new group and calls the commitment state's callback's
  onAbort() method. Then, he resets his commitment state.

* **Send a status update**: The guardian sends a GG_STATUS_UPDATE message to
  all members of his group, containing a representation of his Guardian list,
  Penguin list and sequence number.

* **Register a new username**: The guardian sets his registration state to
  REGISTRATION_IN_PROGRESS and sends a GS_REGISTER message to the PLS. After
  some time, he checks his registration state again. If it is still
  REGISTRATION_IN_PROGRESS, he calls the registration state's onFailed()
  callback and resets the registration state.

* **Deregister**: The guardian sends a GS_DEREGISTER message to the PLS
  containing his UUID, and sets the registration state to UNREGISTERED.

* **Re-register an existing username**: The guardian sends a GS_PING message to
  the PLS containing his name and UUID. He sets his registration state to
  REGISTRATION_IN_PROGRESS. After some time, he checks his registration state
  again. If it is still REGISTRATION_IN_PROGRESS, he calls the registration
  state's onFailed() callback and resets the registration state.

## Hole punching and relaying

Our protocol performs UDP hole punching during group merges as follows:

When guardian A receives an SG_MERGE_REQ, he sends a GG_GROUP_INFO to both the
other guardian B and the PLS. The message to the other guardian is likely lost,
but serves to punch a hole in the NAT of A. The PLS receives the GG_GRP_INFO
and relays it to B. B will then initiate the two phase commitment protocol by
sending a GG_GRP_CHANGE to A (among others), after which a connection from A to
B and vice versa is possible.

This works in many cases. However, some NATs use port randomization, in which
case hole punching is not possible. In this case, messages between guardians
shall be relayed via the PLS.

The guardians use the following method to determine whether they are in such a "bad NAT":

* They regularly check whether they can reach the PLS and any other guardian.
  When they can reach the PLS, but no other guardian, it is to be assumed that
  they are in a bad NAT.
* When a guardian gets a GG_GRP_INFO, but the group change following it fails,
  he assumes that he is in a bad NAT.

When a guardian is in a bad NAT, he sets his own badNat status to true. He then sends all his GG-messages to the PLS instead of the actual recipient. The PLS will then relay it to the correct recipient.

The other guardians will also need to use the PLS as a relay when communicating with this guardian. Because of that, every guardian checks before sending any message whether the recipient has his badNat flag set. If so, the message is sent to the PLS.

Relaying works because the recipient's IP and port can be included in a
message. If the PLS receives any message that is not meant for him, he looks at
the recipientIp and recipientPort fields. If they are set, the message is
relayed, otherwise a generic error is sent to the sender.

## Scenarios

### Registering a new user

Jake launches the Penguard app and selects guardian mode. Since Jake runs the
app for the first time, he is prompted for an username. The application
displays a loading circle and sends a REGISTER message to the PLS containing the
desired username. The PLS looks up the username in his lookup table, but does
not find it. The PLS generates a fresh UUID to associate with the new user and
stores username, UUID, IP and Port in his lookup table. The PLS sends an ACK
message containing the new username and the newly associated UUID back to the
app. The app transitions to the guarding activity.


### Failed registration

Josh launches the Penguard app and selects guardian mode. Since Josh runs the
app for the first time, he is prompted for an username. He enters his desired
username. The application displays a loading circle and sends a REGISTER
message to the PLS containing the desired username. The PLS looks up that
username and finds that it is already stored. The PLS sends an Error message to
the application. The application displays an error message, informing Josh that
the username is taken, and prompts him for a new one.

### Re-registration

Nico launches the Penguard app and selects guardian mode. Since Nico has used
the app before, it reuses his old username and UUID. The app displays a loading
circle and sends a PING message to the PLS containing the username and UUID.
The PLS looks up the username in his table and compares the associated UUID to
the sent one. They match, so the PLS sends an ACK message back to the app
containing the username and the same UUID. The app transitions to the guarding
activity.

### Group join

Jake would like to join the group Josh and Nico have already formed. He selects
the 'join' option within the Penguard app and is prompted for an username of a
member of the group he'd like to join. He enters Josh's username and taps
submit. The PGS sends a GROUP_REQ message to the PLS containing Josh's username
that Nico entered. The PLS looks up Josh in his table and finds the associated
IP and Port. The PLS sends a MERGE_REQ message to Josh containing Jake's
username, IP and port. Josh's phone buzzes, informing Josh of the join request.
Josh looks at his phone and sees a notification informing him that someone
wants to join his Penguard group. He opens the Penguard app. The app shows him
the username of the person requesting the join and prompts him to accept or
deny the request. Josh chooses to accept. Josh's PGS sends a  GROUP_INFO message to Jake and the PLS containing a list of all
guardians of his group (containing Josh and Nico) and all penguins of his group
. Jake's PGS now has all the information on all the new group
members. It assembles a new, merged group status, and gives it a higher
sequence number than both old stati. It then initiates a group change by
sending a GRP_CHANGE message to all guardians of both his own and the other
group, i.e. to Nico and Josh. Josh's and Nico's PGS both accept the change and
reply with a VOTE_YES message containing the same sequence number. Upon receiving both VOTE_YES messages, Jake's
PGS sends out a COMMIT message to all other parties and updates his group list.
Upon receiving the COMMIT, both Nico's and Josh's PGS update their group list
as well.

### Quit

Jake would like to leave the group he has formed with Josh and Nico. He stops
the Penguard service and closes the app. Josh's and Nico's PGS continue to send
STATUS messages to Jake, which are all lost. Once Jake's Last-Seen-Time, which
Josh and Nick keep track of, is older than a set time, the application
interface will indicate that Jake is no longer reachable. Josh and Nico can
then choose to kick Jake from the group.

### Kick

Josh and Nico would like to remove Jake from their Penguard group, since he has
left. Josh opens the Penguard app and opens the Guardian list. He selects the
"remove" option next to Jake's name. Josh's PGS creates a new GRP_CHANGE
message with Jake removed from the group's guardians and an increased sequence
number. Josh's PGS sends the GRP_CHANGE message to all group members except
Jake (i.e. only Nico), and sends a GG_KICK message to Jake. The latter is lost
because Jake is offline. Nico's PGS, upon receiving the GRP_CHANGE, replies
with a VOTE_YES with the new sequence number.  Josh's PGS, upon receiving the
VOTE_YES, updates his guardian list and sends a COMMIT message to Nico. Nico's
PGS, upon receiving the COMMIT, updates his guardian list.

### Simultaneous Kick

Nico, Josh, Jake and Phil form a Penguard group. Josh decides to remove Jake
from the group while simultaneously, Nico wants to remove Phil. Hence, Nico
sends a GRP_CHANGE message to Josh and Jake while at the same time, Josh sends
one to Nico and Phil.

Josh receives Nico's GRP_CHANGE after he has sent his own. Because Josh is
already in the middle of the atomic commitment protocol while a new run of the
protocol is initiated, he replies with a VOTE_NO. Simultaneously, Nico answers
Josh's GRP_CHANGE with VOTE_NO for the same reason. Jake and Phil each got only
one GRP_CHANGE, to which they answer VOTE_YES to Josh and Nico, respectively.

Josh got a VOTE_NO reply from Nico, so he does not update his group list and
sends an ABORT message to both Nico and Phil. Josh's Penguard application
displays an error message saying that removing Jake from the group failed, and
prompts him to retry. The same thing happens to Nico.

### Status update

Nico and Josh form a Penguard group. Nico's PGS sends a STATUS message to Josh,
containing the current group, group sequence number, and a 'seen' status for
every penguin. Josh's PGS receives the message, compares the sequence number to
his own, sees that they are equal and copies the penguin seen stati to his
internal representation of Nico and updates the last seen status therein. He
then sends a single ACK message to Nico. Nico's PGS receives the ACK and
updates the last seen status in his internal representation of Josh.

### Disconnected guardian

Nico, Josh, Jake and Phil form a Penguard group. Nico temporarily lost his
internet connection. Josh decides to add a penguin to the group. He scans for
bluetooth devices using the Penguard app, picks a device from the list, and
gives it a name. Josh's PGS sends a GRP_CHANGE message to Nico, Jake and Phil
containing the new group (which includes Nico, Josh, Jake, Phil, all their
previous penguins and the new penguin) and an increased sequence number. Jake
and Phil both reply with a VOTE_YES message for the current sequence number.
Josh's PGS waits for Nico's response, but doesn't get one. After a timeout,
Josh decides that Nico is absent. Since he has gotten no VOTE_NO message, he
updates his group list and sends a COMMIT message to Nico, Jake and Phil. Jake
and Phil, upon receiving it, update their group lists. Nico regains his
internet connection. Jake sends a STATUS message to Nico containing the new
group and increased sequence number. Nico receives the message and compares the
sequence number to his own. Noticing that it is larger, he updates his group
list.


