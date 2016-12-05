# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: msg.proto

import sys
_b=sys.version_info[0]<3 and (lambda x:x) or (lambda x:x.encode('latin1'))
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
from google.protobuf import descriptor_pb2
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor.FileDescriptor(
  name='msg.proto',
  package='penguard',
  serialized_pb=_b('\n\tmsg.proto\x12\x08penguard\"\xbc\x04\n\nPGPMessage\x12\'\n\x04type\x18\x01 \x02(\x0e\x32\x19.penguard.PGPMessage.Type\x12\x0c\n\x04name\x18\x02 \x01(\t\x12\x1c\n\x03\x61\x63k\x18\x03 \x01(\x0b\x32\r.penguard.AckH\x00\x12 \n\x05\x65rror\x18\x04 \x01(\x0b\x32\x0f.penguard.ErrorH\x00\x12\x1e\n\x04ping\x18\x05 \x01(\x0b\x32\x0e.penguard.PingH\x00\x12&\n\x08mergeReq\x18\x06 \x01(\x0b\x32\x12.penguard.MergeReqH\x00\x12&\n\x08groupReq\x18\x07 \x01(\x0b\x32\x12.penguard.GroupReqH\x00\x12 \n\x05group\x18\x08 \x01(\x0b\x32\x0f.penguard.GroupH\x00\x12 \n\x05seqNo\x18\t \x01(\x0b\x32\x0f.penguard.SeqNoH\x00\"\xf7\x01\n\x04Type\x12\n\n\x06SG_ACK\x10\x00\x12\n\n\x06SG_ERR\x10\x01\x12\x10\n\x0cSG_MERGE_REQ\x10\x02\x12\x0f\n\x0bGS_REGISTER\x10\x03\x12\x11\n\rGS_DEREGISTER\x10\x04\x12\x0b\n\x07GS_PING\x10\x05\x12\x10\n\x0cGS_GROUP_REQ\x10\x06\x12\x14\n\x10GG_STATUS_UPDATE\x10\x07\x12\n\n\x06GG_ACK\x10\x08\x12\x11\n\rGG_GRP_CHANGE\x10\t\x12\r\n\tGG_COMMIT\x10\n\x12\x0c\n\x08GG_ABORT\x10\x0b\x12\x0f\n\x0bGG_VOTE_YES\x10\x0c\x12\x0e\n\nGG_VOTE_NO\x10\r\x12\x0f\n\x0bGG_GRP_INFO\x10\x0e\x42\t\n\x07\x63ontent\";\n\x03\x41\x63k\x12\x0c\n\x04uuid\x18\x01 \x02(\t\x12\x0c\n\x04name\x18\x02 \x01(\t\x12\n\n\x02ip\x18\x03 \x02(\t\x12\x0c\n\x04port\x18\x04 \x02(\x05\"\x16\n\x05\x45rror\x12\r\n\x05\x65rror\x18\x01 \x02(\t\"\x14\n\x04Ping\x12\x0c\n\x04uuid\x18\x01 \x02(\t\"\x16\n\x05SeqNo\x12\r\n\x05seqno\x18\x01 \x02(\x05\"2\n\x08MergeReq\x12\x0c\n\x04name\x18\x01 \x02(\t\x12\n\n\x02ip\x18\x02 \x02(\t\x12\x0c\n\x04port\x18\x03 \x02(\x05\"\x18\n\x08GroupReq\x12\x0c\n\x04name\x18\x01 \x02(\t\"5\n\nPGPPenguin\x12\x0c\n\x04name\x18\x01 \x02(\t\x12\x0b\n\x03mac\x18\x02 \x02(\t\x12\x0c\n\x04seen\x18\x03 \x01(\x08\"5\n\x0bPGPGuardian\x12\x0c\n\x04name\x18\x01 \x01(\t\x12\n\n\x02ip\x18\x02 \x01(\t\x12\x0c\n\x04port\x18\x03 \x01(\x05\"i\n\x05Group\x12\x0e\n\x06seq_no\x18\x01 \x02(\x05\x12(\n\tguardians\x18\x02 \x03(\x0b\x32\x15.penguard.PGPGuardian\x12&\n\x08penguins\x18\x03 \x03(\x0b\x32\x14.penguard.PGPPenguinB3\n\"verteiltesysteme.penguard.protobufB\rPenguardProto')
)
_sym_db.RegisterFileDescriptor(DESCRIPTOR)



_PGPMESSAGE_TYPE = _descriptor.EnumDescriptor(
  name='Type',
  full_name='penguard.PGPMessage.Type',
  filename=None,
  file=DESCRIPTOR,
  values=[
    _descriptor.EnumValueDescriptor(
      name='SG_ACK', index=0, number=0,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='SG_ERR', index=1, number=1,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='SG_MERGE_REQ', index=2, number=2,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='GS_REGISTER', index=3, number=3,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='GS_DEREGISTER', index=4, number=4,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='GS_PING', index=5, number=5,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='GS_GROUP_REQ', index=6, number=6,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='GG_STATUS_UPDATE', index=7, number=7,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='GG_ACK', index=8, number=8,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='GG_GRP_CHANGE', index=9, number=9,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='GG_COMMIT', index=10, number=10,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='GG_ABORT', index=11, number=11,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='GG_VOTE_YES', index=12, number=12,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='GG_VOTE_NO', index=13, number=13,
      options=None,
      type=None),
    _descriptor.EnumValueDescriptor(
      name='GG_GRP_INFO', index=14, number=14,
      options=None,
      type=None),
  ],
  containing_type=None,
  options=None,
  serialized_start=338,
  serialized_end=585,
)
_sym_db.RegisterEnumDescriptor(_PGPMESSAGE_TYPE)


_PGPMESSAGE = _descriptor.Descriptor(
  name='PGPMessage',
  full_name='penguard.PGPMessage',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='type', full_name='penguard.PGPMessage.type', index=0,
      number=1, type=14, cpp_type=8, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='name', full_name='penguard.PGPMessage.name', index=1,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='ack', full_name='penguard.PGPMessage.ack', index=2,
      number=3, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='error', full_name='penguard.PGPMessage.error', index=3,
      number=4, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='ping', full_name='penguard.PGPMessage.ping', index=4,
      number=5, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='mergeReq', full_name='penguard.PGPMessage.mergeReq', index=5,
      number=6, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='groupReq', full_name='penguard.PGPMessage.groupReq', index=6,
      number=7, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='group', full_name='penguard.PGPMessage.group', index=7,
      number=8, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='seqNo', full_name='penguard.PGPMessage.seqNo', index=8,
      number=9, type=11, cpp_type=10, label=1,
      has_default_value=False, default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
    _PGPMESSAGE_TYPE,
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
    _descriptor.OneofDescriptor(
      name='content', full_name='penguard.PGPMessage.content',
      index=0, containing_type=None, fields=[]),
  ],
  serialized_start=24,
  serialized_end=596,
)


_ACK = _descriptor.Descriptor(
  name='Ack',
  full_name='penguard.Ack',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='uuid', full_name='penguard.Ack.uuid', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='name', full_name='penguard.Ack.name', index=1,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='ip', full_name='penguard.Ack.ip', index=2,
      number=3, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='port', full_name='penguard.Ack.port', index=3,
      number=4, type=5, cpp_type=1, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=598,
  serialized_end=657,
)


_ERROR = _descriptor.Descriptor(
  name='Error',
  full_name='penguard.Error',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='error', full_name='penguard.Error.error', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=659,
  serialized_end=681,
)


_PING = _descriptor.Descriptor(
  name='Ping',
  full_name='penguard.Ping',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='uuid', full_name='penguard.Ping.uuid', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=683,
  serialized_end=703,
)


_SEQNO = _descriptor.Descriptor(
  name='SeqNo',
  full_name='penguard.SeqNo',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='seqno', full_name='penguard.SeqNo.seqno', index=0,
      number=1, type=5, cpp_type=1, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=705,
  serialized_end=727,
)


_MERGEREQ = _descriptor.Descriptor(
  name='MergeReq',
  full_name='penguard.MergeReq',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='name', full_name='penguard.MergeReq.name', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='ip', full_name='penguard.MergeReq.ip', index=1,
      number=2, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='port', full_name='penguard.MergeReq.port', index=2,
      number=3, type=5, cpp_type=1, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=729,
  serialized_end=779,
)


_GROUPREQ = _descriptor.Descriptor(
  name='GroupReq',
  full_name='penguard.GroupReq',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='name', full_name='penguard.GroupReq.name', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=781,
  serialized_end=805,
)


_PGPPENGUIN = _descriptor.Descriptor(
  name='PGPPenguin',
  full_name='penguard.PGPPenguin',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='name', full_name='penguard.PGPPenguin.name', index=0,
      number=1, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='mac', full_name='penguard.PGPPenguin.mac', index=1,
      number=2, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='seen', full_name='penguard.PGPPenguin.seen', index=2,
      number=3, type=8, cpp_type=7, label=1,
      has_default_value=False, default_value=False,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=807,
  serialized_end=860,
)


_PGPGUARDIAN = _descriptor.Descriptor(
  name='PGPGuardian',
  full_name='penguard.PGPGuardian',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='name', full_name='penguard.PGPGuardian.name', index=0,
      number=1, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='ip', full_name='penguard.PGPGuardian.ip', index=1,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=_b("").decode('utf-8'),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='port', full_name='penguard.PGPGuardian.port', index=2,
      number=3, type=5, cpp_type=1, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=862,
  serialized_end=915,
)


_GROUP = _descriptor.Descriptor(
  name='Group',
  full_name='penguard.Group',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    _descriptor.FieldDescriptor(
      name='seq_no', full_name='penguard.Group.seq_no', index=0,
      number=1, type=5, cpp_type=1, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='guardians', full_name='penguard.Group.guardians', index=1,
      number=2, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    _descriptor.FieldDescriptor(
      name='penguins', full_name='penguard.Group.penguins', index=2,
      number=3, type=11, cpp_type=10, label=3,
      has_default_value=False, default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],
  enum_types=[
  ],
  options=None,
  is_extendable=False,
  extension_ranges=[],
  oneofs=[
  ],
  serialized_start=917,
  serialized_end=1022,
)

_PGPMESSAGE.fields_by_name['type'].enum_type = _PGPMESSAGE_TYPE
_PGPMESSAGE.fields_by_name['ack'].message_type = _ACK
_PGPMESSAGE.fields_by_name['error'].message_type = _ERROR
_PGPMESSAGE.fields_by_name['ping'].message_type = _PING
_PGPMESSAGE.fields_by_name['mergeReq'].message_type = _MERGEREQ
_PGPMESSAGE.fields_by_name['groupReq'].message_type = _GROUPREQ
_PGPMESSAGE.fields_by_name['group'].message_type = _GROUP
_PGPMESSAGE.fields_by_name['seqNo'].message_type = _SEQNO
_PGPMESSAGE_TYPE.containing_type = _PGPMESSAGE
_PGPMESSAGE.oneofs_by_name['content'].fields.append(
  _PGPMESSAGE.fields_by_name['ack'])
_PGPMESSAGE.fields_by_name['ack'].containing_oneof = _PGPMESSAGE.oneofs_by_name['content']
_PGPMESSAGE.oneofs_by_name['content'].fields.append(
  _PGPMESSAGE.fields_by_name['error'])
_PGPMESSAGE.fields_by_name['error'].containing_oneof = _PGPMESSAGE.oneofs_by_name['content']
_PGPMESSAGE.oneofs_by_name['content'].fields.append(
  _PGPMESSAGE.fields_by_name['ping'])
_PGPMESSAGE.fields_by_name['ping'].containing_oneof = _PGPMESSAGE.oneofs_by_name['content']
_PGPMESSAGE.oneofs_by_name['content'].fields.append(
  _PGPMESSAGE.fields_by_name['mergeReq'])
_PGPMESSAGE.fields_by_name['mergeReq'].containing_oneof = _PGPMESSAGE.oneofs_by_name['content']
_PGPMESSAGE.oneofs_by_name['content'].fields.append(
  _PGPMESSAGE.fields_by_name['groupReq'])
_PGPMESSAGE.fields_by_name['groupReq'].containing_oneof = _PGPMESSAGE.oneofs_by_name['content']
_PGPMESSAGE.oneofs_by_name['content'].fields.append(
  _PGPMESSAGE.fields_by_name['group'])
_PGPMESSAGE.fields_by_name['group'].containing_oneof = _PGPMESSAGE.oneofs_by_name['content']
_PGPMESSAGE.oneofs_by_name['content'].fields.append(
  _PGPMESSAGE.fields_by_name['seqNo'])
_PGPMESSAGE.fields_by_name['seqNo'].containing_oneof = _PGPMESSAGE.oneofs_by_name['content']
_GROUP.fields_by_name['guardians'].message_type = _PGPGUARDIAN
_GROUP.fields_by_name['penguins'].message_type = _PGPPENGUIN
DESCRIPTOR.message_types_by_name['PGPMessage'] = _PGPMESSAGE
DESCRIPTOR.message_types_by_name['Ack'] = _ACK
DESCRIPTOR.message_types_by_name['Error'] = _ERROR
DESCRIPTOR.message_types_by_name['Ping'] = _PING
DESCRIPTOR.message_types_by_name['SeqNo'] = _SEQNO
DESCRIPTOR.message_types_by_name['MergeReq'] = _MERGEREQ
DESCRIPTOR.message_types_by_name['GroupReq'] = _GROUPREQ
DESCRIPTOR.message_types_by_name['PGPPenguin'] = _PGPPENGUIN
DESCRIPTOR.message_types_by_name['PGPGuardian'] = _PGPGUARDIAN
DESCRIPTOR.message_types_by_name['Group'] = _GROUP

PGPMessage = _reflection.GeneratedProtocolMessageType('PGPMessage', (_message.Message,), dict(
  DESCRIPTOR = _PGPMESSAGE,
  __module__ = 'msg_pb2'
  # @@protoc_insertion_point(class_scope:penguard.PGPMessage)
  ))
_sym_db.RegisterMessage(PGPMessage)

Ack = _reflection.GeneratedProtocolMessageType('Ack', (_message.Message,), dict(
  DESCRIPTOR = _ACK,
  __module__ = 'msg_pb2'
  # @@protoc_insertion_point(class_scope:penguard.Ack)
  ))
_sym_db.RegisterMessage(Ack)

Error = _reflection.GeneratedProtocolMessageType('Error', (_message.Message,), dict(
  DESCRIPTOR = _ERROR,
  __module__ = 'msg_pb2'
  # @@protoc_insertion_point(class_scope:penguard.Error)
  ))
_sym_db.RegisterMessage(Error)

Ping = _reflection.GeneratedProtocolMessageType('Ping', (_message.Message,), dict(
  DESCRIPTOR = _PING,
  __module__ = 'msg_pb2'
  # @@protoc_insertion_point(class_scope:penguard.Ping)
  ))
_sym_db.RegisterMessage(Ping)

SeqNo = _reflection.GeneratedProtocolMessageType('SeqNo', (_message.Message,), dict(
  DESCRIPTOR = _SEQNO,
  __module__ = 'msg_pb2'
  # @@protoc_insertion_point(class_scope:penguard.SeqNo)
  ))
_sym_db.RegisterMessage(SeqNo)

MergeReq = _reflection.GeneratedProtocolMessageType('MergeReq', (_message.Message,), dict(
  DESCRIPTOR = _MERGEREQ,
  __module__ = 'msg_pb2'
  # @@protoc_insertion_point(class_scope:penguard.MergeReq)
  ))
_sym_db.RegisterMessage(MergeReq)

GroupReq = _reflection.GeneratedProtocolMessageType('GroupReq', (_message.Message,), dict(
  DESCRIPTOR = _GROUPREQ,
  __module__ = 'msg_pb2'
  # @@protoc_insertion_point(class_scope:penguard.GroupReq)
  ))
_sym_db.RegisterMessage(GroupReq)

PGPPenguin = _reflection.GeneratedProtocolMessageType('PGPPenguin', (_message.Message,), dict(
  DESCRIPTOR = _PGPPENGUIN,
  __module__ = 'msg_pb2'
  # @@protoc_insertion_point(class_scope:penguard.PGPPenguin)
  ))
_sym_db.RegisterMessage(PGPPenguin)

PGPGuardian = _reflection.GeneratedProtocolMessageType('PGPGuardian', (_message.Message,), dict(
  DESCRIPTOR = _PGPGUARDIAN,
  __module__ = 'msg_pb2'
  # @@protoc_insertion_point(class_scope:penguard.PGPGuardian)
  ))
_sym_db.RegisterMessage(PGPGuardian)

Group = _reflection.GeneratedProtocolMessageType('Group', (_message.Message,), dict(
  DESCRIPTOR = _GROUP,
  __module__ = 'msg_pb2'
  # @@protoc_insertion_point(class_scope:penguard.Group)
  ))
_sym_db.RegisterMessage(Group)


DESCRIPTOR.has_options = True
DESCRIPTOR._options = _descriptor._ParseOptions(descriptor_pb2.FileOptions(), _b('\n\"verteiltesysteme.penguard.protobufB\rPenguardProto'))
# @@protoc_insertion_point(module_scope)
