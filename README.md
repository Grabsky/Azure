# Azure
![Latest Release](https://img.shields.io/badge/dynamic/json?url=https%3A%2F%2Fapi.github.com%2Frepos%2FGrabsky%2FAzure%2Freleases%2Flatest&query=tag_name&logo=gradle&style=for-the-badge&label=%20&labelColor=%231C2128&color=%23454F5A)
![Build Status](https://img.shields.io/github/actions/workflow/status/Grabsky/Azure/gradle.yml?style=for-the-badge&logo=github&logoColor=white&label=%20)
![CodeFactor Grade](https://img.shields.io/codefactor/grade/github/Grabsky/Azure/main?style=for-the-badge&logo=codefactor&logoColor=white&label=%20)

Modern and clean, but opinionated multi-purpose plugin for **[Paper](https://github.com/PaperMC/Paper)** servers. Maintained for use on servers I develop for and no public release is planned as of now.

<br />

> [!IMPORTANT]
> Default configuration and translations are in Polish language, making the plugin not suitable for external use. This is something I may work on in the future, but no promises. Contributions are always welcome.

<br />

## Requirements
Requires **Java 21** (or higher) and **Paper 1.21.7** (or higher).

<br />

## Building (Linux)
Some dependencies use **[GitHub Gradle Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry)** and thus may require extra configuration steps for the project to build properly.

```shell
# Cloning repository.
$ git clone https://github.com/Grabsky/Azure.git
# Entering cloned repository.
$ cd ./Azure
# Compiling and building artifacts.
$ ./gradlew clean build
# Publishing the API to local repository.
$ ./gradlew clean publishToMavenLocal
```

<br />

## Contributing
This project is open for contributions. Help in regards of improving performance, adding new features or fixing bugs is greatly appreciated.
