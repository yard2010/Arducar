#!/usr/bin/python

import SocketServer
import threading
import socket
import sys

class ServerHandler(SocketServer.BaseRequestHandler):
    def handle(self):
        while 1:
            data = self.request.recv(4096).strip()
            print data


class ThreadedCarServer(SocketServer.TCPServer, SocketServer.ThreadingMixIn):
    def __init__(self, server_address, server_handler, car_address, name=None):
        SocketServer.TCPServer.__init__(self, server_address, server_handler)
        self.__create_car_socket(car_address)

    def __create_car_socket(self, car_address):
        self.carSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.carSocket.connect(car_address)


if __name__ == "__main__":
    SERVER_HOST = ""
    SERVER_PORT = 9080

    CAR_HOST = sys.argv[1]
    CAR_PORT = 9081


    # Run again if client is disconnected
    while True:
        try:
            server = ThreadedCarServer((SERVER_HOST, SERVER_PORT), ServerHandler, (CAR_HOST, CAR_PORT))

            server_thread = threading.Thread(target=server.serve_forever(0.005))
            server_thread.daemon = True
            server_thread.start()

            print "Started Server on thread: {} on ip: {}".format(server_thread.name, server.server_address)
        except Exception as e:
            print e.message

