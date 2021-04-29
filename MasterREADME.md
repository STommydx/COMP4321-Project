# COMP 4321 Project

[![Java CI with Gradle](https://github.com/STommydx/COMP4321-Project-Phase1/actions/workflows/gradle.yml/badge.svg)](https://github.com/STommydx/COMP4321-Project-Phase1/actions/workflows/gradle.yml)

The project is the course project for the HKUST COMP 4321 course. It is a wbe search engine based on vector space model
and powered by RocksDB.

There are 2 directories under this folder, they are `COMP4321-Project` hosting backend logics with `jsp` and tomcat
sever to serve the backend API calls, and `comp4321-proj-ui` contains the frontend web user interface written in
reactJS.

## Repository Structure

There are 2 directories under this current directory. Please respect the repository structure and do not separate/rename
the directories.

For the backend directory `COMP4321-Project`, below shows the important files/directories of it.

```
├── build                                      (Build Folder)
│   └── libs
│       ├── COMP4321Project-1.0-all.jar        (Generated Jar)
│       └── COMP4321Project-1.0.war            (Generated WAR)
├── build.gradle                               (Gradle Config)
├── db                                         (Rocks DB Tables, not included)
│   ├── ForwardIndex
│   ├── InvertedIndex
│   ├── LookupTable
│   └── SuggestIndex
├── phase1.sh                                  (Convenient Script for running the CLI)
├── phase2.sh                                  (Convenient Script for running the web server)
├── README.md                                  (Readme file for the directory)
├── src
│   └── main
│       ├── java                               (Java source files)
│       ├── resources                          (Resource files such as stopword list)
│       └── webapp                             (JSP source files)
└── tomcat                                     (Folder storing Tomcat libraries)
```

For the frontend directory `comp4321-proj-ui`, below shows the important files/directories of it.

```
├── Dockerfile                                  ()
├── nginx.conf                                  (Predefined config file for nginx server)
├── package.json                                (Dependences for yarn)
├── public
├── README.md                                   (Readme file for the directory)
├── src                                         ()
└── yarn.lock                                   ()
```

## Installation Guide

### Pre-requisite

#### Java

The project runs with Java 8. It is recommended to have JDK 8 installed.

Gradle is used for dependency management for this project. The gradle wrapper script will be able to detect your Java
installation. Make sure the java binary directory is in `$PATH` and `$JAVA_HOME` is set to the Java installation
directory, e.g. `/usr/bin/java`.

#### UNIX-like Environment

Some scripts included in the directories only run in UNIX-like environment. It is not required but convenient scripts
cannot be run outside UNIX-like environment.

The project is well-tested in the following environment:

- CentOS 7 (Cloud VM environment)
- Arch Linux
- MacOS Big Sur

Although theoretically this project should have no problem running in Windows, it is not tested and not guaranteed if it
would work.

#### Tomcat

The search engine backend APIs are served with `Apache Tomcat` server. The recommended version is `10.0.5`.

Download and extract the [package](https://tomcat.apache.org/download-10.cgi) to any location you prefer. Alternatively,
you may install Tomcat through your system's package manager. You may want to set the environmental
variable `CATALINA_HOME` variable to the installation location `apache-tomcat-10.0.5`.

#### Node.js and Yarn

The project is created with `create-react-web`. To setup the dependencies for building the app, the yarn package manager
needs to be set up.

To install nodejs (and npm), it can be done with package manager.  
Debian and Ubuntu:

```
sudo apt update  
sudo apt install nodejs
```

CentOS 7:

```
curl -L https://rpm.nodesource.com/setup_10.x | sudo bash -
sudo yum install nodejs
```

MacOS:
<!-- OS by Shitty stupid Monopoly company that gets sued finally and I don't have -->

```
```

To install `yarn`, it can with installed with npm

```
sudo npm install --global yarn
```  

Please refer to the [yarn website](https://classic.yarnpkg.com/en/docs/install) for alternative setup instructions.

#### Static Web Server

To serve the UI website, you will need a static web server to serve the files. Moreover, it should be able to proxy
requests to support API calls to the backend JSP web server. We recommend using `nginx` for this purpose.

#### Docker and Docker Compose (Optional)

This is necessary if and only if installation with docker and docker compose is preferred.

Installing Docker in CentOS:

1. Set up repository
    ```
    sudo yum install yum-utils
    sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
    ```

2. Install docker engine
    ```
    sudo yum install docker-ce docker-ce-cli containerd.io
    ```

3. Start Docker
   ```
   sudo systemctl start docker
   ```

4. Verify that docker is installed correctly by running the `hello-world` image
   ```
   sudo docker run hello-world
   ```

For more information, please refer to docker's [installation guide](https://docs.docker.com/engine/install/centos/).

Installing docker compose in CentOS:

1. Curl a stable release of docker compose
   ```
   sudo curl -L "https://github.com/docker/compose/releases/download/1.29.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
   ```

2. Add executable permission
   ```
   sudo chmod +x /usr/local/bin/docker-compose
   ```

3. Create symbolic link
   ```
   sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
   ```

For more information, please refer to the [installation guide](https://docs.docker.com/compose/install/) of docker compose.

### Hassle-free Deployment with Docker (HDD)

It is recommended to use docker compose for starting up the whole search engine stack instead of installing it
separately. It is done in the backend directory `COMP4321-Project` where `docker-compose.yml` resides.

```
docker-compose build
docker-compose up -d
```

### Building the Backend

The commands and relative paths listed in this section `Building the Backend`
are executed in/relative to directory `COMP4321-Project/`.  
Please change directory into the directory `COMP4321-Project/` to execute the commands.

#### Dependency Management

Dependency in backend directory `COMP4321-Project` is handled by Gradle.

#### Building the CLI for backend

For simplicity, you can build a jar file with all libraries packaged in a single jar. Run the shadowJar Gradle task for
building a fat jar.

```
./gradlew shadowJar
```

The compiled jar file will be located at `build/libs/COMP4321Project-1.0-all.jar`.

#### Building the Web Application for backend

Web application WAR file can be built for deployment with Tomcat server through `war` Gradle task.

```
./gradlew war
```

The compiled war file will be located at `build/libs/COMP4321Project-1.0.war`.

### Building the Frontend

The commands and relative paths listed in this section `Building the Frontend`
are executed in/relative to directory `comp4321-proj-ui/`.  
Please change directory into the directory `comp4321-proj-ui/` to execute the commands listed below.

#### Dependency Management

Dependency in frontend directory `comp4321-proj-ui` is handled with yarn package manager. All the dependencies should
first be installed with the following command.

```
yarn install  
```

#### Building the frontend UI

To build the static web pages, you may use the `build` script in `react-create-app`. The resulting pages will be stored
in the `./build` folder.

```
yarn build
```

### Deploying the UI

The commands and relative paths listed in this section `Deploying the UI`
are executed in/relative to directory `comp4321-proj-ui/`.  
Please change directory into the directory `comp4321-proj-ui/` to execute the commands listed below.

#### Setting up the Static Web Server

You may setup any of the static web server of your preference. Copy all the build files to the web server directory for
serving. The below command assumes nginx server is employed and files in `/usr/share/nginx/html` are served as the root.

```
cp -r ./build/. /usr/share/nginx/html
```

The setup can be verified by loading the home page.

#### Linking the Backend API

For the actual search functionality, you need to setup proxy to point all `/api` requests to the backend JSP server. For
example, if you are using nginx, you may want to setup like the config below (assume backend is deployed at port `3001`)
.

```
server {
    location /api {
        proxy_pass	http://localhost:3001;
    }
}
```

#### Docker Compose

In case you prefer a docker setup, we provide a docker compose configuration file located at backend directory
`COMP4321-Project/` for easy deployment.

To build the docker image needed, run the following:

```bash=
docker-compose build ui
```

To start the deployment, run the following:

```bash=
docker-compose up -d ui
```

### Deploying the Backend Search Engine

The commands and relative paths listed in this section `Deploying the Backend Search Engine`
are executed in/relative to directory `COMP4321-Project/`.  
Please change directory into the directory `COMP4321-Project/` to execute the commands.

The search engine web application is packaged in a single WAR file. There are several possible ways to deploy it.

#### Crawling and Pre-requisites

Before deploying the web application, it is required for the pages to be crawled and indexed into RocksDB for at least
once. It can be done with:

1. convenient script `phase1.sh`
    ```
    ./phase1.sh --crawl --num-docs=20000
    ```
   Options available for the script can be shown by
    ```
    ./phase1.sh --help
    ```
2. shadowJar built in a [section](#deploying-the-backend-search-engine) before
   ```
   java -jar build/libs/COMP4321Project-1.0-all.jar --crawl --num-docs=20000
   ```
   Again, available arguments can be list by
   ```
   java -jar build/libs/COMP4321Project-1.0-all.jar --help
   ```

Environmental variable `SE_DB_BASE_PATH` needs to be set to the RocksDB storage folder. By default, the RocksDB is
stored in `$PWD/db`

#### Convenient Script

To your convenience, we provide a script that do all the things for you. The script will download and extract Tomcat
automatically. Then, the web application will be built (with `war` Gradle task) and deployed to Tomcat.

To start the server, run the following:

```
./phase2.sh startup
```

By default, the application is deployed at port `8080`. You may visit http://localhost:8080 to see it in action.

To stop the server, run the following:

```
./phase2.sh shutdown
```

The port can be changed by modifying `tomcat/conf/server.xml` AFTER running both `startup` and `shutdown` of `phase2.sh`
. By changing `port="8080"` in the following block to `port="3001"` or other desired port, the Tomcat server will serve
at port `3001` or other port when it is started.

```xml

<Connector port="8080" protocol="HTTP/1.1"
           connectionTimeout="20000"
           redirectPort="8443"/>
```

#### Docker Compose

In case you prefer a docker setup, we provide a docker compose configuration file for easy deployment. By default, it
assumes the RocksDB is stored in `$PWD/db`. You may want to change the mount path or use docker volume instead by
modifying `docker-compose.yml`.

To build the docker image needed, run the following:

```bash=
docker-compose build web
```

To start the deployment, run the following:

```bash=
docker-compose up -d web
```

You may want to change the listen port in `docker-compose.yml`. The default port is `3000`.

#### Manual Deploying

The WAR file is located at `build/libs/COMP4321Project-1.0.war` after running the `war` Gradle task. You may follow the
instruction of your web server distribution for deploying a WAR application.

For Tomcat, copy the generated WAR file to `$CATALINA_HOME/webapps/` to deploy the application. You may refer to the
official [documentation](https://tomcat.apache.org/tomcat-10.0-doc/deployer-howto.html) for details.