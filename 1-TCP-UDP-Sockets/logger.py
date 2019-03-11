#!/usr/bin/env python

import datetime
import socket
import struct
import sys

file = 'log.txt'
if len(sys.argv) > 1:
    file = sys.argv[1]

IP_ADDR = '224.1.1.1'
PORT = 8888

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
sock.bind((IP_ADDR, PORT))
mreq = struct.pack("4sl", socket.inet_aton(IP_ADDR), socket.INADDR_ANY)

sock.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)

while True:
    data, addr = sock.recvfrom(1024)
    buf = "%s : %s" % (str(datetime.datetime.now())[:-7], data.decode('utf-8'))
    print(buf)
    with open(file, "a+") as open_file:
        open_file.write(buf)
