#!/usr/bin/python

import socket

my_socket = socket.socket()
host = socket.gethostname()
port = 9080
my_socket.bind((host, port))

my_socket.listen(5)
while True:
    client, address = my_socket.accept()
    print "Got data from client:", address
    client.send("Thanks for you visit")
    client.close()
