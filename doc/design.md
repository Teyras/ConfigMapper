# Specifying the configuration format

The configuration format is defined using standard Java classes. The fields that 
are supposed to contain options loaded from the configuration file are marked 
using annotations. Our library then fills these fields using the Reflection API. 
This approach has following advantages:

**Setting types and defaults naturally** - the type of an option is inferred 
from the type of the mapped object. Default values are the ones the fields are 
initialized with during the construction the configuration object (before 
mapping).

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

**Possible GUI generation**
