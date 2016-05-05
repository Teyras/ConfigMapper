---
geometry: margin=1in
...

# Use case analysis

There are two main approaches to working with configuration files - either the 
user edits the file directly, or the program features an interface that allows 
setting preferences in a more comfortable way.

The first approach only requires the library to read the configuration file. 
Writing functionality is however required by the second approach. It is 
important that the saved file is similar to the original. However, it is not 
necessary to keep its structure exactly the same (e.g. preserve variable 
substitutions) - when the configuration file is being handled by the program 
itself, it is seldom read by a human.

It is also a good practice to encapsulate configuration in an object that can be 
passed to classes that use it, instead of accessing configuration options 
directly from random places in the code.

# Specifying the configuration format

The configuration format is defined using standard Java classes. The fields that 
are supposed to contain options loaded from the configuration file are marked 
using annotations. Our library then fills these fields using the Reflection API 
(this is done by the `ConfigMapper` class). This approach has following 
advantages:

**Setting types and defaults naturally** - the type of an option is inferred 
from the type of the mapped field. Default values are the ones the fields are 
initialized with during the construction the configuration object (before 
mapping). Unfortunately, descriptions of options and sections have to be given 
as parameters to the annotations, because it's not possible to access comments 
with the Reflection API.

**Type safety** - it's possible to request a valid configuration object e.g. in 
a constructor. Also, errors such as accessing a nonexistent option are detected 
on compile time instead of run time.

**IDE autocompletion** - because the configuration is a regular object that has 
a class, it's easy for your IDE to autocomplete option names.

**Flexible access to options** - It's up to the programmers decision whether the 
options will be mapped to public fields, or private fields accessed indirectly 
using getters and setters. The latter also allows read-only options. Also, 
thanks to the annotations, we can change option names in the configuration file 
while the API of the configuration file stays the same.

**Possible GUI generation** - another library could use the annotations to 
generate configuration GUI automatically.

## Working with numeric types

The INI specification requires the implementation to support unsigned 64-bit 
integers. This is problematic, as Java doesn't have unsigned types. The library 
supports marking fields with the `NumericValue` annotation that (also) has the 
`unsigned` option. Unsigned values that do not fit in the `long` type are not 
permitted - it is very improbable that a programmer would need to have such a 
large number in a configuration file.

It is up to the programmers decision whether to use `int` or `long` as the 
option value type. If the number in the configuration file cannot fit in the 
specified type, an exception is thrown when the file is loaded.

## Working with enumeration types

We decided to map enumerations to the Java `enum` type. This helps enforce type 
safety, as opposed to working with strings. On the other hand, there has to be a 
way to alias `enum` constants so that their names don't have to match the 
options in the configuration files. The `ConstantAlias` annotation facilitates 
that.

## The relaxed loading mode

Fields that don't have any declaration cannot be mapped to class attributes.
The library provides the `UndeclaredOptions` annotation that allows storing
undeclared options as a key-value map in the annotated field. Because the 
type of these options is unknown, their values are stored as strings.

# The `ConfigMapper` class

The `load` and `save` methods work with a tree structure based on the 
`ConfigNode` class. This structure is an intermediate representation of the 
configuration file. Such design helps to separate the process of actually 
mapping the configuration to objects from parsing an INI file.

The loaded configuration is mapped onto a newly created instance of the mapped 
class. This gives the library better control of the object and also enforces 
better design.

# Working with INI files

INI files are parsed and written by the `IniAdapter` class, an implementation of 
the `ConfigAdapter` interface. This makes it possible to support other 
configuration file formats in the future, such as YAML or JSON.

## Dealing with unexpected situations

This section refers to the INIAdapter as well as the expected behavior of other
custom `ConfigAdapter` interface implementations.

### Reading

### Writing

When writing to a stream, two types of Exceptions are thrown:
* ConfigurationException when the supplied configuration does not have a format that
 is compatible with the specification of the configuration file format (for ini files this
 happens either when a Section node has another section node as a child, or when there are
 not only sections amongst children of the Root node).
* IOException when there is something wrong with writing to the output stream

# The `ConfigFacade` class

The mechanism of mapping configuration files to objects and back is rather 
complicated. The purpose of `ConfigFacade` is to present a simple API to the 
programmer, so that he doesn't have to worry about internal objects such as 
`ConfigNode`. It also provides some overloads (loading configuration from File 
objects instead of a FileStream etc.), so that they don't obscure the API of the 
"internal" classes.
