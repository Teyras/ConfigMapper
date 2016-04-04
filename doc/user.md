---
geometry: margin=1in
...

# ConfigMapper library users documentation

## Basic usage

The ConfigMapper library is used to load and store configuration in/from a local file. This is 
done using an instance of the `ConfigFacade` object, which shields the user from the 
complicated API of the underlying classes and instead provides a simple one. 

```java 
ConfigFacade<BasicMappedClass> facade = new ConfigFacade<>(BasicMappedClass.class, new IniAdapter());
BasicMappedClass mappedConfig = facade.load(input, LoadingMode.STRICT); 
//... More code, possibly modifying the mappedConfig object
facade.save(mappedConfig, output);
```

In the example above, the `ConfigFacade` is typed 
with the `BasicMappedClass`, thus defining the documentation format. If the loading mode
is set to `RELAXED`, the mapped class has to have a property that implements the map interface annotated 
`@UndeclaredOptions`, where the options which do not match any of the classes properties, but appear in the 
configuration file, are stored.

An instance
of the interface `ConfigAdapter` (the class `IniAdapter()` in particular) is provided, 
which specifies the format of the configuration file. This is the only implementation of the 
`ConfigAdapter` interface provided by this library (so far). 

To store the configuration in 
a file, the method `save` on the `facade` object is called. There also is a method 
for saving the default values, which does not consult any class instance, only the declaration.

## Defining configuration format

The configuration format is defined using standard Java classes that the user has to define
himself or herself. The class properties that the user wants to set using this library
have to be annotated by the `@ConfigOption` annotation. The type of an option is inferred 
from the type of the annotated property. The annotation is also used when setting the
section (`section` parameter) and the description (`description` parameter) of the option. 
Default values are defined by initializing the class properties.

Moreover numeric options can be restricted using the annotation `@NumericValue` by defining 
one of the parameters:
* `minimum` sets the numeric minimum of the expected value,
* `maximum` sets the maximum,
* boolean value `unsigned` defines, whether the value shall be only positive.

The library can also handle values specified by enumerated sets. This is done by declaring
a java `enum`. When profiting from the default behavior, the enumerated values are
mapped directly to values in the configuration file. If the user wants to change this, the
annotation `@ConstantAlias` is available taking two parameters. It maps the value of the 
`alias` parameter to the value of the `constant`. The `constant` should be specified 
unqualified (e.g. for mapping `Weekday.SUNDAY` the value `SUNDAY` shall be specified). 

## Use cases

The following section introduces a couple of examples on how to use this library.

### Basic example

Let's say we have a very simple `.ini` file (example bellow) and we just want to quickly parse its' elements
and continue programming. 

```
[network]
hostname = myserver.com
port = 3663

[logging]
debug = off
```

To parse the file above we have to create a config class with corresponding properties and pass it to the `ConfigFacade`
as a generic type. Then call the method load on the facade that will instantiate the properties of the config class.

```java
// Defining the mapped class
class BasicMappedClass {
	@ConfigOption(section = "network")
	private String hostname;

	@ConfigOption(section = "network")
	private int port;

	@ConfigOption(section = "logging")
	private boolean debug;
	
	public boolean getDebug(){
		return debug;
	}
	
	public int getPort(){
		return port;
	}
	
	public String getHostname(){
		return hostname;
	}
}

// instantiating the facade and loading the contents of the file
ConfigFacade<NestedSectionMappedClass> facade = new ConfigFacade<>(NestedSectionMappedClass.class, new IniAdapter());
BasicMappedClass config = new BasicMappedClass(); 
try (FileInputStream fis = new FileInputStream(new File("myIni.ini"))) {
	config = facade.load(input, LoadingMode.STRICT);
} catch (IOException e) {
	System.err.println(e.getStackTrace());
}

/*	
 * retrieving loaded options. This could be done depending on the user either using public properties as in this
 * example, or by getters and setters defined by the user in the mapped class 
 */
String hostname = config.getHostname(); 
//...
```

### Reusing a section definition

Thanks to the flexibility of object oriented programming, user can reuse a definition of a section as a template 
multiple times. Consult the following example.

```java
class NestedSectionMappedClass {
	class SectionWithOneStringOption {
		@ConfigOption
		private String stringOption;
		
		public String getStringOption() {
			return stringOption;
		}
	}

	@ConfigSection
	SectionWithOneStringOption sectionA;

	@ConfigSection
	SectionWithOneStringOption sectionB;
}

NestedSectionMappedClass config = new NestedSectionMappedClass();
// instantiating the facade and loading the contents of the file is done as in the example above so it is left out here

// retrieving the parameters 
String stringFromSectionA = config.sectionA.getStringOption();
String stringFromSectionB = config.sectionB.getStringOption();
``` 