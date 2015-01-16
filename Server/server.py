#!/usr/bin/python

import SocketServer
import threading
import socket


class Event(object):
    pass


class Observable(object):
    def __init__(self):
        self.callbacks = []

    def subscribe(self, callback):
        self.callbacks.append(callback)

    def fire(self, **attrs):
        e = Event()
        e.source = self
        for k, v in attrs.iteritems():
            setattr(e, k, v)
        for fn in self.callbacks:
            fn(e)


class CarClientHandler(object):
    def __init__(self, car_address, observable):

        # Create the socket server for the car client
        self._create_car_socket(car_address)

        # Subscribe to controller events with appropriate callback method
        observable.subscribe(self._on_data_received)


    def _create_car_socket(self, car_address):
        self.carSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.carSocket.bind(car_address)
        self.carSocket.listen(1)
        self.car_conn, self.car_addr = self.carSocket.accept()
        print "Car client connected successfully."

    def _on_data_received(self, data):

        # If data is bad, close connection, otherwise pipe it to the car
        if data in ("", None):
            self.car_conn.close()
        else:
            self.car_conn.send(data)


class ControllerClientHandler(SocketServer.BaseRequestHandler, Observable):
    def handle(self):

        valid_data = True

        while valid_data:
            data = self.request.recv(4096)
            print "Received data from controller: {0}".format(data)
            self.fire(data=data)

            valid_data = self._validate_data(data)

    def _validate_data(self, data):
        """
        :param data: Data received from controller client
        :return: True if data was valid, false if not
        """
        if data in ("", None):
            self.finish()

            return False
        return True


class ThreadedCarServer(SocketServer.TCPServer, SocketServer.ThreadingMixIn):
    def __init__(self, server_address, server_handler, car_address, name=None):
        SocketServer.TCPServer.__init__(self, server_address, server_handler)
        CarClientHandler(car_address, self.RequestHandlerClass) # TODO Not working. Fix usage of class. Must be instance


if __name__ == "__main__":
    SERVER_HOST = ""
    SERVER_PORT = 9080

    CAR_HOST = ""
    CAR_PORT = 9081


    # Run again if client is disconnected
    while True:
        try:
            server = ThreadedCarServer((SERVER_HOST, SERVER_PORT), ControllerClientHandler, (CAR_HOST, CAR_PORT))

            server_thread = threading.Thread(target=server.serve_forever(0.005))
            #server_thread.daemon = True
            server_thread.start()

            print "Started Server on thread: {} on ip: {}".format(server_thread.name, server.server_address)
        except Exception as e:
            print e

