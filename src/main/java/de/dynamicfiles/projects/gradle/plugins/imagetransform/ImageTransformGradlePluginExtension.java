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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
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

    public List<ImageTransformEntry> getProcessedTransformEntries(Project project) {
        return transformEntries.stream().parallel()
                .map(transformEntry -> {
                    return transformEntry.getCopy();
                })
                .map(transformEntry -> {
                    File sourceFile = new File(transformEntry.source);
                    if( !sourceFile.isAbsolute() ){
                        sourceFile = new File(project.getProjectDir(), transformEntry.source);
                    }
                    if( sourceFile.exists() ){
                        transformEntry.source = sourceFile.getAbsolutePath();
                        return transformEntry;
                    }
                    project.getLogger().warn("Input file for transformation does not exist: " + sourceFile.getAbsolutePath());
                    return null;
                })
                .filter(entry -> entry != null)
                // only valid image files
                .filter(existingTransformEntry -> {
                    File sourceFile = new File(existingTransformEntry.source);
                    try{
                        ImageFormat guessedFormat = Imaging.guessFormat(sourceFile);
                        if( guessedFormat != org.apache.commons.imaging.ImageFormats.UNKNOWN ){
                            return true;
                        }
                    } catch(ImageReadException | IOException ex){
                        project.getLogger().warn(null, ex);
                    }
                    project.getLogger().warn("Could not determine file-format from: " + sourceFile.getAbsolutePath());
                    return false;
                })
                // check valid resolutin-setting
                .filter(existingTransformEntry -> {
                    // check if was set
                    if( existingTransformEntry.resolution == null || existingTransformEntry.resolution.trim().isEmpty() ){
                        return false;
                    }
                    // check if "x" is somewhere
                    String[] splitResolution = existingTransformEntry.resolution.split("x");

                    // NOTE: split makes it possible to remove dangling empty entry like this:
                    // "64x64x" -> it results in ["64","64"] instead of ["64","64",""])
                    if( splitResolution.length != 2 ){
                        project.getLogger().warn("Found invalid resolution: " + existingTransformEntry.resolution);
                        return false;
                    }
                    String leftPart = splitResolution[0].trim();
                    String rightPart = splitResolution[1].trim();
                    // contains at least two parts, but at least one might be empty
                    if( leftPart.isEmpty() || rightPart.isEmpty() ){
                        project.getLogger().warn("Found invalid resolution: " + existingTransformEntry.resolution);
                        return false;
                    }
                    // only valid if both parts are numbers only
                    boolean bothPartsContainingNumbersOnly = leftPart.replaceAll("[0-9]+", "").isEmpty() && rightPart.replaceAll("[0-9]+", "").isEmpty();
                    if( bothPartsContainingNumbersOnly == false ){
                        project.getLogger().warn("Found invalid resolution: " + existingTransformEntry.resolution);
                    } else {
                        project.getLogger().info("Found valid resolution: " + leftPart + "x" + rightPart);
                    }
                    return bothPartsContainingNumbersOnly;
                })
                .map(validTransformEntry -> {
                    // set up the destination filename
                    // toFORMAT(['64x64', '128x128'], 'build/jfx/app/*', true)
                    // it is NOT supported to have a STAR as part of the folder-name
                    File sourceFile = new File(validTransformEntry.source);
                    File destinationFile = new File(validTransformEntry.destination);
                    if( destinationFile.getName().equals("*") ){
                        // set new filename like source, just with replaced extension
                        String sourceFileName = sourceFile.getName();
                        int lastIndexOfDot = sourceFileName.lastIndexOf('.');
                        if( lastIndexOfDot < 0 ){
                            // no dot inside source filename
                            if( validTransformEntry.appendResolution ){
                                validTransformEntry.destination = destinationFile.getParentFile().toPath().resolve(sourceFileName + resolutionFilenameDelimiter + validTransformEntry.resolution).toFile().getAbsolutePath();
                            } else {
                                validTransformEntry.destination = destinationFile.getParentFile().toPath().resolve(sourceFileName).toFile().getAbsolutePath();
                            }
                        } else {
                            String sourceFileNameFirstPart = sourceFileName.substring(0, lastIndexOfDot);
                            if( validTransformEntry.appendResolution ){
                                validTransformEntry.destination = destinationFile.getParentFile().toPath().resolve(sourceFileNameFirstPart + resolutionFilenameDelimiter + validTransformEntry.resolution + "." + validTransformEntry.format.getExtension().toLowerCase()).toFile().getAbsolutePath();
                            } else {
                                validTransformEntry.destination = destinationFile.getParentFile().toPath().resolve(sourceFileNameFirstPart + "." + validTransformEntry.format.getExtension().toLowerCase()).toFile().getAbsolutePath();
                            }
                        }
                    } else {
                        // change user-selected filename !
                        if( validTransformEntry.appendResolution ){
                            String destinationFileName = destinationFile.getName();
                            int lastIndexOfDot = destinationFileName.lastIndexOf('.');
                            String destinationFileNameFirstPart = destinationFileName.substring(0, lastIndexOfDot);
                            validTransformEntry.destination = destinationFile.getParentFile().toPath().resolve(destinationFileNameFirstPart + resolutionFilenameDelimiter + validTransformEntry.resolution + "." + validTransformEntry.format.getExtension().toLowerCase()).toFile().getAbsolutePath();
                        }
                    }
                    return validTransformEntry;
                })
                .collect(Collectors.toList());
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
