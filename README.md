# CST Episodic Memory Initial Demo
This repo presents the development of a first version for the episodic memory 
modules in CST.
The codes consist of a single simulation based on the [World Server](https://cst.fee.unicamp.br/examples/ws3dexample)
toy problem develop to exemplify capabilities of the Cognitive Systems Toolkit (CST).
The simulation runs on the CoppeliaSim robotics simulator.

## Running Demo

1. The simulation depends on the library [WS3D-Coppelia](https://github.com/CST-Group/WS3D-Coppelia) to commuicate with CoppeliaSim.
Follow the [Dependencies](https://github.com/CST-Group/WS3D-Coppelia#dependencies) section of the library instructions to install the necessary
dependencies for execution.
2. Open CoppeliaSim
3. Go to the folder for this repository and execute the command
```
    $ ./gradlew run
```