#!/usr/bin/python

import SocketServer
import threading
import sys
import logging
import socket
from pubsub import pub


CONTROLLER_SERVER_HOST = ''
CONTROLLER_SERVER_PORT = 9080
CAR_SERVER_HOST = ''
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


class CarClientHandler(SocketServer.BaseRequestHandler):
    def handle(self):
        # Setting timeout so will not get stuck if no input is given.
        # This is so that when one socket dies, all others will too.
        # Timeout value is in seconds
        self.request.settimeout(TIMEOUT_VAL)

        pub.subscribe(self.controller_listener, 'controller:changed')
        is_connected = True

        # Receive input from car (should be the video stream
        # TODO This loop should be moved out to another thread dedicated for video input from car
        while is_connected:
            try:
                data_received = self.request.recv(4096).strip()
                logging.debug("Car Says: {0}".format(data_received))
            except (socket.timeout, socket.error, Exception) as ex:
                logging.debug("CarClientHandler: " + str(ex))
                is_connected = False

    def controller_listener(self, data):
        logging.debug('CarClientHandler: controller:changed event fired with: ')
        logging.debug('data = {0}'.format(data))
        try:
            self.request.send(data)
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


def handle_car_connection():
    while True:
        car_server = ThreadedCarServer((CAR_SERVER_HOST, CAR_SERVER_PORT),
                                       CarClientHandler)

        car_thread = threading.Thread(target=car_server.serve_forever, args=(0.005,))
        car_thread.start()

        logging.debug("Started Car Server on thread: {} on ip: {}".format(car_thread.name,
                                                                          car_server.server_address))

        # Wait till car thread dies (probably disconnection or something)
        car_thread.join()

if __name__ == "__main__":
    # Setup logger
    logging.basicConfig(filename='log.log', level=logging.DEBUG)
    ch = logging.StreamHandler(sys.stdout)
    logging.getLogger().addHandler(ch)

    # PROBABLY NOT NECESSARY!
    # Set controller server host ip (first attribute)
    if len(sys.argv) >= 2:
        CONTROLLER_SERVER_HOST = sys.argv[1]

    # Set car server host ip (second attribute)
    if len(sys.argv) >= 3:
        CONTROLLER_SERVER_HOST = sys.argv[2]

    try:
        # Start threads each handling their own client. Only finish main thread if those threads die
        controller_connection_thread = threading.Thread(target=handle_controller_connection)
        controller_connection_thread.start()

        car_connection_thread = threading.Thread(target=handle_car_connection)
        car_connection_thread.start()

        # Wait for handling threads to die (they shouldn't unless script is terminated)
        controller_connection_thread.join()
        car_connection_thread.join()

    except Exception as e:
        logging.debug(e)
