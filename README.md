[![Build Status](https://travis-ci.org/FibreFoX/imagetransform-gradle-plugin.svg?branch=master)](https://travis-ci.org/FibreFoX/imagetransform-gradle-plugin)

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
        // following is required for having "commons-imaging"-library
        maven { url "https://repository.apache.org/content/repositories/snapshots/" }
    }
}

repositories {
    mavenCentral()
}


apply plugin: 'imagetransform-gradle-plugin'

// configure imagetransform-gradle-plugin
// global registration
transformImages {
    // convert multiple files
    from fileTree('src/main/images').include('*'), {
        toPNG '64x64', 'build/jfx/app/*'
        toICO '64x64', 'build/jfx/app/*'
        toBMP '64x64', 'build/jfx/app/*'
        toICNS '64x64', 'build/jfx/app/*'
    }
    // convert single file to multiple resolutions, resulting filenames will be extendes with resolution
    from 'src/main/images/source.png', {
        toPNG(['64x64', '128x128'], 'build/jfx/app/*')
        toICO(['64x64h', '128x128'], 'build/jfx/app/*') // invalid configuration
        toBMP(['64x64x', '128x128'], 'build/jfx/app/*') // valid configuration ;)
        toICNS(['64x64', '128x128'], 'build/jfx/app/*')
    }
    // convert single file to multiple resolutions, resulting filenames in the same order will be taken
    from file('src/main/images/source.png'), {
        toPNG(['64x64', '128x128'], ['build/jfx/app/target-64.png', 'build/jfx/app/target-128.png'])
        toICO(['64x64', '128x128'], ['build/jfx/app/target-64.ico', 'build/jfx/app/target-128.ico'])
        toBMP(['64x64', '128x128'], ['build/jfx/app/target-64.bmp', 'build/jfx/app/target-128.bmp'])
        toICNS(['64x64', '128x128'], ['build/jfx/app/target-64.icns', 'build/jfx/app/target-128.icns'])
    }
}

// task registration
task convertImages(type: de.dynamicfiles.projects.gradle.plugins.imagetransform.tasks.TransformTask) {
    transformGlobals true // convert global configuration as part of this task
    dryRun false

    // specify task-specific transformations
    addTransformations {
        from 'src/main/images/source.png', {
            // the last "true" appends the resolution as part of the filename
            toPNG('128x128', 'build/jfx/app/*', true)
            toICO('128x128', 'build/jfx/app/*', true)
            toBMP('64x64', 'build/jfx/app/*', true)
            toICNS('64x64', 'build/jfx/app/*', true)
        }
    }
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


TODO
====
* use gradle-shadow-plugin to include apache commons-imaging, because this is not reachable inside maven-central (only snapshot-versions) ... this is required because sanselan has no ICNS-support
