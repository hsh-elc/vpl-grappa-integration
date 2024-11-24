# Build Guide

This guide provides steps to recreate the Maven project, install necessary dependencies, and produce a fat JAR file for the project.

### Prerequisites

- Install **JRE 17** as it is required for this project.
- Ensure the following files are available and stored in a directory named **lib** in the project root:
  - grappa-webservice-parent.xml
  - proformautil-0.2.1.jar
  - proformautil-2-1-0.2.1.jar
  - proformaxml-0.2.1.jar
  - proformaxml-2-1-0.2.1.jar

### Step 1: Install Project Dependencies Locally

Install all project dependencies locally, including any parent dependencies required by child dependencies within the project.

#### Commands

1. **Install `grappa-webservice-parent`**  
   This is a parent dependency required for Proforma JARs.

   ```shell
   mvn install:install-file -Dfile=./lib/grappa-webservice-parent.xml -DgroupId=de.hsh.grappa -DartifactId=grappa-webservice-parent -Dversion=2.4.1 -Dpackaging=xml
   ```

2. **Install `proformautil`**

   ```shell
   mvn install:install-file -Dfile=./lib/proformautil-0.2.1.jar -DgroupId=proforma -DartifactId=proformautil -Dversion=0.2.1 -Dpackaging=jar
   ```

3. **Install `proformautil-2-1`**

   ```shell
   mvn install:install-file -Dfile=./lib/proformautil-2-1-0.2.1.jar -DgroupId=proforma -DartifactId=proformautil-2-1 -Dversion=0.2.1 -Dpackaging=jar
   ```

4. **Install `proformaxml`**

   ```shell
   mvn install:install-file -Dfile=./lib/proformaxml-0.2.1.jar -DgroupId=proforma -DartifactId=proformaxml -Dversion=0.2.1 -Dpackaging=jar
   ```

5. **Install `proformaxml-2-1`**

   ```shell
   mvn install:install-file -Dfile=./lib/proformaxml-2-1-0.2.1.jar -DgroupId=proforma -DartifactId=proformaxml-2-1 -Dversion=0.2.1 -Dpackaging=jar
   ```

### Step 2: Build Fat JAR

To build the fat JAR, use the following command:

```shell
mvn clean package
```

This command cleans any previous builds and packages the project into a single JAR file that includes all dependencies.
