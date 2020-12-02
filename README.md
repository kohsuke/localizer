
# What is this?

This small tool reads your property files and generate Java classes that enables type-safe access to message resources.

For example, when you have a property file called ````org/acme/Messages.properties```` that looks like this:

````
foo=error at {0} with {1}
````

This tool generates the following ````org/acme/Messages.java````:
````
public class Messages {  
     
   private final static ResourceBundleHolder holder = new ResourceBundleHolder(Messages.class);
   
  /**  
    * error at {0} with {1}  
    */  
  public static String foo(Object arg1, Object arg2) {  
      return holder.format("foo",arg1,arg2);  
  }  
     
   /**  
     * error at {0} with {1}  
     */  
  public static Localizable _foo(Object arg1, Object arg2) {  
      return new Localizable(holder, "foo", arg1, arg2);  
  }  
}
````

The first method formats the message by using the default locale, and the second method returns an object that can be later formatted into ````String```` by specifying Locale.

In this way, you can get auto-completion on choosing the right message, you'll never refer to a non-existent message, and you'll always use the right number of arguments.

# How to use this?
## Maven
For projects built with Maven, add the following entries to your POM.

For the list of configurations to the ````localizer-maven-plugin````, refer to [this document](https://github.com/AODocs/localizer/blob/master/maven-plugin/src/main/java/org/jvnet/localizer/GeneratorMojo.java):
````
<build>
    ...
    <plugins>
        ...
        <plugin>
            <groupId>org.jvnet.localizer</groupId>
            <artifactId>localizer-maven-plugin</artifactId>
            <version>1.28</version>
            <executions>
                <execution>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
<dependencies>
    <dependency>
        <groupId>org.jvnet.localizer</groupId>
        <artifactId>localizer</artifactId>
        <version>1.28</version>
    </dependency>
    ...
</dependencies>
<repositories>
    <repository>
        <id>jenkins-repo</id>
        <url>https://repo.jenkins-ci.org/releases/</url>
    </repository>
    ...
</repositories>
<pluginRepositories>
    <pluginRepository>
        <id>jenkins-repo</id>
        <url>https://repo.jenkins-ci.org/releases/</url>
    </pluginRepository>
</pluginRepositories>
````

## Ant
For projects built with Ant, use the following task to generate source files:

```` 
<taskdef name="localizer-gen" classname="org.jvnet.localizer.GeneratorTask">
    <classpath>
        <pathelement location="path/to/localizer-maven-plugin.jar"/>
        <pathelement location="path/to/localizer.jar"/>
    </classpath>
</taskdef>
<localizer-gen todir="build/geenrated-sources" dir="./resources">
    <include name="**/Messages.properties"/>
</localizer-gen>
```` 

The ````localizer-gen```` task is a [matching task](http://ant.apache.org/manual/dirtasks.html), so you can use the usual FileSet-based filtering technique to specify the property files to be processed.

# Using LocaleProvider

When you use methods that return ````String````, the implementation consults a singleton ````LocaleProvider```` for determining the locale to be used.

The default implementation simply returns ````Locale.getDefault()````, but in other situations (for example in web apps), you can have this method return different locales (for example by using ````ServletRequest.getLocale()````.)