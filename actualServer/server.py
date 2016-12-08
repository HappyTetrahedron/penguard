import asyncio
import socket
import fcntl
import struct
from server_protocol import PenguinServerProtocol

def start_server(port):
    # Fancy python 3.5 stuff for async stuff
    loop = asyncio.get_event_loop()
    ip = get_ip_address("eth0")
    print("Starting UDP server on %s:%d" % (ip, port))
    # One protocol instance will be created to serve all client requests
    listen = loop.create_datagram_endpoint(
        PenguinServerProtocol,
        local_addr=(get_ip_address("eth0"), port))
    transport, protocol = loop.run_until_complete(listen)
    
    try:
        loop.run_forever()
    except KeyboardInterrupt:
        pass
    
    transport.close()
    loop.close()

def get_ip_address(ifname):
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    return socket.inet_ntoa(fcntl.ioctl(
        s.fileno(),
        0x8915,  # SIOCGIFADDR
        struct.pack('256s', bytes(ifname[:15], "UTF-8"))
    )[20:24])
