# ConfigMapper

ConfigMapper is a library that allows mapping configuration files to Java objects using annotations
and the reflection API. It also allows modifying and saving the configuration.

## Example

```
[network]
hostname = myserver.com
port = 3663

[logging]
debug = off
```

This INI file can be mapped onto a Java class defined as follows:

```java
class BasicMappedClass {
    // The description is optional and will be used as a comment if the configuration is saved to a file
	@ConfigOption(section = "network", description = "Host name of the server")
	private String hostname;

	@ConfigOption(section = "network")
	private int port;

	@ConfigOption(section = "logging")
	private boolean debug;
	
	public boolean getDebug() {
		return debug;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getHostname() {
		return hostname;
	}
}
```
