# Redis Clone in Scala 3

## Overview

This project is a Scala 3 implementation of a Redis clone, inspired by the book *Build Your Own Redis in C/C++* by James Smith. The goal of this project is to provide a learning experience by re-implementing the core features of Redis using Scala 3.

## Reference

For more information on the original implementation and the concepts behind Redis, please refer to the [Build Your Own Redis](https://build-your-own.org/redis/) website.

## Project Structure

- `src/main/scala/chapter3/Server.scala`: Contains the server implementation that handles TCP and UDP connections.
- `build.sbt`: SBT build configuration file.

## Getting Started

### Prerequisites

- Scala 3.3.5
- SBT 1.5.5 or later

### Running the Server

1. Clone the repository:
   ```sh
   git clone https://github.com/yourusername/redis_clone.git
   cd redis_clone
   ```

2. Update the `application.conf` file with the desired configuration:
   ```hocon
   server {
     protocol = "tcp"  # or "udp"
     address = "127.0.0.1"
     port = 8080
   }
   ```

3. Start the server using SBT:
   ```sh
   sbt run
   ```

## Features

- **TCP and UDP Support**: The server can handle both TCP and UDP connections based on the configuration.
- **Message Logging and Acknowledgment**: The server logs incoming messages and sends back an acknowledgment.

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.