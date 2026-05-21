# ParkLight

> Light the way to your spot.

A smart parking lot management system with shortest-path-based slot allocation.
Final project for the Advanced Java course at HIT.

## Repository layout

- `AlgorithmModule/` - Shortest path algorithm library (Part A), packaged as a JAR.
- `ParkLightApp/` - The ParkLight application that consumes the library (Part B).

## Tech stack

- Java 8 (`JavaSE-1.8`)
- JUnit 4 for unit tests
- No external frameworks. Plain `javac` + `jar`.

## How to build

### Part A - AlgorithmModule

    cd AlgorithmModule
    mkdir -p build/main build/test build/all

    javac --release 8 -d build/main \
        src/main/java/com/parklight/algorithm/*.java

    javac --release 8 -d build/test \
        -cp "build/main:lib/junit-4.13.2.jar:lib/hamcrest-core.jar" \
        src/main/test/com/parklight/algorithm/*.java

    cp -R build/main/* build/all/
    cp -R build/test/* build/all/
    jar cf AlgorithmModule.jar -C build/all .

### Part B - ParkLightApp

    cd ParkLightApp
    mkdir -p build/main build/test

    javac --release 8 -d build/main \
        -cp "lib/AlgorithmModule.jar" \
        src/main/java/com/parklight/dm/*.java \
        src/main/java/com/parklight/dao/*.java \
        src/main/java/com/parklight/service/*.java

    javac --release 8 -d build/test \
        -cp "build/main:lib/AlgorithmModule.jar:../AlgorithmModule/lib/junit-4.13.2.jar:../AlgorithmModule/lib/hamcrest-core.jar" \
        src/main/test/com/parklight/service/*.java

## How to run the tests

### Part A (4 tests)

    cd AlgorithmModule
    java -cp "AlgorithmModule.jar:lib/junit-4.13.2.jar:lib/hamcrest-core.jar" \
        org.junit.runner.JUnitCore com.parklight.algorithm.IAlgoShortestPathTest

### Part B (4 tests)

    cd ParkLightApp
    java -cp "build/main:build/test:lib/AlgorithmModule.jar:../AlgorithmModule/lib/junit-4.13.2.jar:../AlgorithmModule/lib/hamcrest-core.jar" \
        org.junit.runner.JUnitCore com.parklight.service.ParkingServiceTest

## Architecture

    +-------------------------------------------------+
    |  ParkingServiceTest (end-to-end test)           |
    +-------------------------------------------------+
                           |
                           v
    +-------------------------------------------------+
    |  Service layer                                  |
    |    ParkingService    BillingService             |
    +-------------------------------------------------+
            |                 |
            v                 v
    +--------------+   +-----------------+
    |  IDao (DAO)  |   |  IAlgoShortest- |
    |              |   |       Path      |
    +--------------+   +-----------------+
            |                 |
            v                 v
    +--------------+   +-----------------+
    | DaoFileImpl  |   | DijkstraAlgo /  |
    | (Object I/O) |   |    AStarAlgo    |
    +--------------+   +-----------------+

Services depend on interfaces, not concrete classes. The shortest-path
algorithm is injected from outside, so swapping Dijkstra for A* requires no
change to the service code.

## Domain model

- `Vehicle` - has a license plate and a type (REGULAR / DISABLED / ELECTRIC).
- `ParkingSpot` - has an id, a type, (x, y) coordinates, and an occupied flag.
- `ParkingTicket` - links a vehicle to a spot with entry/exit times and price.

The parking lot is modeled as a weighted graph (entrance + spots + intersections,
edges = distances). When a vehicle arrives, the service finds the closest
available compatible spot using the injected shortest-path algorithm.
