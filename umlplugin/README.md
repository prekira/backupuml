# umlplugin

Upon running mvn site, generates png of uml diagram using DtoMap of project.

Future: fix bug to render on html of mvn site (issue with current version of mvn site reporting plugins)

Add this to pom.xml to use plugin:

To dependencies:
<dependency>
   <groupId>net.sourceforge.plantuml</groupId>
   <artifactId>plantuml</artifactId>
   <version>8059</version>
</dependency>

_________________________________
To plugins:

     <plugin>
       <groupId>com.prekiraUml</groupId>
          <artifactId>sample</artifactId>
        <version>0.0.1-SNAPSHOT</version>
         <configuration>
           <aggregate>true</aggregate>
         </configuration>
         <executions>
           <execution>
             <phase>install</phase>
             <goals>
               <goal>report</goal>
             </goals>
           </execution>
         </executions>
     </plugin>





Add this for mvn site to work:

<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-site-plugin</artifactId>
  <version>3.7.1</version>
</plugin>
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-project-info-reports-plugin</artifactId>
  <version>3.0.0</version>
</plugin>



Add to pom.xml for tests to show up on mvn site

<reporting>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-report-plugin</artifactId>
        <version>2.18</version>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jxr-plugin</artifactId>
        <version>2.3</version>
      </plugin>
    </plugins>
  </reporting>
