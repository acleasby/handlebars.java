/**
 * Copyright (c) 2012-2015 Edgar Espina
 *
 * This file is part of Handlebars.java.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * This copy of Woodstox XML processor is licensed under the
 * Apache (Software) License, version 2.0 ("the License").
 * See the License for details about distribution rights, and the
 * specific rights regarding derivate works.
 *
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing Woodstox, in file "ASL2.0", under the same directory
 * as this file.
 */
package com.github.jknack.handlebars.maven;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * Created by acleasby on 4/14/15.
 */
@Mojo(name = "cleanTemplates", defaultPhase = LifecyclePhase.CLEAN, threadSafe = true)
public class CleanTemplatesPlugin extends HandlebarsPlugin {
    /**
     * The output file.
     */
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}/js/helpers.js")
    private String output;

    @Override
    protected void doExecute() throws Exception {
        File outputFile = new File(output);
        if (outputFile.exists()) {
            getLog().info("Removing output file: " + output);
            outputFile.delete();
        }
    }
}
