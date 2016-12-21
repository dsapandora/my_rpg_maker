
Building
--------

Below directions assume a POSIX environment. It should still work on Windows (I've done it), but you may need to modify the directions slightly.

Pre-requisites:

+  git
+  Java 6, 7, or 8. OR Openjdk-7, Openjdk-8 (8u40-b27-1 or more)

Build instructions:

1. Check that your Java install is working. It can be Java 6, 7, or 8. If this does not work, make sure Java is installed an in your PATH.
    ```
    $ java -version                                                                                                                                              
    java version "1.7.0_55"
    OpenJDK Runtime Environment (IcedTea 2.4.7) (ArchLinux build 7.u55_2.4.7-1-x86_64)
    OpenJDK 64-Bit Server VM (build 24.51-b03, mixed mode)
    ```

2. Fork this repository into your own GitHub account. Clone your fork and enter it.
    ```
    $ git clone https://github.com/yourusername/my_rpg_maker.git
    $ cd my_rpg_maker
    ```

3. Run the included gradlew script to build and run the program:
    ```
    Linux / Mac:
    $ ./gradlew run

    Windows:
    gradlew.bat run
    ```

4. You should be able to import the Gradle project into Eclipse or another IDE.
   See: https://github.com/libgdx/libgdx/wiki/Gradle-and-Eclipse

Commiting and Automated tests
-----------------------------

Automated tests are how my_rpg_maker verifies that changes don't break existing functionality. Run automated tests before committing!

1. Run automated tests with:
    ```
    $ ./gradlew test
    ```

Packaging into binaries
-----------------------

Prerequisites:

+ launch4j
  + launch4j may require 32-bit libraries installed. On Ubuntu 14.04:
    ```
    sudo apt-get install lib32z1 lib32ncurses5 lib32bz2-1.0 lib32stdc++6
    ```

Instructions:

1. Enter the repository and run the package shell script.
    ```
    $ cd my_rpg_maker
    $ ./package/package.sh
    ```

2. Find your binaries in:
    ```
    $ ls package/target/
    my_rpg_maker-0.1-SNAPSHOT.exe  my_rpg_maker-0.1-SNAPSHOT.tar.gz
    ```

Misc
----

Please use LF line endings. This will allow for CRLF line endings under Windows, but auto-convert to LF on checkin.
    ```
    $ git config --global core.autocrlf input
    ```

Development Notes
-----------------

+ If you add a resource type, make sure you add it to the list at Resource.resourceTypes.

+ If you add an my_rpg_maker.model.event.EventCmd, be sure to add it to EventCmd.types.

+ Use Array as the collection type. It works in tests now due to a custom DeepEqualMatcher, is performant, and well supported in serialization.

