import asyncio
from server_protocol import PenguinServerProtocol

def start_server(port):
    # Fancy python 3.5 stuff for async stuff
    loop = asyncio.get_event_loop()
    print("Starting UDP server")
    # One protocol instance will be created to serve all client requests
    listen = loop.create_datagram_endpoint(
        PenguinServerProtocol,
        local_addr=('127.0.0.1', port))
    transport, protocol = loop.run_until_complete(listen)
    
    try:
        loop.run_forever()
    except KeyboardInterrupt:
        pass
    
    transport.close()
    loop.close()



