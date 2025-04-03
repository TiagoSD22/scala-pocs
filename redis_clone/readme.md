# Redis Clone in Scala 3

## Overview

This project is a Scala 3 implementation of a Redis clone, inspired by the book *Build Your Own Redis in C/C++* by James Smith. The goal of this project is to provide a learning experience by re-implementing the core features of Redis using Scala 3.

## Reference

For more information on the original implementation and the concepts behind Redis, please refer to the [Build Your Own Redis](https://build-your-own.org/redis/) website.

## Project Structure

- `src/main/scala/chapter3/Server.scala`: Contains the server implementation that handles TCP and UDP connections.
- `src/main/scala/chapter4/Server.scala`: Contains the updated server implementation that handles TCP connections and processes client requests.
- `src/main/scala/chapter4/Client.scala`: Contains the client implementation that connects to the server, sends messages, and receives acknowledgments.
- `src/main/scala/chapter4/App.scala`: Contains the application entry point to start the server and send client requests.
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