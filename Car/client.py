import socket
import threading
import sys

SERVER_IP = ""
SERVER_PORT = 9081
MAX_MESSAGE_LEN = len("X-100Y-100")


def process_input():
    is_connected = True
    data = ""

    while is_connected:
        try:
            data += sock.recv(1024).strip()
        except socket.error:
            print("Connection forcibly closed")
            is_connected = False

        print "Received: {}".format(data)
        data = parse_velocities(data)


def parse_velocities(data):
    # Parse all given data
    while len(data) > 0 and data[0] == "X":
        curr_message = data[:MAX_MESSAGE_LEN]

        x_vel_data = curr_message.split("Y")[0]

        # If x data is not in data, stop
        if x_vel_data is None:
            return data

        # Get the y data after the x. The beginning of the following message must be present to parse this
        y_vel_data = curr_message[len(x_vel_data):].split("X")[0]

        # If y data is not in data, stop
        if y_vel_data is None:
            return data

        # Remove the "X", "Y" mark in the data
        y_vel_data = y_vel_data[1:]
        x_vel_data = x_vel_data[1:]

        # Actually move the car
        move_car(x_vel_data, y_vel_data)

        # Remove message from data (+2 for the two headers removed)
        data = data[len(y_vel_data) + len(x_vel_data) + 2:]
    return data


def move_car(velocity_x, velocity_y):
    print("TODO Complete. X: {0}, Y: {1}".format(velocity_x, velocity_y))


def send_video():
    pass


if __name__ == "__main__":

    # Set controller server host ip (first attribute)
    if len(sys.argv) >= 2:
        SERVER_IP = sys.argv[1]

    while True:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

        # Keep trying to connect until you get it right
        while sock.connect_ex((SERVER_IP, SERVER_PORT)) > 0:
            pass

        print "Connected to main server!"

        # Create and start the two car handling threads
        car_control_thread = threading.Thread(target=process_input)
        video_control_thread = threading.Thread(target=send_video)

        video_control_thread.start()
        car_control_thread.start()

        video_control_thread.join()
        car_control_thread.join()
