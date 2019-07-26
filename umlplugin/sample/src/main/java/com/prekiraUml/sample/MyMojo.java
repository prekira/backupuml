package com.prekiraUml.sample;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.model.Build;

import org.apache.maven.model.Model;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.util.Locale;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Goal which touches a timestamp file.
 * 
 * @param <MavenProject>
 *
 */
@Mojo(name = "report", defaultPhase=LifecyclePhase.SITE,
requiresDependencyResolution=ResolutionScope.TEST,
requiresDependencyCollection=ResolutionScope.TEST,
threadSafe=true
)
public class MyMojo<MavenProject> extends AbstractMojo {

    /**
     * The project directory
     */
    @Parameter(defaultValue="${project.build.outputDirectory}", readonly=false)
    private File basedir;
   
    @Parameter(defaultValue = "${project}", readonly = false)
    private MavenProject project;

    public Model model;
    public Build build;
    public File targetDir;

    public void execute() throws MojoExecutionException {
        
        getLog().info("Accessing project");
        if (project != null) {
            this.model = ((org.apache.maven.project.MavenProject) project).getModel();
            this.build = model.getBuild();
            this.targetDir = new File(build.getDirectory());
            try {
                //path to find dtomap 
                String path = this.build.getDirectory();
                String projectBaseDir  = basedir.getParent();
                String projectJsonPath =projectBaseDir.substring(0, path.indexOf("target"))+ "api/target/generated-sources/generated-dto/";
                
                App.print(projectJsonPath);
                //call 2x to prevent file finding errors
                //shell script in case you want to run any other commands
                App.createDiag(projectJsonPath);

                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command("callPlantUml.sh");
                getLog().info("RAN SHELL SCRIPT");
                App.createDiag(projectJsonPath);

                //TODO: attempt to add to mvn site html page
                String htmlPath =projectBaseDir.substring(0, path.indexOf("target"))+ "target/site/summary.html";                
                String appendHtml = "<img src=\"DtoMapPlantUml.png\">";

                

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            getLog().info("access failed");
        }
        
    }

    public String getOutputName() {
            // This report will generate simple-report.html when invoked in a project with `mvn site`
            return "simple-report";
    }

    public String getName(Locale locale) {
            // Name of the report when listed in the project-reports.html page of a project
            return "Simple Report";
    }

    public String getDescription(Locale locale) {
            // Description of the report when listed in the project-reports.html page of a project
            return "Reporting plugin that generates uml diagram from dtomap.json.";
    }
}
