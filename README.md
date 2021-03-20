# COMP 4321 Project

[![Java CI with Gradle](https://github.com/STommydx/COMP4321-Project-Phase1/actions/workflows/gradle.yml/badge.svg)](https://github.com/STommydx/COMP4321-Project-Phase1/actions/workflows/gradle.yml)

The project is the course project for the HKUST COMP 4321 course. It is a web search engine based on vector space model and powered by RocksDB. 

## Repository Structure
Below shows the important files/folder of the project repository.

```
├── build                                      (Build Folder)
│   └── libs
│       └── COMP4321ProjectPhase1-1.0-all.jar  (Generated Jar)
├── build.gradle                               (Gradle Config)
├── Database_Design_Document.pdf               (Design Document required in Phase 1 Project Spec)
├── db                                         (Rocks DB Tables)
│   ├── ForwardIndex
│   ├── InvertedIndex
│   └── LookupTable
├── phase1.sh                                  (Convenient Script for running the project)
├── README.md                                  (This document)
├── readme.txt                                 (readme.txt required in Phase 1 Project Spec)
├── spider_result.txt                          (crawled result required in Phase 1 Project Spec)
├── src                                        (Folder storing all source code)
│   └── main
└── stopwords-en.txt                           (Stopword list used)
```

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

## Contribution Guidelines

### Cloning the project

The project work best with the Intellij IDEA. To import the project in IDEA:

1. Choose `Get from Version Control`
2. Choose GitHub
3. Login GitHub
4. Select this repository `COMP4321-Project-Phase1`
5. Click clone!
6. Open the project
7. Wait for a little bit. IDEA should import the gradle project automatically. ;)

Of course, you can clone the project via the command line if you prefer.

### Submitting Code Changes

The project would not be successful without your contribution! Follow the steps to create a new feature:

1. VCS -> Git -> Branches
2. New Branch
3. Name your branch as `feature/yourfeaturename`
4. Make awesome changes to the code!
5. Commit your changes using VCS -> Commit, remember to stage your changes and make a nice commit message

Before submitting, you should clean up your work:

1. Switch to `master` branch, do a pull to update to the latest changes
2. Switch back to `feature/yourfeaturename`, run a rebase with master
3. Resolve conflicts if needed, seek help if you don't know how to do so

Lastly push the branch to GitHub and create a PR:

1. VCS -> Git -> Push Branch `feature/yourfeaturename`
2. Go to GitHub and create a pull request
3. Set the source as `feature/yourfeaturename` and merge into `master`
4. Wait for the approval!

Refer to the following 2 links for more details on the PR workflow:

1. [GitHub Standard Fork & Pull Request Workflow](https://gist.github.com/Chaser324/ce0505fbed06b947d962)
2. [Pull Requests | Atlassian Git Tutorial](https://www.atlassian.com/git/tutorials/making-a-pull-request)

### Who do I talk to?

Please contact the repo owner Tommy LI in case you have any questions. Feel free to have a chat on other misc stuffs too!
