#!/usr/bin/python

import SocketServer
import threading
import sys
import logging
import socket
from pubsub import pub


CONTROLLER_SERVER_HOST = ''
CONTROLLER_SERVER_PORT = 9080
CAR_SERVER_PORT = 9081
TIMEOUT_VAL = 5


class ControllerClientHandler(SocketServer.BaseRequestHandler):
    def handle(self):
        # Setting timeout so will not get stuck if no input is given.
        # This is so that when one socket dies, all others will too.
        # Timeout value is in seconds
        self.request.settimeout(TIMEOUT_VAL)

        is_connected = True

        # Run as long as data is good
        while is_connected:
            try:
                data_received = self.request.recv(4096).strip()
            except (socket.timeout, socket.error, Exception) as ex:
                logging.debug("ControllerClientHandler: " + str(ex))

            is_connected = len(data_received) > 0

            pub.sendMessage('controller:changed', data=data_received)


class ThreadedControllerServer(SocketServer.TCPServer, SocketServer.ThreadingMixIn):
    pass


class CarClientHandler(object):

    def __init__(self):
        # UDP Connection to internet
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.sock.bind((socket.gethostbyname(socket.gethostname()), CAR_SERVER_PORT))
        self.car_addr = ""

        # Receive initial message from client to get address
        self.car_addr = self.sock.recvfrom(1024)[1]

    def handle(self):
        pub.subscribe(self.controller_listener, 'controller:changed')

    def controller_listener(self, data):
        logging.debug('CarClientHandler: controller:changed event fired with: ')
        logging.debug('data = {0}'.format(data))
        try:
            self.sock.sendto(data, (self.car_addr, CAR_SERVER_PORT))
        except:
            logging.debug("Connection forcibly closed")


class ThreadedCarServer(SocketServer.TCPServer, SocketServer.ThreadingMixIn):
    pass


def handle_controller_connection():
    while True:
        controller_server = ThreadedControllerServer((CONTROLLER_SERVER_HOST, CONTROLLER_SERVER_PORT),
                                                     ControllerClientHandler)

        controller_thread = threading.Thread(target=controller_server.serve_forever, args=(0.005,))
        controller_thread.start()

        logging.debug("Started Controller Server on thread: {} on ip: {}".format(controller_thread.name,
                                                                                 controller_server.server_address))

        # Wait till controller thread dies (probably disconnection or something)
        controller_thread.join()

if __name__ == "__main__":
    # Setup logger
    logging.basicConfig(filename='log.log', filemode='w', level=logging.DEBUG)
    ch = logging.StreamHandler(sys.stdout)
    logging.getLogger().addHandler(ch)

    try:
        # Start threads each handling their own client. Only finish main thread if those threads die
        controller_connection_thread = threading.Thread(target=handle_controller_connection)
        controller_connection_thread.start()

        car_server = CarClientHandler()
        car_server.handle()

        # Wait for handling threads to die (they shouldn't unless script is terminated)
        controller_connection_thread.join()

    except Exception as e:
        logging.debug(e)
