Pawel Cieslewski (2169-8969)
Will Livesey (1874-1956)

Project 4:
Facebook Part 1

Hello! :)

In order to build and run the project type the following commands in this order:

On first terminal:
    sbt "run-main Server.Router"

    (Note: Please ensure that the server is running before running the clients.)

On second terminal:
    sbt "run-main Client.Main numClients"

    (Note: The largest numClients we got to be stable is 2000.)

where numClients is a positive integer.


Usage Instructions:

    After the program is running, enter a number between 0 to numClients-1 on the client terminal to listen to that client's activity

    Please include the quotation marks as well.

    We recommend restarting the server and the client simulator between tests to ensure proper state.


(Note: If you don't enter in numClients it will default to 1000 clients.)

All features are implemented and working. Output will be displayed in the client terminal.

Enjoy!

Sincerely,
Pawel and Will
