Imagetransform-Gradle-Plugin
============================



Why does this gradle-plugin exist?
==================================

https://github.com/FibreFoX/javafx-gradle-plugin/issues/25



Example `build.gradle`
======================

Please adjust your parameters accordingly:

```groovy
buildscript {
    dependencies {
        classpath group: 'de.dynamicfiles.projects.gradle.plugins', name: 'imagetransform-gradle-plugin', version: '1.0.0'
    }
    
    repositories {
        mavenCentral()
    }
}

repositories {
    mavenCentral()
}


apply plugin: 'imagetransform-gradle-plugin'

// configure imagetransform-gradle-plugin
transformImages {
}
```


Gradle Tasks
============

* `gradle transformImages` - TODO



Last Release Notes
==================

**Version 1.0.0 (???-2016)**

**NOT YET RELEASED - still in development**


(Not yet) Release(d) Notes
==========================

upcoming Version 1.0.1 (???-2016)

* nothing changed yet
