# CNT5016C - BitTorrent 
The following README contains build instructions, completed features, and things to do for our teams progress with the CNT5106C BitTorrent project.

NOTE: The current config files are provided in the mid checkpoint since our SSH code is not fully tested. These will not be provided in the final version.

## How to Run
Navigate to the "BitTorrent" project and execute the following commands in the terminal.

```
find -name "*.java" > source.txt
javac -d ./ @source.txt
find -name "*.class" > class.txt
jar cfm PeerProcess.jar manifest.txt @class.txt
java -jar PeerProcess.jar 1000 true
```

## Output
The application will run as described in the project description. Specifically, the application will create multiple processes as indicated in the PeerInfo configuration file. The processes will be initialzed through an SSH connection. The peers will then connect using a asynchronous socket channel and exchange BitTorrent handshake and bitfield messages. Once these messages are exchanged between two peers, the choking/preferred neighbor processes are started, and peers begin requesting pieces from one another.

Each peer will maintain a log message and generate the messages listed in the project description. 

## What is Working
The BitTorrent protocol as described in the project description has been implemented. This includes, but is not limited to:

- Network Related Features
    - Establishing TCP connections using asynchronous socket channels
    - Exchanging BitTorrent handshake messages
    - Serializing and deserializiing handshake and "actual messages".
    - Creating threaded functions for handling the reception of "actual messages".
    - Creating threaded functions for determining the preferred neighbors/choking and the optimistically unchoked neighbor calculations.
    - Connecting multiple machines via SSH
    - Implementing node termination when all peers have received the file. 

- Non-network Related Features
    - Reading the common and peer info configration files
    - Creating and writing the log file.
    - Splitting and reconstructing the file
    - Calculating and updating bitfields between p2p connections
    - Implemented a GUI for configuring the simulation

## Design Details
A single peer is composed of 5 threads when running the BitTorrent protocol:
- Listening for new peer connections
- Handling the reception of messages. Incoming messages are placed in a buffer.
- Listening for the node termination conditions.
- Unchoking/Preferred neighbors calculation
- Optimistically unchoked neighbor calculation

The data shared between the threads are synchronized to prevent race conditions.
