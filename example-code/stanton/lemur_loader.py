#!/usr/bin/env python
#
# Lemur JZML loader module for Python
# Copyright (c) 2007 Matthew Stanton
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
#
# Contact Matt Stanton <luchak@gmail.com> with questions, comments, bug
# reports, patches, etc.

import socket
import struct
import sys

_MAX_BLOCK_SIZE = 1448
_LEMUR_PORT = 8002

class LemurLoader(object):
  """A class for sending JZML data to a Lemur."""

  def __init__(self, host):
    """Initializes a LemurLoader to talk to a particular Lemur."""
    self.__host = host

  def __Pad(self, buf):
    """Returns a copy of buf padded with NULs to make its length a multiple of
    4 bytes."""

    if len(buf) % 4 == 0:
      return buf

    pad_buf = ""
    for i in range(4 - (len(buf) % 4)):
      pad_buf += "\x00"

    return buf + pad_buf

  def __FormatInt(self, int):
    """Returns a string containing a 32-bit integer in network byte order."""
    return struct.pack("!i", int)

  def Send(self, buf):
    """Sends the text in buf to the Lemur.  Assumes that buf contains JZML
    data (or at least something that won't make your Lemur too unhappy).

    The communication protocol is roughly as follows:  a message consists of
    one header packet and a series of data packets.  These packets contain
    values of both integer and string types.  Integer values are 32 bits, in
    network byte order.  Strings are padded with NULs to a multiple of 4 bytes.

    The header packet contains the following values, in order:
    - An integer containing the total number of bytes we will send in all
      packets, except for this integer.  This figure includes padding.
    - A string "/jzml".
    - A string ",b".  (The OSC type tag.)
    - An integer containing the number of bytes in the JZML data we are
      sending.  This figure does not include padding.

    Data packets are capped at _MAX_BLOCK_SIZE bytes each.  They contain our
    JZML data, and the last may have a couple of bytes of NUL padding at the
    end.
    """
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect((self.__host, _LEMUR_PORT))

    padded_buf = self.__Pad(buf) 

    # Send header packet.
    # We get our magic constant of 16 from 20 bytes of header data (count 'em!
    # :) ), minus the 4 bytes that give the total length, which don't get
    # included in the total.
    s.send("%s%s%s%s" % (self.__FormatInt(16 + len(padded_buf)),
                         self.__Pad("/jzml"),
                         self.__Pad(",b"),
                         self.__FormatInt(len(buf))))

    # Send data packets.
    bytes_sent = 0
    while (bytes_sent < len(padded_buf)):
      bytes_to_send = len(padded_buf) - bytes_sent
      assert bytes_to_send % 4 == 0
      bytes_this_block = min(_MAX_BLOCK_SIZE, bytes_to_send)
      s.send(padded_buf[bytes_sent:(bytes_sent + bytes_this_block)])
      bytes_sent += bytes_this_block

    # Clean up and return.
    s.close()

    return bytes_sent


def main(argv):
  """Reads a JZML file, mangles the contents a little, and sends the results to
  the Lemur."""

  if len(argv) < 3:
    print ("usage: %s lemur_ip file_name" % argv[0])
    sys.exit(1)

  lemur_loader = LemurLoader(argv[1])

  # Read our input data.
  input_file = open(argv[2])
  jzml_data = input_file.read().strip()
  input_file.close()

  # Mangle the input data to make it Lemur-friendly.  I really don't know what
  # all this stuff is for, or why we don't send end tags, or any of that.  I'm
  # just trying to mimic what JazzEditor does.
  #
  # Actually, I know what one of these tags does:  <RESET/> clears the screen.
  # The rest are still a mystery.
  if (jzml_data[:6] == "<JZML>"):
    jzml_data = "<RESET/><TARGET request=\"1\"/><SYNCHRO mode=\"0\"/>" + \
               jzml_data[6:]
  if (jzml_data[-7:] == "</JZML>"):
    jzml_data = jzml_data[:-7]

  bytes_sent = lemur_loader.Send(jzml_data)

  print ("%s bytes sent" % bytes_sent)

if __name__ == "__main__":
  main(sys.argv)
