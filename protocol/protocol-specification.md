#PGP specification

## Parties

* **Penguard Guardian Service**, PGS
* **Penguard Liaison Server**, PLS

## Message structure

Each PGPMessage contains a type and the name of the sender. The latter can be unset. Each PGPMessage further contains a 'content', which can be one of several types, specified below.

## Message types

* **SG_ACK**: Sent by PLS to PGS to acknowledge a registration or deregistration.
* **SG_ERR**: Sent by PLS to PGS to indicate that registration group request failed.
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

* **Ack**: used for SG_ACK, contains UUID, username, public IP and port of the recipient
* **Error**: used for SG_ERR, contains error message string
* **Ping**: used for GS_PING, contains UUID of the sender
* **SeqNo**: used for GG_VOTE_YES, GG_VOTE_NO, GG_COMMIT and GG_ABORT to identify the specific status that is to be voted on, committed or aborted
* **MergeReq**: used for SG_MERGE_REQ, contains name, ip and port of the guardian requesting the merge
* **GroupReq**: used for GS_GROUP_REQ, contains name of a member of the group the sender wants to join
* **Group**: used for GG_STATUS, GG_GRP_CHANGE, GG_GRP_INFO. Contains a list of
  guardians (each with name, IP and port), a list of penguins (each with name,
  MAC address, and a boolean indicating whether the sender sees that penguin at
  the moment), as well as a sequence number that is increased on every group
  change and can be used to find out which of two differing group stati is the
  newest.

## Guardian Behaviour

The guardian keeps an internal representation of the current group. The representation includes a list of guardians in the group, a list of penguins in the group and a sequence number that is increased with every change made to the group.

For each penguin, the guardian keeps track of which other guardians currently see this penguin.

The guardian keeps a last-seen timestamp for every other guardian of the group. Whenever he receives a message, he will update this timestamp for the according guardian.

The guardian also keeps a flag for the atomic commitment protocol. When the guardian is engaged in an atomic commitment protocol, that flag is true, otherwise false. The initiant of the atomic commit is also stored. This is used to prevent simultaneous commits. Furthermore, the guardian keeps a list of other guardians who have voted yes, which is usually empty.

When a guardian receives a STATUS message, he compares the contained sequence number to his own. If his own is lower, he updates his internal representation accordingly. He also copies the 'seen' states for every penguin to his internal representation and updates the sender's last-seen timestamp in his internal representation.

When a guardian receives an ACK, he simply updates the sender's last-seen timestamp.

When a guardian receives a GRP_CHANGE, he checks his committing flag. If it is false, he sets it to true, remembers the initiant of the commit (i.e. the sender) as well as the content of the message and replies with VOTE_YES. If it is true, he replies with VOTE_NO.

When a guardian receives a VOTE_YES, and his committing flag is set, and he is the initiant, and the sequence number of the message equals the sequence number of the new state, he adds the sender to the list of guardians who have voted yes so far. Otherwise, this message is ignored.

When a guardian receives a VOTE_NO, and his committing flag is set, and he is the initiant, and the sequence number of the message matches the new state's sequence number, he sends an ABORT message to all other group members and clears the committing flag and initiant field as well as the voted-yes list. Otherwise the message is ignored.

When a guardian receives a COMMIT, and the committing flag is set, and the initiant is the sender, and the sequence number matches that of the last GRP_CHANGE, he updates his internal representation according to the previously remembered GRP_CHANGE message and clears the committing flag and the initiant field. Otherwise the message is ignored.

When a guardian receives an ABORT, and the committing flag is set, and the initiant is the sender, and the sequence number matches, he clears his committing flag and the initiant and throws away the last remembered GRP_CHANGE message.

When a guardian receives a GRP_INFO, he creates a new group list which is a merged version of both his internal representation and the list within the message. He then sends a GRP_CHANGE to all members of the new, merged group. 

## Scenarios

### Registering a new user

Jake launches the Penguard app and selects guardian mode. Since Jake runs the
app for the first time, he is prompted for an username. The application
displays a spinner and sends a REGISTER message to the PLS containing the
desired username. The PLS looks up the username in his lookup table, but does
not find it. The PLS generates a fresh UUID to associate with the new user and
stores username, UUID, IP and Port in his lookup table. The PLS sends an ACK
message containing the new username and the newly associated UUID back to the
app. The app transitions to the guarding activity.


### Failed registration

Josh launches the Penguard app and selects guardian mode. Since Josh runs the
app for the first time, he is prompted for an username. He enters his desired
username. The application displays a spinner and sends a REGISTER message to
the PLS containing the desired username. The PLS looks up that username and
finds that it is already stored. The Register message did not contain an UUID,
so the PLS does not update the existing entry. The PLS sends an Error message
to the application. The application displays an error message, informing Josh
that the username is taken, and prompts him for a new one.

### Re-registration

Nico launches the Penguard app and selects guardian mode. Since Nico has used
the app before, it reuses his old username and UUID. The app displays a spinner
and sends a REGISTER message to the PLS containing the username and UUID. The
PLS looks up the username in his table and compares the associated UUID to the
sent one. They match, so the PLS sends an ACK message back to the app
containing the username and the same UUID. The app transitions to the guarding
activity.

### Group join

Nico would like to join the group Josh and Nico have already formed. He selects
the 'join' option within the Penguard app and is prompted for an username of a
member of the group he'd like to join. He enters Josh's username and taps
submit. The PGS sends a GROUP_REQ message to the PLS containing Josh's username
that Nico entered. The PLS looks up Josh in his table and finds the associated
IP and Port. The PLS sends a MERGE_REQ message to Josh containing Nico's
username, IP and port. Josh's phone buzzes, informing Josh of the join request.
Josh looks at his phone and sees a notification informing him that someone
wants to join his Penguard group. He opens the Penguard app. The app shows him
the username of the person requesting the join and prompts him to accept or
deny the request. Josh chooses to accept. Josh's PGS sends a  GROUP_INFO message containing a list of all
guardians of his group (containing Josh and Nico) and all penguins of his group
. Nico's PGS now has all the information on all the new group
members. It assembles a new, merged group status, and gives it a higher
sequence number than both old stati. It then initiates a group change by
sending a GRP_CHANGE message to all guardians of both his own and the other
group, i.e. to Jake and Josh. Jake's and Nico's PGS both accept the change and
reply with a VOTE_YES message containing the same sequence number. Upon receiving both VOTE_YES messages, Nico's
PGS sends out a COMMIT message to all other parties and updates his group list.
Upon receiving the COMMIT, both Jake's and Josh's PGS update their group list
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
a VOTE_YES with the new sequence number. Josh's PGS, upon receiving the VOTE_YES, updates his guardian list
and sends a COMMIT message to Nico. Nico's PGS, upon receiving the COMMIT,
updates his guardian list.

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


