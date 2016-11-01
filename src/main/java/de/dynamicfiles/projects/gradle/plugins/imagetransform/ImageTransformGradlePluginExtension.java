/*
 * Copyright 2016 Danny Althoff
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.dynamicfiles.projects.gradle.plugins.imagetransform;

import de.dynamicfiles.projects.gradle.plugins.imagetransform.dto.ImageTransformEntry;
import de.dynamicfiles.projects.gradle.plugins.imagetransform.dto.ImageFormatRequest;
import groovy.lang.Closure;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;

/**
 *
 * @author Danny Althoff
 */
public class ImageTransformGradlePluginExtension {

    private List<ImageTransformEntry> transformEntries = new ArrayList<>();

    public void from(String singleSourcePath, Closure closure) {
        if( singleSourcePath.contains("*") ){
            // NOPE, has to be done via files("something/*.ext")
            throw new GradleException("You transformImages-configuration is faulty! Please make sure to specify multiple source-files via \"fileTree('someFolder').include('*.ext')\".");
        }

        ImageFormatRequest imageFormatTarget = new ImageFormatRequest();
        imageFormatTarget.setSource(singleSourcePath);

        // org.gradle.util.ConfigureUtil is considered to be "internal"
        // https://discuss.gradle.org/t/entire-package-org-gradle-util-is-missing-from-javadoc-and-groovydoc/14407/5
        closure.setDelegate(imageFormatTarget);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();

        transformEntries.addAll(imageFormatTarget.getTransformEntries());
    }

    public void from(File singleFileSources, Closure closure) {
        from(singleFileSources.getAbsolutePath(), closure);
    }

    public void from(FileCollection multipleSources, Closure closure) {
        Set<File> files = multipleSources.getFiles();
        if( files.isEmpty() ){
            return;
        }

        files.forEach(file -> {
            from(file.getAbsolutePath(), closure);
        });
    }

    public List<ImageTransformEntry> getTransformEntries() {
        return transformEntries;
    }

    // javafx-gradle-plugin stuff
    private boolean noAutoBinding = false;
    private String appName = null;
    // normal stuff
    private boolean appendResolution = false;
    private String resolutionFilenameDelimiter = "-";

    public boolean isNoAutoBinding() {
        return noAutoBinding;
    }

    public void setNoAutoBinding(boolean noAutoBinding) {
        this.noAutoBinding = noAutoBinding;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public boolean isAppendResolution() {
        return appendResolution;
    }

    public void setAppendResolution(boolean appendResolution) {
        this.appendResolution = appendResolution;
    }

    public String getResolutionFilenameDelimiter() {
        return resolutionFilenameDelimiter;
    }

    public void setResolutionFilenameDelimiter(String resolutionFilenameDelimiter) {
        this.resolutionFilenameDelimiter = resolutionFilenameDelimiter;
    }

}
