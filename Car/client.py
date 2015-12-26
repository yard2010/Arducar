import socket
import threading
import sys
import logging

SERVER_IP = ""
SERVER_PORT = 9081
MAX_MESSAGE_LEN = len("X-100Y-100")
TIMEOUT_VAL = 5
NO_DATA_LIMIT = 10


def process_input(read_sock):
    is_connected = True
    data = ""
    no_data_count = 0

    # Setting timeout so will not get stuck if no input is given.
    # This is so that when one socket dies, all others will too.
    # Timeout value is in seconds
    read_sock.settimeout(TIMEOUT_VAL)

    while is_connected:
        try:
            data += read_sock.recv(1024).strip()
        except (socket.timeout, socket.error) as ex:
            logging.debug("Controller Process Input: " + str(ex))
            is_connected = False

        # Make sure there's data, otherwise let thread die
        if len(data) > 0:
            logging.debug("Received: {}".format(data))
            data = parse_velocities(data)

            # Reset count because data has been received
            no_data_count = 0
        else:
            # Data is empty. This could be a problem. Increase counter
            # If data is empty for NO_DATA_LIMIT times, kill connection
            if no_data_count < NO_DATA_LIMIT:
                no_data_count += 1
            else:
                is_connected = False


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
    logging.debug("TODO Complete. X: {0}, Y: {1}".format(velocity_x, velocity_y))


def send_video(write_sock):
    # TODO complete this function. Need to stream video. This is a stub
    # write_sock.send("0101 fake video data")
    pass


if __name__ == "__main__":

    # Setup logger
    logging.basicConfig(filename='log.log', level=logging.DEBUG)
    ch = logging.StreamHandler(sys.stdout)
    logging.getLogger().addHandler(ch)

    # Set controller server host ip (first attribute)
    if len(sys.argv) >= 2:
        SERVER_IP = sys.argv[1]

    # TODO This single while should be split into two independent handling threads. SPLIT SOCKETS AS WELL?
    # Otherwise client will lose connection over either controller OR video timeout
    # Split sockets or otherwise timeout on the receiving end will kill the sending end and vice versa. Very bad
    while True:
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

        # Keep trying to connect until you get it right
        while sock.connect_ex((SERVER_IP, SERVER_PORT)) > 0:
            logging.debug("Failed to connect to server. Retrying")

        logging.debug("Connected to main server!")

        # Create and start the two car handling threads
        car_control_thread = threading.Thread(target=process_input, args=(sock,))
        video_control_thread = threading.Thread(target=send_video, args=(sock,))

        video_control_thread.start()
        car_control_thread.start()

        video_control_thread.join()
        car_control_thread.join()

        logging.debug("Threads died (connection lost), retrying to connect to server.")
