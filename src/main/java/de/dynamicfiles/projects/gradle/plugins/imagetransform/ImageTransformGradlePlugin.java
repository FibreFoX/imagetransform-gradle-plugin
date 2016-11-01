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
import de.dynamicfiles.projects.gradle.plugins.imagetransform.tasks.TransformTask;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 *
 * @author Danny Althoff
 */
public class ImageTransformGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        // extend project-model to get our settings/configuration via nice configuration
        project.getExtensions().create("transformImages", ImageTransformGradlePluginExtension.class);

        // "There can be only one" ;)
        TransformTask transformTask = project.getTasks().replace("transformImages", TransformTask.class);

        transformTask.setDescription("Convert images into different formats");

        // as this plugin was requested for the javafx-gradle-plugin, we do auto-plumbing here ;)
        project.afterEvaluate(evaluatedProject -> {
            // get current configuration
            ImageTransformGradlePluginExtension ext = project.getExtensions().getByType(ImageTransformGradlePluginExtension.class);

            // set global registered entries as output-files
            List<ImageTransformEntry> globalTransformEntries = ext.getProcessedTransformEntries(evaluatedProject);
            evaluatedProject.getLogger().info(String.format("Found %s global entries", globalTransformEntries.size()));
            registerTransformEntryOutputs(evaluatedProject, transformTask, globalTransformEntries);

            // set task specific registered entries as output-files
            List<ImageTransformEntry> taskTransformEntries = transformTask.getTaskSpecificExt().getProcessedTransformEntries(evaluatedProject);
            evaluatedProject.getLogger().info(String.format("Found %s task entries", taskTransformEntries.size()));
            registerTransformEntryOutputs(evaluatedProject, transformTask, taskTransformEntries);

            // but only when wanted, it's possible to opt-out this feature
            if( !ext.isNoAutoBinding() ){
                Optional.ofNullable(evaluatedProject.getTasks().findByName("jfxNative")).ifPresent(jfxNativeTask -> {
                    jfxNativeTask.dependsOn(transformTask);
                });
            }

            // try to get appName, if not set (would overwrite wanted value otherwise...)
            if( ext.getAppName() == null || ext.getAppName().trim().isEmpty() ){
                // try to work on "jfx"-extension
                Optional.ofNullable(project.getExtensions().findByName("jfx")).ifPresent(jfxExt -> {
                    Class<? extends Object> jfxExtClass = jfxExt.getClass();
                    try{
                        // no class information here, using reflection
                        Method method = jfxExtClass.getMethod("getAppName");
                        Optional.ofNullable(method.invoke(jfxExt))
                                .ifPresent(appName -> {
                                    if( appName instanceof String ){
                                        ext.setAppName((String) appName);
                                    }
                                });
                    } catch(NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex){
                        project.getLogger().warn(null, ex);
                    }
                });
            }
        });
    }

    private void registerTransformEntryOutputs(Project project, DefaultTask task, List<ImageTransformEntry> transformEntries) {
        if( transformEntries == null || transformEntries.isEmpty() ){
            return;
        }
        // register all generated files as task-output for "up-to-date"-checking
        task.getOutputs().upToDateWhen(upToDateTask -> {
            List<File> possibleDestinationFiles = transformEntries.stream().parallel().map(transformEntry -> {
                return new File(transformEntry.destination);
            }).collect(Collectors.toList());
            long existingDestinationFilesCount = possibleDestinationFiles.stream().parallel().filter(File::exists).count();
            // if existing files are equal to possible files, this task is up2date
            return possibleDestinationFiles.size() == existingDestinationFilesCount;
        });
    }

}
