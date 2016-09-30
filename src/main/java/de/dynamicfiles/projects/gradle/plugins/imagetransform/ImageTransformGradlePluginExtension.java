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

import de.dynamicfiles.projects.gradle.plugins.imagetransform.dto.ImageFormatTarget;
import groovy.lang.Closure;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.gradle.api.file.FileCollection;
import org.gradle.util.ConfigureUtil;

/**
 *
 * @author Danny Althoff
 */
public class ImageTransformGradlePluginExtension {

    private List<ImageTransformEntry> transformEntries = new ArrayList<>();

    public void from(String singleSourcePath, Closure closure) {
        ImageFormatTarget imageFormatTarget = new ImageFormatTarget();
        ConfigureUtil.configure(closure, imageFormatTarget);

        // TODO prepare conversion-maps
        if( singleSourcePath.contains("*") ){
            // NOPE, has to be done via files("something/*.ext")
        }
    }

    public void from(FileCollection multipleSources, Closure closure) {
        Set<File> files = multipleSources.getFiles();
        if( files.isEmpty() ){
            return;
        }

        ImageFormatTarget imageFormatTarget = new ImageFormatTarget();
        ConfigureUtil.configure(closure, imageFormatTarget);
    }

    private boolean noAutoBinding = false;
    private String appName = null;
    private boolean appendResolution = true;
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
