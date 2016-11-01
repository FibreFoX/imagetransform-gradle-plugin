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
package de.dynamicfiles.projects.gradle.plugins.imagetransform.dto;

import org.apache.commons.imaging.ImageFormat;

/**
 *
 * @author Danny Althoff
 */
public class ImageTransformEntry {

    public String source = null;
    public String destination = null;
    public String resolution = null;
    public boolean appendResolution = false;
    public ImageFormat format = null;

    public ImageTransformEntry getCopy() {
        ImageTransformEntry imageTransformEntry = new ImageTransformEntry();
        imageTransformEntry.source = source;
        imageTransformEntry.destination = destination;
        imageTransformEntry.resolution = resolution;
        imageTransformEntry.appendResolution = appendResolution;
        imageTransformEntry.format = format;
        return imageTransformEntry;
    }

}
