# Azure
<span>
    <a href=""><img alt="Latest Published Version" src="https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo.grabsky.cloud%2Freleases%2Fcloud%2Fgrabsky%2Fazure-api%2Fmaven-metadata.xml&style=for-the-badge&logo=gradle&label=%20"></a>
    <a href=""><img alt="Build Status" src="https://img.shields.io/github/actions/workflow/status/Grabsky/Azure/gradle.yml?style=for-the-badge&logo=github&logoColor=white&label=%20"></a>
    <a href=""><img alt="CodeFactor Grade" src="https://img.shields.io/codefactor/grade/github/Grabsky/Azure/main?style=for-the-badge&logo=codefactor&logoColor=white&label=%20"></a>
</span>
<p></p>

Modern and clean, but opinionated multi-purpose plugin for [PaperMC/Paper](https://github.com/PaperMC/Paper) servers. Maintained for use on servers I develop for and no public release is planned as of now.

<br />

> [!IMPORTANT]
> Default configuration and translations are in Polish language, making the plugin not suitable for external use. This is something I may work on in the future, but no promises. Contributions are always welcome.

<br />

## Requirements
Requires **Java 17** (or higher) and **Paper 1.20.4** (or higher).

<br />

## Building (Linux)
Some dependencies use [GitHub Gradle Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry) and thus may require extra configuration steps for the project to build properly.

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
