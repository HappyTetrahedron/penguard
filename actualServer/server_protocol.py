#!/bin/python4

from msg_pb2 import PGPMessage
from google.protobuf.internal import encoder
from google.protobuf.internal import decoder

import dataset
import uuid



class PenguinServerProtocol:
    def __init__(self):
        self.handlers = {
                PGPMessage.GS_REGISTER: self.register_client,
                PGPMessage.GS_DEREGISTER: self.deregister_client,
                PGPMessage.GS_PING: self.handle_ping,
                PGPMessage.GS_GROUP_REQ: self.group_request,
                PGPMessage.GG_GRP_INFO: self.group_info_forwarding}
        self.db = dataset.connect('sqlite:///penguard.db')


    def connection_made(self, transport):
        self.transport = transport

    def datagram_received(self, data, addr):
        print('Received message from %s' % (addr[0]))
        self.handle_message(data, addr)

    def handle_message(self, data, addr):
        msg = self.parse_message(data)
        print('Received message type %s from %s' % (PGPMessage.Type.Name(msg.type), msg.name))
        handle = self.handlers.get(msg.type, self.default_handler)
        handle(msg, addr)

    def parse_message(self, data):
        size,pos = decoder._DecodeVarint(data, 0)
        msg = PGPMessage()
        msg.ParseFromString(data[pos : pos+size])
        return msg

    def send(self, msg, addr):
        # serialize message
        serialized_message = msg.SerializeToString()
        msg_length = len(serialized_message)
        msg_length = encoder._VarintBytes(msg_length)

        # the actual message sent is (len(msg) ++ msg)
        final_message = msg_length + serialized_message
        self.transport.sendto(msg_length + serialized_message, addr)


    # =================== HANDLERS ===================
    def default_handler(self, msg, addr):
        self.send_err('No handler found for this message type.', addr)


    def register_client(self, msg, addr):
        table = self.db['guardians']
        if table.find_one(name=msg.name):
            # Name not available -> send error message
            self.send_err('Name is not available.', addr)

        else:
            # create unique uuid
            new_uuid = self.create_uuid(table)

            # store that stuff
            table.insert(dict(name=msg.name, uuid=new_uuid, ip=addr[0], port=addr[1]))

            # build response
            self.send_ack(new_uuid, addr, name=msg.name)


    def deregister_client(self, msg, addr):
        table = self.db['guardians']
        if table.find_one(uuid=msg.goodbye.uuid):
            # delete from table
            table.delete(uuid=msg.goodbye.uuid)

            # respond with ack
            self.send_ack(msg.goodbye.uuid, addr)

        else:
            # user not found -> send error
            self.send_err('User with this uuid has not been found.', addr)


    def handle_ping(self, msg, addr):
        table = self.db['guardians']

        # find guardian in table
        guardian = table.find_one(uuid=msg.ping.uuid)

        # a response will be sent anyway
        if guardian:
            if self.guardian_changed(guardian, msg, addr):
                self.update_guardian(table, guardian, msg, addr)

            # send ack to client
            self.send_ack(msg.ping.uuid, addr, name=msg.name)

        else:
            # user not found -> response is error message
            self.send_err('User with this uuid has not been found', addr)
        

    def group_request(self, msg, addr):
        table = self.db['guardians']
        guardian = table.find_one(name=msg.groupReq.name)

        if guardian:
            # send ack to the request guardian
            self.send_ack(msg.ping.uuid, addr, name=msg.name)

            # build MergeReq message
            response = PGPMessage()
            response.type=PGPMessage.SG_MERGE_REQ
            response.mergeReq.name = msg.name
            response.mergeReq.ip = addr[0]
            response.mergeReq.port = addr[1]

            # send to the requested guardian
            self.send(response, (guardian['ip'], guardian['port']))

            print('Send merge request')

        else:
            self.send_err('User with this name has not been found.', addr)

    
    def group_info_forwarding(self, msg, addr):
        ip = msg.groupInfo.senderIP
        port = msg.groupInfo.senderPort
        self.send(msg, ip, addr)
        


    # ============== helpers =================
    def send_ack(self, uuid, addr, name=None):
        print('Sent message to %s:%d, message_type=ack' % (addr[0], addr[1]))
        # build ack
        response = PGPMessage()

        response.type = PGPMessage.SG_ACK
        response.ack.uuid = uuid
        response.ack.ip = addr[0]
        response.ack.port = addr[1]
        if name:
            response.ack.name = name

        # actually send it
        self.send(response, addr)

    def send_err(self, error_message, addr):
        print('Sent message to %s:%d, message_type=error, message: %s' % (addr[0], addr[1], error_message))
        # build protobuf
        response = PGPMessage()
        response.type=PGPMessage.SG_ERR
        response.error.error = error_message

        # send it
        self.send(response, addr)


    def create_uuid(self, table):
        # create new uuids until we have one that doesen't exist yet
        unique_uuid = False
        while not unique_uuid:
            new_uuid = str(uuid.uuid4())
            if not table.find_one(uuid=new_uuid):
                unique_uuid = True

        return new_uuid


    def guardian_changed(self, guardian, msg, addr):
        name_changed = guardian['name'] == msg.name
        ip_changed = guardian['ip'] == addr[0]
        port_changed = guardian['port'] = addr[1]

        return name_changed or ip_changed or port_changed

    def update_guardian(self, table, guardian, msg, addr):
        guardian['name'] = msg.name
        guardian['ip'] = addr[0]
        guardian['port'] = addr[1]
        table.update(guardian, ['uuid'])

