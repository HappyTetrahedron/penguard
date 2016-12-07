#!/bin/python4

from msg_pb2 import PGPMessage
from google.protobuf.internal import encoder

import dataset
import uuid



class PenguinServerProtocol:
    def __init__(self):
        self.handlers = {
                PGPMessage.GS_REGISTER: self.register_client,
                PGPMessage.GS_DEREGISTER: self.deregister_client,
                PGPMessage.GS_PING: self.handle_ping,
                PGPMessage.GS_GROUP_REQ: self.group_request}
        self.db = dataset.connect('sqlite:///penguard.db')


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
        response = PGPMessage()
        response.type=PGPMessage.SG_ERR
        response.error.error = 'No handler found for this message type.'
        send(response, addr)

    def register_client(self, msg, addr):
        table = self.db['guardians']
        if table.find_one(name=msg.name):
            response = PGPMessage()
            response.type=PGPMessage.SG_ERR
            response.error.error = 'User with that name already exists.'
            send(response, addr)
        else:
            # create unique uuid
            unique_uuid = False
            while not unique_uuid:
                new_uuid = uuid.uuid4
                if not table.find_one(uuid=new_uuid):
                    unique_uuid = True
                    new_uuid = str(new_uuid)

            # store that stuff
            table.insert(dict(name=msg.name, uuid=new_uuid, addr=addr))
            default_handler(msg, addr)

            # build response
            response = PGPMessage()
            response.type=PGPMessage.SG_ACK
            response.ack.uuid = new_uuid
            response.ack.ip = addr[0]
            response.ack.port = addr[1]

            # send
            send(response, addr)


    def deregister_client(self, msg, addr):
        default_handler(msg, addr)

    def handle_ping(self, msg, addr):
        default_handler(msg, addr)

    def group_request(self, msg, addr):
        default_handler(msg, addr)
