# COMP 4321 Project Phase 1 User Guide

The project is the course project for the HKUST COMP 4321 course. It is a web search engine based on vector space model and powered by RocksDB. This is the user guide for the web search engine crawler and indexer done in phase 1.

## Installation Guide

### Prerequisite

#### Java

The project runs with Java 8. It is recommended to have JDK 8 installed. Higher versions are not tested.

Gradle is used for dependency management for this project. The gradle wrapper script will be able to detect your Java installation. Make sure the java binary directory is in `$PATH` and `$JAVA_HOME` is set to the Java installation directory.

#### Unix-like Environment

We require a unix-like environment to run some of the scripts. It is not required but it would make your life easier.

The project is well-tested in the following environment:

- CentOS 7 (Cloud VM given)
- Arch Linux
- MacOS Big Sur

Although theoretically this project should have no problem running in the Windows environment, it is not tested and we will not guarantee that it would work.

### Building the Project

With Gradle, all the dependecy (including RocksDB and jsoup) will be automatically pulled in during compilation. For simplicity, you can build a jar file with all libraries packaged in a single jar.

```bash=
./gradlew shadowJar
```

The compiled jar file will be located at `build/libs/COMP4321ProjectPhase1-1.0-all.jar`.

## Usage Guide

### Running the Program

We provide a convenient script `phase1.sh` to run the program easily. The script will automatically run the `shadowJar` gradle task if the required jar does not exist. The arguments of the script will be directly forwarded to the java application.

```bash=
./phase1.sh
```

You can see all of the options available by invoking the help option.

```bash=
./phase1.sh --help
```

```
Usage: <main class> [-chpV] [-f=<forwardDb>] [-o=<outputFile>] [-u=<crawlUrl>]
  -c, --crawl            Crawl the web to update database document records
  -f, --forward-index=<forwardDb>
                         The database name of the forward index
  -h, --help             Display this help message
  -o, --output=<outputFile>
                         The file to print for printing database records
  -p, --print            Printing database forward index to file
  -u, --url=<crawlUrl>   The root URL to crawl
```

### Crawler

To crawl the pages and index the pages into the database, one can specify the `--crawl` option.

```bash=
./phase1.sh --crawl
```

The `--url` option can be specified to set the root page to be crawled. It will crawl the https://www.cse.ust.hk by default if the option is not specified.

```bash=
./phase1.sh --crawl --url https://www.ece.ust.hk
```

### Index Viewer

To print the forward index stored in the database, one can specify the `--print` option.

```bash=
./phase1.sh --print
```

The program will output to `spider_result.txt` as specified in the project description. To change the output location, the `--output` option can be specified.

```bash=
./phase1.sh --print --output crawl_result.txt
```
