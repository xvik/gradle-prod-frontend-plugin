# gradle-prod-frontend-plugin
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](http://www.opensource.org/licenses/MIT)
[![CI](https://github.com/xvik/gradle-prod-frontend-plugin/actions/workflows/CI.yml/badge.svg)](https://github.com/xvik/gradle-prod-frontend-plugin/actions/workflows/CI.yml)
[![Appveyor build status](https://ci.appveyor.com/api/projects/status/github/xvik/gradle-prod-frontend-plugin?svg=true)](https://ci.appveyor.com/project/xvik/gradle-prod-frontend-plugin)
[![codecov](https://codecov.io/gh/xvik/gradle-prod-frontend-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/xvik/gradle-prod-frontend-plugin)

### About

Optimize static resources for production

Features:
* Feature 1
* Feature 2

##### Summary

* Configuration: `prodFrontend`
* Tasks:
    - `task1` - brief task description       

### Setup


[![Maven Central](https://img.shields.io/maven-central/v/ru.vyarus/gradle-prod-frontend-plugin.svg)](https://maven-badges.herokuapp.com/maven-central/ru.vyarus/gradle-prod-frontend-plugin)

[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/ru.vyarus/prod-frontend/ru.vyarus.prod-frontend.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=plugins%20portal)](https://plugins.gradle.org/plugin/ru.vyarus.prod-frontend)

```groovy
plugins {
    id 'ru.vyarus.prod-frontend' version '0.1.0'
}
```

OR

```groovy
buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath 'ru.vyarus:gradle-prod-frontend-plugin:0.1.0'
    }
}
apply plugin: 'ru.vyarus.prod-frontend'
``` 

### Usage

---
[![gradle plugin generator](http://img.shields.io/badge/Powered%20by-%20Gradle%20plugin%20generator-green.svg?style=flat-square)](https://github.com/xvik/generator-gradle-plugin)
