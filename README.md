#What is Orchid?
Orchid is a Tor client implementation and library written in pure Java.

It was written from the Tor specification documents, which are available here.

Orchid runs on Java 5+ and the Android devices.

Version 1.0 was released on November 27, 2013 and announced at the Minga por la Libertad Tecnologica.

#How can Orchid be used?
In a basic use case, running Orchid will open a SOCKS5 listener which can be used as a standalone client where Tor would otherwise be used.

Orchid can also be used as a library by any application running on the JVM. This is what Orchid was really designed for and this is the recommended way to use it. Orchid can be used as a library in any Java application, or any application written in a language that compiles bytecode that will run on the Java virtual machine, e.g., JRuby, Clojure, Scala..

#Why was Orchid developed?
Orchid was developed for seamless integration of Tor into Java applications. The first application to have built-in Tor support is Martus, a human rights application developed by Benetech.

Another reason Orchid was developed was to work through and debug the Tor specification documents. Orchid was also created to provide a reference implementation in Java. This may be easier to understand for those who are unfamiliar with the C programming language. The implementation is also simpler because only the client has been implemented.

#Should Orchid be used with a regular browser for anonymous browsing?
Probably not. We recommend that the Tor Browser Bundle (or better yet, Subgraph OS) be used, as there are privacy leaks through the browser that are unrelated to Tor. However, Orchid can be used with the Tor Browser bundle in the place of native Tor.

Orchid's strength is that it can be used to Torify Java and JVM applications with near transparency.

Orchid is licensed under a three-clause BSD license. Orchid flower image by craiglea used under CC.
