# InterProg

A brief description of what this project does and who it's for

## Installation

#### First steps :

```bash
git clone https://github.com/Nixo630/InterProg.git
cd InterProg
```

#### Prerequisite :

- Have gradle on his own computer
- Have java (java 21 or higher)

#### In order to install gradle :

##### Linux :

```bash
sdk install gradle
```

##### MacOs :

```bash
sdk install gradle
```

##### Windows :
####
To install gradle on Windows you can only install it manually.
So you need to follow the steps explained at https://docs.gradle.org/current/userguide/installation.html

Once you installed gradle you can check your version with :

```bash
gradle --version
```

You need to have at least Gradle 8.12.1 to run this project.

#### Install all the dependencies and run the application :
####
##### Compile and install :
####
```bash
gradle build
```

##### Run :

####
```bash
gradle run
```

## Export the project :

In order to simply export the project in a .exe file or .jar file you just need to follow these commands :

#####
If you didn't build the project at least once :
```bash
gradle build
```

Then you export the project and every assets and dependencies in a .jar file :
```bash
gradle fatJar
```
And finally you export the project in a .exe file (if you only want the .exe file you still need the .jar one first with the previous command) :
```bash
gradle createExe
```
You will now see a InterProg.jar file in the /build/libs/ directory

And you will also see a InterProg.exe file in the /build/launch4j/ directory.

Note : To import it you also need the lib directory
