Given: 1 PLS, 3 Guardians, 2 Penguins

# General stuff #

- Users can ignore certain penguins
- All penguins that have already been encountered are saved in a (MAC - Name) database, so they can be found later
- We don't scan at first, we wait for the user to click on the name of the penguin and then search explicitly for that penguin. If we don't find that penguin, the user can click "add penguin" to add a new penguin and give him a name.
- To differentiate the different users on the PLS, the PLS generates UUIDs. If a new user with the same username wants to register, the server will just deny his request.

# PGP PLS registration examples #

- Guardians will register at the PLS as soon as they form a new group (beginning to track a single penguin is already counted as forming a group)
	"Hi I'm user G1, want to form a new group and my penguin is P1"
	Server assigns UUID deadbeef to G1
	Server enters IP address of G1 in a database
	"ACK"
	(The PLS might broadcast the "new group formed"-event to all users registered at the PLS.)
- Guardian 2 wants to join a group.
	G2 -> PLS: "Hi I'm user G2"

	Server assign UUID radish to G2

	PLS -> G2: "ACK"
	G2 -> PLS: "List all users"
	PLS -> G2: "Users are: G1"
	G2 -> PLS: "Join G1"

	PLS -> G1: "G2 wants to join your group"

	User whose phone is G1 decides to accept request
	G1 -> G2: "I'm G1, our group is [G1] with penguins [P1]"
	G2 -> G1: "I'm G2, our group is [G2] with penguins []"
	

# PGP peer-to-peer penguin watch examples #
- We have two kinds of messages: Important ones and less important ones.
	- Important messages include: Merge group, user leaves a group, new penguin joins a group, user disconnected, user has new IP
	- Important messages are sent via 2PC or similar
	- Unimportant messages include: Latest sightings of the penguins
	- Unimportant messages are confirmed with a simple ACK

- Guardians individually update their penguins-last-seen-at-lists.
	G1 -> G2, G3: "I see [P1]"
	G2, G3 -> G1: "ACK"
