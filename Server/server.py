#!/usr/bin/python

# python SocketServer
# import threading
# import socket


import SocketServer
import threading
from pubsub import pub


CONTROLLER_SERVER_HOST = ''
CONTROLLER_SERVER_PORT = 9080
CAR_SERVER_HOST = ''
CAR_SERVER_PORT = 9081


class ControllerClientHandler(SocketServer.BaseRequestHandler):
    def handle(self):
        while 1:
            data_recieved = self.request.recv(4096).strip()
            # print "Controller Says: {0}".format(data_recieved)
            pub.sendMessage('controller:changed', data=data_recieved)


class ThreadedControllerServer(SocketServer.TCPServer, SocketServer.ThreadingMixIn):
    pass


class CarClientHandler(SocketServer.BaseRequestHandler):
    def handle(self):
        pub.subscribe(self.controller_listener, 'controller:changed')
        while 1:
            print "Car Says: {0}".format(self.request.recv(4096).strip())

    def controller_listener(self, data):
        print 'CarClientHandler: controller:changed event fired with: '
        print 'data =', data

class ThreadedCarServer(SocketServer.TCPServer, SocketServer.ThreadingMixIn):
    pass

if __name__ == "__main__":
    # Run again if client is disconnected
    while True:
        try:
            controller_server = ThreadedControllerServer((CONTROLLER_SERVER_HOST, CONTROLLER_SERVER_PORT),
                                                         ControllerClientHandler)

            controller_thread = threading.Thread(target=controller_server.serve_forever, args=(0.005,))
            controller_thread.start()

            print "Started Controller Server on thread: {} on ip: {}".format(controller_thread.name,
                                                                             controller_server.server_address)

            car_server = ThreadedCarServer((CAR_SERVER_HOST, CAR_SERVER_PORT),
                                           CarClientHandler)

            car_thread = threading.Thread(target=car_server.serve_forever, args=(0.005,))
            car_thread.start()

            print "Started Car Server on thread: {} on ip: {}".format(car_thread.name,
                                                                      car_server.server_address)

            controller_thread.join()
            car_thread.join()

        except Exception as e:
            print e


# SERVER_HOST = ""
# SERVER_PORT = 9080
#
# CAR_HOST = ""
# CAR_PORT = 9081
#
#
# class CarClientHandler(object):
#     def __init__(self):
#
#         # Create the socket server for the car client
#         self._create_car_socket((CAR_HOST, CAR_PORT))
#
#         # Subscribe to controller events with appropriate callback method
#         observable.subscribe(self._on_data_received)
#
#
#     def _create_car_socket(self, car_address):
#         self.carSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
#         self.carSocket.bind(car_address)
#         self.carSocket.listen(1)
#         self.car_conn, self.car_addr = self.carSocket.accept()
#         print "Car client connected successfully."
#
#     def _on_data_received(self, data):
#
#         # If data is bad, close connection, otherwise pipe it to the car
#         if data in ("", None):
#             self.car_conn.close()
#         else:
#             self.car_conn.send(data)
#
#
# class ControllerClientHandler(SocketServer.BaseRequestHandler, Observable):
#     def handle(self):
#         CarClientHandler(self)
#
#         valid_data = True
#
#         while valid_data:
#             data = self.request.recv(4096)
#             print "Received data from controller: {0}".format(data)
#             self.fire(data=data)
#
#             valid_data = self._validate_data(data)
#
#     def _validate_data(self, data):
#         """
#         :param data: Data received from controller client
#         :return: True if data was valid, false if not
#         """
#         if data in ("", None):
#             self.finish()
#
#             return False
#         return True
#
#
# class ThreadedCarServer(SocketServer.TCPServer, SocketServer.ThreadingMixIn):
#     def __init__(self, server_address, server_handler, name=None):
#         SocketServer.TCPServer.__init__(self, server_address, server_handler)

#
# if __name__ == "__main__":
#     # Run again if client is disconnected
#     while True:
#         try:
#             server = ThreadedCarServer((SERVER_HOST, SERVER_PORT), ControllerClientHandler)
#
#             server_thread = threading.Thread(target=server.serve_forever(0.005))
#             #server_thread.daemon = True
#             server_thread.start()
#
#             print "Started Server on thread: {} on ip: {}".format(server_thread.name, server.server_address)
#         except Exception as e:
#             print e

