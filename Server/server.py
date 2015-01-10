#!/usr/bin/python

import socket

HOST = socket.gethostname()
PORT = 9080
BUFFER_LENGTH = 4096

my_socket = socket.socket()
my_socket.bind((HOST, PORT))

print "Welcome to my lovely server!"

my_socket.listen(5)

client, address = my_socket.accept()
print "New client:", client, "From address:", address[0]

while True:
    data = client.recv(BUFFER_LENGTH)
    print str(data)  
