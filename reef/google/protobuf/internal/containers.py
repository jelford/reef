# Protocol Buffers - Google's data interchange format
# Copyright 2008 Google Inc.  All rights reserved.
# http://code.google.com/p/protobuf/
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are
# met:
#
#     * Redistributions of source code must retain the above copyright
# notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above
# copyright notice, this list of conditions and the following disclaimer
# in the documentation and/or other materials provided with the
# distribution.
#     * Neither the name of Google Inc. nor the names of its
# contributors may be used to endorse or promote products derived from
# this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
# LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
# A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
# OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
# SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
# LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
# THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

"""Contains container classes to represent different protocol buffer types.

This file defines container classes which represent categories of protocol
buffer field types which need extra maintenance. Currently these categories
are:
  - Repeated scalar fields - These are all repeated fields which aren't
    composite (e.g. they are of simple types like int32, string, etc).
  - Repeated composite fields - Repeated fields which are composite. This
    includes groups and nested messages.
"""

__author__ = 'petar@google.com (Petar Petrov)'


class BaseContainer(object):

  """Base container class."""

  # Minimizes memory usage and disallows assignment to other attributes.
  __slots__ = ['_message_listener', '_values']

  def __init__(self, message_listener):
    """
    Args:
      message_listener: A MessageListener implementation.
        The RepeatedScalarFieldContainer will call this object's
        TransitionToNonempty() method when it transitions from being empty to
        being nonempty.
    """
    self._message_listener = message_listener
    self._values = []

  def __getitem__(self, key):
    """Retrieves item by the specified key."""
    return self._values[key]

  def __len__(self):
    """Returns the number of elements in the container."""
    return len(self._values)

  def __ne__(self, other):
    """Checks if another instance isn't equal to this one."""
    # The concrete classes should define __eq__.
    return not self == other


class RepeatedScalarFieldContainer(BaseContainer):

  """Simple, type-checked, list-like container for holding repeated scalars."""

  # Disallows assignment to other attributes.
  __slots__ = ['_type_checker']

  def __init__(self, message_listener, type_checker):
    """
    Args:
      message_listener: A MessageListener implementation.
        The RepeatedScalarFieldContainer will call this object's
        TransitionToNonempty() method when it transitions from being empty to
        being nonempty.
      type_checker: A type_checkers.ValueChecker instance to run on elements
        inserted into this container.
    """
    super(RepeatedScalarFieldContainer, self).__init__(message_listener)
    self._type_checker = type_checker

  def append(self, elem):
    """Appends a scalar to the list. Similar to list.append()."""
    self._type_checker.CheckValue(elem)
    self._values.append(elem)
    self._message_listener.ByteSizeDirty()
    if len(self._values) == 1:
      self._message_listener.TransitionToNonempty()

  def remove(self, elem):
    """Removes a scalar from the list. Similar to list.remove()."""
    self._values.remove(elem)
    self._message_listener.ByteSizeDirty()

  def __setitem__(self, key, value):
    """Sets the item on the specified position."""
    # No need to call TransitionToNonempty(), since if we're able to
    # set the element at this index, we were already nonempty before
    # this method was called.
    self._message_listener.ByteSizeDirty()
    self._type_checker.CheckValue(value)
    self._values[key] = value

  def __eq__(self, other):
    """Compares the current instance with another one."""
    if self is other:
      return True
    # Special case for the same type which should be common and fast.
    if isinstance(other, self.__class__):
      return other._values == self._values
    # We are presumably comparing against some other sequence type.
    return other == self._values


class RepeatedCompositeFieldContainer(BaseContainer):

  """Simple, list-like container for holding repeated composite fields."""

  # Disallows assignment to other attributes.
  __slots__ = ['_message_descriptor']

  def __init__(self, message_listener, message_descriptor):
    """
    Note that we pass in a descriptor instead of the generated directly,
    since at the time we construct a _RepeatedCompositeFieldContainer we
    haven't yet necessarily initialized the type that will be contained in the
    container.

    Args:
      message_listener: A MessageListener implementation.
        The RepeatedCompositeFieldContainer will call this object's
        TransitionToNonempty() method when it transitions from being empty to
        being nonempty.
      message_descriptor: A Descriptor instance describing the protocol type
        that should be present in this container.  We'll use the
        _concrete_class field of this descriptor when the client calls add().
    """
    super(RepeatedCompositeFieldContainer, self).__init__(message_listener)
    self._message_descriptor = message_descriptor

  def add(self):
    """Adds a new element to the list and returns it."""
    new_element = self._message_descriptor._concrete_class()
    new_element._SetListener(self._message_listener)
    self._values.append(new_element)
    self._message_listener.ByteSizeDirty()
    self._message_listener.TransitionToNonempty()
    return new_element

  def __delitem__(self, key):
    """Deletes the element on the specified position."""
    self._message_listener.ByteSizeDirty()
    del self._values[key]

  def __eq__(self, other):
    """Compares the current instance with another one."""
    if self is other:
      return True
    if not isinstance(other, self.__class__):
      raise TypeError('Can only compare repeated composite fields against '
                      'other repeated composite fields.')
    return self._values == other._values

  # TODO(robinson): Implement, document, and test slicing support.
