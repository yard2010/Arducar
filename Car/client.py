import socket
import threading

SERVER_IP = "192.168.0.102"
SERVER_PORT = 9081
MAX_MESSAGE_LEN = len("X-100Y-100")


def process_input():
    while True:
        data = ""
        data += sock.recv(1024)
        print "Received: {}".format(data)
        x, y = get_velocities(data)

        if None not in (x, y):
            move_car(x, y)


def get_velocities(data):
    # Shouldn't fail here
    if data[0] == "X":
        curr_message = data[:MAX_MESSAGE_LEN]

        x_vel_data = curr_message.split("Y")[0]

        # If x data is not in data, stop
        if x_vel_data is None:
            return

        # Remove the "X" mark in the data
        x_vel_data = x_vel_data[1:]

        # Get the y data after the x. The beginning of the following message must be present to parse this
        y_vel_data = curr_message[len(x_vel_data):].split("X")[0]

        # If y data is not in data, stop
        if y_vel_data is None:
            return

        # Remove the "X" mark in the data
        y_vel_data = y_vel_data[1:]

        # return velocities
        return x_vel_data, y_vel_data
    else:
        print "Data starts with '{0}' for some reason.. fix it".format(data[0])
        del data


def move_car(velocity_X, velocity_y):
    print("TODO Complete. X: {0}, Y: {1}".format(velocity_X, velocity_y))


def send_video():
    pass


if __name__ == "__main__":

    while True:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.connect((SERVER_IP, SERVER_PORT))

        print "Connected to main server!"

        # Create and start the two car handling threads
        car_control_thread = threading.Thread(target=process_input)
        video_control_thread = threading.Thread(target=send_video)

        video_control_thread.start()
        car_control_thread.start()

        video_control_thread.join()
        car_control_thread.join()