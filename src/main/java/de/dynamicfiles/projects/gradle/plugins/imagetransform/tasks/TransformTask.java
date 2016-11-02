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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
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
    private boolean transformGlobals = true;
    private boolean dryRun = false;

    @TaskAction
    public void performTransformations() {
        Project project = getProject();
        // get all transformation entries
        ImageTransformGradlePluginExtension ext = project.getExtensions().getByType(ImageTransformGradlePluginExtension.class);

        if( transformGlobals ){
            List<ImageTransformEntry> globalTransformEntries = ext.getProcessedTransformEntries(project);
            project.getLogger().info(String.format("Found %s global entries inside task", globalTransformEntries.size()));
            workOnTransformEntries(globalTransformEntries);
        }

        List<ImageTransformEntry> taskTransformEntries = taskSpecificExt.getProcessedTransformEntries(project);
        project.getLogger().info(String.format("Found %s task entries inside task", taskTransformEntries.size()));
        workOnTransformEntries(taskTransformEntries);
    }

    public ImageTransformGradlePluginExtension getTaskSpecificExt() {
        return taskSpecificExt;
    }

    public void addTransformations(Closure closure) {
        closure.setDelegate(taskSpecificExt);
        closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        closure.call();
    }

    public boolean isTransformGlobals() {
        return transformGlobals;
    }

    public void setTransformGlobals(boolean transformGlobals) {
        this.transformGlobals = transformGlobals;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    private void workOnTransformEntries(List<ImageTransformEntry> transformEntries) {
        Project project = getProject();

        transformEntries.stream()
                .forEach(validTransformEntry -> {
                    if( dryRun ){
                        project.getLogger().info("would work on > ");
                        project.getLogger().info("\t" + "source: " + validTransformEntry.source);
                        project.getLogger().info("\t" + "requested target destination: " + validTransformEntry.destination);
                        project.getLogger().info("\t" + "requested target resolution: " + validTransformEntry.resolution);
                        project.getLogger().info("\t" + "requested target format: " + validTransformEntry.format.getName());
                        return;
                    }

                    File sourceFile = new File(validTransformEntry.source);
                    File destinationFile = new File(validTransformEntry.destination);

                    try{
                        // create parent folders if not existing
                        Files.createDirectories(destinationFile.toPath().getParent());

                        // TODO only use commons-imaging if ImageIO lacks support for target format
                        Dimension imageSize = Imaging.getImageSize(sourceFile);
//                        System.out.println("Source has dimention: " + imageSize.getWidth() + "x" + imageSize.getHeight());
                        BufferedImage bufferedImage = Imaging.getBufferedImage(sourceFile);
                        int bufferedImageType = bufferedImage.getType();

                        String[] splitResolution = validTransformEntry.resolution.split("x");
                        int width = Integer.parseUnsignedInt(splitResolution[0], 10);
                        int height = Integer.parseUnsignedInt(splitResolution[1], 10);

                        // some references for resizing images:
                        // * http://stackoverflow.com/questions/16497853/scale-a-bufferedimage-the-fastest-and-easiest-way
                        // * https://community.oracle.com/docs/DOC-983611
                        // * http://stackoverflow.com/questions/4216123/how-to-scale-a-bufferedimage
                        // * http://stackoverflow.com/questions/19212990/what-is-an-imageobserver#19213140
                        BufferedImage scaledImage = new BufferedImage(width, height, bufferedImageType != 0 ? bufferedImageType : BufferedImage.TYPE_INT_ARGB);

                        Graphics2D graphicsTarget = scaledImage.createGraphics();
                        // drawImage works async, so make writeImage being called after that ;)
                        boolean couldGenerateImage = graphicsTarget.drawImage(bufferedImage, 0, 0, width, height, (Image img, int infoflags, int x, int y, int width1, int height1) -> {
                            project.getLogger().info("ImageObserver updateImage got called!");
                            boolean abortedGeneratingImage = (infoflags & ImageObserver.ABORT) != 0;
                            if( abortedGeneratingImage ){
                                // skip this image-generation
                                project.getLogger().info("Image-generation got aborted!");
                                return false;
                            }
                            boolean finishedGeneratingImage = (infoflags & ImageObserver.ALLBITS) != 0;
                            if( finishedGeneratingImage ){
                                writeImage(graphicsTarget, project, destinationFile, scaledImage, validTransformEntry);
                                return false;
                            }
                            return true;
                        });
                        if( couldGenerateImage ){
                            project.getLogger().info("Image-generation completed!");
                            writeImage(graphicsTarget, project, destinationFile, scaledImage, validTransformEntry);
                        } else {
                            project.getLogger().info("Image-generation did not complete!");
                            // if could not generate image, hopefully "imageUpdate"/lambda will be called (hopefully)
                        }
                    } catch(NumberFormatException | IOException | ImageReadException ex){
//                        ex.printStackTrace();
                        if( destinationFile.exists() ){
                            destinationFile.delete();
                        }
                    }
                });
    }

    private void writeImage(Graphics2D graphicsTarget, Project project, File destinationFile, BufferedImage scaledImage, ImageTransformEntry validTransformEntry) {
        graphicsTarget.dispose();

        project.getLogger().info("Trying to write image-file: " + destinationFile.getAbsolutePath());
        try{
            // TODO handle incomplete Imaging-library
            // https://issues.apache.org/jira/browse/IMAGING-188
            Imaging.writeImage(scaledImage, destinationFile, validTransformEntry.format, new HashMap<>());
        } catch(IOException | ImageWriteException ex){
            project.getLogger().warn(null, ex);
        }
    }
}
