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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.imaging.ImageFormats;
import org.gradle.api.GradleException;

/**
 *
 * @author Danny Althoff
 */
public class ImageFormatRequest {

    private List<ImageTransformEntry> transformEntries = new ArrayList<>();

    private String sourcePath = null;

    public void setSource(String singleSourcePath) {
        sourcePath = singleSourcePath;
    }

    public List<ImageTransformEntry> getTransformEntries() {
        return transformEntries;
    }

    public void toPNG(String resolution, String destination) {
        toPNG(resolution, destination, false);
    }

    public void toPNG(String resolution, String destination, boolean appendResolution) {
        ImageTransformEntry imageTransformEntry = new ImageTransformEntry();

        imageTransformEntry.resolution = resolution;
        imageTransformEntry.destination = destination;
        imageTransformEntry.source = sourcePath;
        imageTransformEntry.appendResolution = appendResolution;
        imageTransformEntry.format = ImageFormats.PNG;

        transformEntries.add(imageTransformEntry);
    }

    public void toICO(String resolution, String destination) {
        toICO(resolution, destination, false);
    }

    public void toICO(String resolution, String destination, boolean appendResolution) {
        ImageTransformEntry imageTransformEntry = new ImageTransformEntry();

        imageTransformEntry.resolution = resolution;
        imageTransformEntry.destination = destination;
        imageTransformEntry.source = sourcePath;
        imageTransformEntry.appendResolution = appendResolution;
        imageTransformEntry.format = ImageFormats.ICO;

        transformEntries.add(imageTransformEntry);
    }

    public void toBMP(String resolution, String destination) {
        toBMP(resolution, destination, false);
    }

    public void toBMP(String resolution, String destination, boolean appendResolution) {
        ImageTransformEntry imageTransformEntry = new ImageTransformEntry();

        imageTransformEntry.resolution = resolution;
        imageTransformEntry.destination = destination;
        imageTransformEntry.source = sourcePath;
        imageTransformEntry.appendResolution = appendResolution;
        imageTransformEntry.format = ImageFormats.BMP;

        transformEntries.add(imageTransformEntry);
    }

    public void toICNS(String resolution, String destination) {
        toICNS(resolution, destination, false);
    }

    public void toICNS(String resolution, String destination, boolean appendResolution) {
        ImageTransformEntry imageTransformEntry = new ImageTransformEntry();

        imageTransformEntry.resolution = resolution;
        imageTransformEntry.destination = destination;
        imageTransformEntry.source = sourcePath;
        imageTransformEntry.appendResolution = appendResolution;
        imageTransformEntry.format = ImageFormats.ICNS;

        transformEntries.add(imageTransformEntry);
    }

    public void toPNG(List<String> resolutions, String destination) {
        toPNG(resolutions, destination, true);
    }

    private void toPNG(List<String> resolutions, String destination, boolean appendResolution) {
        resolutions.stream().forEach(resolution -> {
            toPNG(resolution, destination, appendResolution);
        });
    }

    public void toICO(List<String> resolutions, String destination) {
        toICO(resolutions, destination, true);
    }

    private void toICO(List<String> resolutions, String destination, boolean appendResolution) {
        resolutions.stream().forEach(resolution -> {
            toICO(resolution, destination, appendResolution);
        });
    }

    public void toBMP(List<String> resolutions, String destination) {
        toBMP(resolutions, destination, true);
    }

    private void toBMP(List<String> resolutions, String destination, boolean appendResolution) {
        resolutions.stream().forEach(resolution -> {
            toBMP(resolution, destination, appendResolution);
        });
    }

    public void toICNS(List<String> resolutions, String destination) {
        toICNS(resolutions, destination, true);
    }

    private void toICNS(List<String> resolutions, String destination, boolean appendResolution) {
        resolutions.stream().forEach(resolution -> {
            toICNS(resolution, destination, appendResolution);
        });
    }

    public void toPNG(List<String> resolutions, List<String> destinations) {
        if( resolutions.size() != destinations.size() ){
            throw new GradleException("It is required to have resolutions being same size as destinations.");
        }
        for( int index = 0; index < resolutions.size(); index += 1 ){
            toPNG(resolutions.get(index), destinations.get(index));
        }
    }

    public void toICO(List<String> resolutions, List<String> destinations) {
        if( resolutions.size() != destinations.size() ){
            throw new GradleException("It is required to have resolutions being same size as destinations.");
        }
        for( int index = 0; index < resolutions.size(); index += 1 ){
            toICO(resolutions.get(index), destinations.get(index));
        }
    }

    public void toBMP(List<String> resolutions, List<String> destinations) {
        if( resolutions.size() != destinations.size() ){
            throw new GradleException("It is required to have resolutions being same size as destinations.");
        }
        for( int index = 0; index < resolutions.size(); index += 1 ){
            toBMP(resolutions.get(index), destinations.get(index));
        }
    }

    public void toICNS(List<String> resolutions, List<String> destinations) {
        if( resolutions.size() != destinations.size() ){
            throw new GradleException("It is required to have resolutions being same size as destinations.");
        }
        for( int index = 0; index < resolutions.size(); index += 1 ){
            toICNS(resolutions.get(index), destinations.get(index));
        }
    }
}
