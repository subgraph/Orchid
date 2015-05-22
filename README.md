Orchid
======

The Orchid TOR library is a pure Java implementation of the TOR protocol.  This repository
was cloned from subgraph/Orchid.  The library also provides a built-in TOR-enabled proxy
server.  The remainder of this file describes using the library - see the javadocs for
details on integrating it with your Java program.

Usage
-----

Starting Orchid is pretty easy.  Using Java 1.5 or greater, simply point your browser (or
other HTTP client) to localhost:9150.  If you also want the dashboard to be exposed, launch
Orchid using the following command:

    java -Dcom.subgraph.orchid.dashboard.port=10000 -jar orchid-1.0.0.jar

Dashboard
---------

Viewing the dashboard can be accomplished with any "TCP listener".  From the CLI, the easiest
way to watch the dashboard is to use NetCat as follows:

    netcat localhost 10000
