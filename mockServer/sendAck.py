#!/bin/python3

import msg_pb2
import socket
import sys
from google.protobuf.internal import encoder

theip = sys.argv[1]

print(theip)
print(str(sys.argv))

msg = msg_pb2.PGPMessage()
msg.type = msg_pb2.PGPMessage.SG_ACK
msg.ack.ip = theip
msg.ack.port = 6789
msg.ack.uuid = "123e4567-e89b-12d3-a456-426655440000"
msg.ack.name = "username"


msgs = msg.SerializeToString()
msgl = encoder._VarintBytes(len(msgs))
msgf = msgl + msgs

s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
s.sendto(msgf, (theip, 6789))

