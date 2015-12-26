#!/usr/bin/python

import SocketServer
import threading
import sys
import logging
from pubsub import pub


CONTROLLER_SERVER_HOST = ''
CONTROLLER_SERVER_PORT = 9080
CAR_SERVER_HOST = ''
CAR_SERVER_PORT = 9081


class ControllerClientHandler(SocketServer.BaseRequestHandler):
    def handle(self):
        while True:
            data_received = self.request.recv(4096).strip()
            pub.sendMessage('controller:changed', data=data_received)


class ThreadedControllerServer(SocketServer.TCPServer, SocketServer.ThreadingMixIn):
    pass


class CarClientHandler(SocketServer.BaseRequestHandler):
    def handle(self):
        pub.subscribe(self.controller_listener, 'controller:changed')
        is_connected = True

        while is_connected:
            try:
                logging.debug("Car Says: {0}".format(self.request.recv(4096).strip()))
            except Exception:
                logging.debug("Connection forcibly closed")
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

    # Run again if client is disconnected
    while True:
        try:
            controller_server = ThreadedControllerServer((CONTROLLER_SERVER_HOST, CONTROLLER_SERVER_PORT),
                                                         ControllerClientHandler)

            controller_thread = threading.Thread(target=controller_server.serve_forever, args=(0.005,))
            controller_thread.start()

            logging.debug("Started Controller Server on thread: {} on ip: {}".format(controller_thread.name,
                                                                                     controller_server.server_address))

            car_server = ThreadedCarServer((CAR_SERVER_HOST, CAR_SERVER_PORT),
                                           CarClientHandler)

            car_thread = threading.Thread(target=car_server.serve_forever, args=(0.005,))
            car_thread.start()

            logging.debug("Started Car Server on thread: {} on ip: {}".format(car_thread.name,
                                                                              car_server.server_address))

            controller_thread.join()
            car_thread.join()

        except Exception as e:
            logging.debug(e)
