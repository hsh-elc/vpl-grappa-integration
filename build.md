# Build Guide

This guide provides steps to recreate the Maven project, install necessary dependencies, and produce a fat JAR file named `ProformaFormatter-<version>-fat-jar-with-dependencies.jar` for the project.

### Prerequisites

- Install **JDK 17** as it is required for this project.

### Step 1: Install Project Dependencies Locally

Install all project dependencies locally, including any parent dependencies required by child dependencies within the project.

Run the shell script to install all required dependencies to your local Maven repository:
```shell
./mvnInstallDependenciesFromGithub.sh
```

To change the version of the ProFormA libraries, edit the `proforma.version` attribute inside the **pom.xml** of this project. After changing the version, you need to run the script again.

### Step 2: Build Fat JAR

To build the fat JAR, use the following command:

```shell
mvn clean package
```

This command cleans any previous builds and packages the project into a single JAR file that includes all dependencies. The resulting file can be found in the `target` subfolder.
