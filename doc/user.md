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

In the example above, the `ConfigFacade` is typed (TODO: typed neni spravny slovo!!) 
with the `BasicMappedClass`, thus defining the documentation format. If the loading mode
is set to `RELAXED`, the mapped class has to have a property (TODO: map type?) annotated `@UndeclaredOptions`,
where the options which do not match any of the classes properties, but appear in the 
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