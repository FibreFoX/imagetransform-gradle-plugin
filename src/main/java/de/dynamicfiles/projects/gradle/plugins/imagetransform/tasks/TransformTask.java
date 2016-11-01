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
package de.dynamicfiles.projects.gradle.plugins.imagetransform.tasks;

import de.dynamicfiles.projects.gradle.plugins.imagetransform.dto.ImageTransformEntry;
import de.dynamicfiles.projects.gradle.plugins.imagetransform.ImageTransformGradlePluginExtension;
import groovy.lang.Closure;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

/**
 *
 * @author Danny Althoff
 */
public class TransformTask extends DefaultTask {

    private ImageTransformGradlePluginExtension taskSpecificExt = new ImageTransformGradlePluginExtension();
    private boolean runGlobalTransformations = true;
    private boolean dryRun = false;

    @TaskAction
    public void performTransformations() {
        Project project = getProject();
        // get all transformation entries
        ImageTransformGradlePluginExtension ext = project.getExtensions().getByType(ImageTransformGradlePluginExtension.class);

        // TODO look for a way to register this prior to task-execution :(
//        registerTransformEntryOutputs(ext.getTransformEntries());
//        registerTransformEntryOutputs(taskSpecificExt.getTransformEntries());
        if( runGlobalTransformations ){
            workOnTransformEntries(ext.getTransformEntries());
        }
        workOnTransformEntries(taskSpecificExt.getTransformEntries());
    }

    public void addTransformations(Closure closure) {
        closure.setDelegate(taskSpecificExt);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
    }

    public boolean isRunGlobalTransformations() {
        return runGlobalTransformations;
    }

    public void setRunGlobalTransformations(boolean runGlobalTransformations) {
        this.runGlobalTransformations = runGlobalTransformations;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    private void registerTransformEntryOutputs(List<ImageTransformEntry> transformEntries) {
        if( transformEntries == null || transformEntries.isEmpty() ){
            return;
        }
        // register all generated files as task-output for "up-to-date"-checking
        getOutputs().files(
                transformEntries.stream().map(transformEntry -> {
                    File destinationFile = new File(transformEntry.destination);
                    if( destinationFile.isAbsolute() ){
                        return destinationFile;
                    }
                    return new File(getProject().getProjectDir(), transformEntry.destination);
                }).collect(Collectors.toList())
        );
    }

    private void workOnTransformEntries(List<ImageTransformEntry> transformEntries) {
        Project project = getProject();

        transformEntries.stream()
                .map(transformEntry -> {
                    File sourceFile = new File(transformEntry.source);
                    if( !sourceFile.isAbsolute() ){
                        sourceFile = new File(project.getProjectDir(), transformEntry.source);
                    }
                    if( sourceFile.exists() ){
                        transformEntry.source = sourceFile.getAbsolutePath();
                        return transformEntry;
                    }
                    return null;
                })
                .filter(entry -> entry != null)
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
                .peek(entry -> {
                    if( dryRun ){
                        System.out.println("would work on > ");
                        System.out.println("\t" + "source: " + entry.source);
                        System.out.println("\t" + "requested target destination (RAW): " + entry.destination);
                        System.out.println("\t" + "requested target resolution: " + entry.resolution);
                        System.out.println("\t" + "requested target format: " + entry.format.getName());
                    }
                })
                .map(validTransformEntry -> {
                    // toFORMAT(['64x64', '128x128'], 'build/jfx/app/*', true)
                    // set up the destination filename
                    File sourceFile = new File(validTransformEntry.source);
                    File destinationFile = new File(validTransformEntry.destination);
                    if( destinationFile.getName().equals("*") ){
                        // set new filename like source, just with replaced extension
                        String sourceFileName = sourceFile.getName();
                        int lastIndexOfDot = sourceFileName.lastIndexOf('.');
                        String sourceFileNameFirstPart = sourceFileName.substring(0, lastIndexOfDot);
                        if( lastIndexOfDot < 0 ){
                            // no dot inside source filename
                            if( validTransformEntry.appendResolution ){
                                validTransformEntry.destination = destinationFile.getParentFile().toPath().resolve(sourceFileNameFirstPart + "-" + validTransformEntry.resolution + sourceFileName.substring(lastIndexOfDot, sourceFileName.length() - 1)).toFile().getAbsolutePath();
                            } else {
                                validTransformEntry.destination = destinationFile.getParentFile().toPath().resolve(sourceFileName).toFile().getAbsolutePath();
                            }
                        } else {
                            if( validTransformEntry.appendResolution ){
                                validTransformEntry.destination = destinationFile.getParentFile().toPath().resolve(sourceFileNameFirstPart + "-" + validTransformEntry.resolution + "." + validTransformEntry.format.getExtension().toLowerCase()).toFile().getAbsolutePath();
                            } else {
                                validTransformEntry.destination = destinationFile.getParentFile().toPath().resolve(sourceFileNameFirstPart + "." + validTransformEntry.format.getExtension().toLowerCase()).toFile().getAbsolutePath();
                            }
                        }
                    }
                    return validTransformEntry;
                })
                .forEach(validTransformEntry -> {
                    if( dryRun ){
                        System.out.println("would work on > ");
                        System.out.println("\t" + "source: " + validTransformEntry.source);
                        System.out.println("\t" + "requested target destination: " + validTransformEntry.destination);
                        System.out.println("\t" + "requested target resolution: " + validTransformEntry.resolution);
                        System.out.println("\t" + "requested target format: " + validTransformEntry.format.getName());
                    }
                    // TODO work on this item :)
                });
    }
}
