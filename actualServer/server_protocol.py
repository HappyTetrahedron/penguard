#!/bin/python4

from msg_pb2 import PGPMessage
from google.protobuf.internal import encoder



class PenguinServerProtocol:
    def __init__(self):
        self.handlers = {
                PGPMessage.GS_REGISTER: self.register_client,
                PGPMessage.GS_DEREGISTER: self.deregister_client,
                PGPMessage.GS_PING: self.handle_ping,
                PGPMessage.GS_GROUP_REQ: self.group_request}

    def connection_made(self, transport):
        self.transport = transport

    def datagram_received(self, data, addr):
        handle_message(data.decode())
        message = parse_message(data.decode())
        
        print('Received %r from %s' % (message, addr))
        print('Send %r to %s' % (message, addr))
        self.transport.sendto(data, addr)

    def handle_message(self, data):
        msg = parse_message(data)
        handle = self.handlers.get(msg.type, default_handler)
        handle(msg, addr)

    def parse_message(self, data):
        msg = PGPMessage()
        msg.parseFromString(data)
        return msg

    def send(self, msg, addr):
        serialized_message = msg.SerializeToString()
        msg_length = len(serialized_message)
        msg_length = encoder._VarintBytes(msg_length)
        final_message = msg_length + serialized_message
        self.transport.sendto(msg_length + serialized_message, addr)
        


    # =================== HANDLERS ===================
    def default_handler(self, msg, addr):
        msg.type=PGPMessage.SG_ERR
        msg.error.error = 'no handler found for this message type'
        send(msg, addr)

    def register_client(self, msg, addr):
        default_handler(msg, addr)

    def deregister_client(self, msg, addr):
        default_handler(msg, addr)

    def handle_ping(self, msg, addr):
        default_handler(msg, addr)

    def group_request(self, msg, addr):
        default_handler(msg, addr)
