"""
:synopsis: Google-generated code for parsing protocol buffers.

This docstring is the only modification we have made to the generated code!

"""

# Generated by the protocol buffer compiler.  DO NOT EDIT!

from google.protobuf import descriptor
from google.protobuf import message
from google.protobuf import reflection
from google.protobuf import descriptor_pb2
# @@protoc_insertion_point(imports)


DESCRIPTOR = descriptor.FileDescriptor(
  name='vOutput.proto',
  package='',
  serialized_pb='\n\rvOutput.proto\"F\n\x05Value\x12\x13\n\x0b\x64oubleValue\x18\x01 \x01(\x01\x12\x13\n\x0bstringValue\x18\x02 \x01(\t\x12\x13\n\x0b\x63ustomValue\x18\x03 \x01(\x0c\"C\n\x08Snapshot\x12\x11\n\ttimestamp\x18\x01 \x02(\x03\x12\r\n\x05\x61\x63tor\x18\x02 \x02(\t\x12\x15\n\x05value\x18\x03 \x02(\x0b\x32\x06.Value\"(\n\tTimeSerie\x12\x1b\n\x08snapshot\x18\x01 \x03(\x0b\x32\t.Snapshot')




_VALUE = descriptor.Descriptor(
  name='Value',
  full_name='Value',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='doubleValue', full_name='Value.doubleValue', index=0,
      number=1, type=1, cpp_type=5, label=1,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='stringValue', full_name='Value.stringValue', index=1,
      number=2, type=9, cpp_type=9, label=1,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='customValue', full_name='Value.customValue', index=2,
      number=3, type=12, cpp_type=9, label=1,
      has_default_value=False, default_value="",
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
  serialized_start=17,
  serialized_end=87,
)


_SNAPSHOT = descriptor.Descriptor(
  name='Snapshot',
  full_name='Snapshot',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='timestamp', full_name='Snapshot.timestamp', index=0,
      number=1, type=3, cpp_type=2, label=2,
      has_default_value=False, default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='actor', full_name='Snapshot.actor', index=1,
      number=2, type=9, cpp_type=9, label=2,
      has_default_value=False, default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='value', full_name='Snapshot.value', index=2,
      number=3, type=11, cpp_type=10, label=2,
      has_default_value=False, default_value=None,
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
  serialized_start=89,
  serialized_end=156,
)


_TIMESERIE = descriptor.Descriptor(
  name='TimeSerie',
  full_name='TimeSerie',
  filename=None,
  file=DESCRIPTOR,
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='snapshot', full_name='TimeSerie.snapshot', index=0,
      number=1, type=11, cpp_type=10, label=3,
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
  serialized_start=158,
  serialized_end=198,
)


_SNAPSHOT.fields_by_name['value'].message_type = _VALUE
_TIMESERIE.fields_by_name['snapshot'].message_type = _SNAPSHOT

class Value(message.Message):
  __metaclass__ = reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _VALUE
  
  # @@protoc_insertion_point(class_scope:Value)

class Snapshot(message.Message):
  __metaclass__ = reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _SNAPSHOT
  
  # @@protoc_insertion_point(class_scope:Snapshot)

class TimeSerie(message.Message):
  __metaclass__ = reflection.GeneratedProtocolMessageType
  DESCRIPTOR = _TIMESERIE
  
  # @@protoc_insertion_point(class_scope:TimeSerie)

# @@protoc_insertion_point(module_scope)
