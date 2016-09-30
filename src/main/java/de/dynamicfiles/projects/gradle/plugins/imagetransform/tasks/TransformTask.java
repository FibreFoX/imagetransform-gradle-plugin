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

import de.dynamicfiles.projects.gradle.plugins.imagetransform.ImageTransformGradlePluginExtension;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

/**
 *
 * @author Danny Althoff
 */
public class TransformTask extends DefaultTask {

    @TaskAction
    public void performTransformations() {
        Project project = getProject();
        // get all transformation entries
        ImageTransformGradlePluginExtension ext = project.getExtensions().getByType(ImageTransformGradlePluginExtension.class);

        try{
            // TODO
            Sanselan.getBufferedImage(new File(""));
        } catch(ImageReadException | IOException ex){
            Logger.getLogger(TransformTask.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
