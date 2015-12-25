#!/usr/bin/python

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
                print "Car Says: {0}".format(self.request.recv(4096).strip())
            except Exception:
                print("Connection forcibly closed")
                is_connected = False

    def controller_listener(self, data):
        print 'CarClientHandler: controller:changed event fired with: '
        print 'data =', data
        try:
            self.request.send(data)
        except:
            print("Connection forcibly closed")


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
