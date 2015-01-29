# import SocketServer
# import threading
#
#
# class ServerHandler(SocketServer.BaseRequestHandler):
#     def handle(self):
#         while 1:
#             pass
#             #data = self.request.recv(4096).strip()
#             #self._parse_data(data)
#
#     def _parse_data(self, data):
#         if data != '':
#             print data
#
#
# class ThreadedCarReceiver(SocketServer.TCPServer, SocketServer.ThreadingMixIn):
#     pass
#
# if __name__ == "__main__":
#     SERVER_HOST = ""
#     SERVER_PORT = 9081
#
#     try:
#         server = ThreadedCarReceiver((SERVER_HOST, SERVER_PORT), ServerHandler)
#
#         server_thread = threading.Thread(target=server.serve_forever(0.005))
#         server_thread.daemon = True
#         server_thread.start()
#
#         print "Started Server on thread: {} on ip: {}".format(server_thread.name, server.server_address)
#     except Exception as e:
#         print e.message

import socket

SERVER_IP = "192.168.0.102"
SERVER_PORT = 9081

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect((SERVER_IP, SERVER_PORT))

print "Connected to main server!"

while True:
    data = sock.recv(1024)
    print "Received: {}".format(data)