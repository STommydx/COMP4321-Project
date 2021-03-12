# COMP 4321 Project

This is the working repository for the COMP 4321 project. The project is a search engine based on RocksDB.

## How do I get set up?

### Prerequisites

#### Java

The project runs with Java 8. It is recommended to have JDK 8 installed. Higher versions are not tested.

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

### Running the server

You can find gradle tasks from the Gradle tab on the rightmost toolbar. The `application -> run` task starts the server.

Alternatively, you can run the server in the terminal with the command below:

```
./gradlew run
```

## Contribution guidelines

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

## Versioning

The `master` branch is under constant development but is considered stable. If you would like to find the phase 1/2 submission for the project, please checkout the tag `phase1`/`phase2`.
