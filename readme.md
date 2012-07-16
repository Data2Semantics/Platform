Defines skeleton for modules and how they should interact within this platform. 

* A module brings together pieces of code that works together to perform a **task** in a workflow.
* Modules written in **Java** will share a common command line wrapper for ease of deployment
* When running as a service, modules will work together following a **blackboard** architecture (i.e. they will share a common triple store). 
* Modules have the following **input/output**:
	* i/o is RDF files when run as *program* 
	* i/o is a [Sail API](http://www.openrdf.org/doc/sesame2/system/ch05.html) reference to an RDF store when run as *module* inside a Java environment. 
* All modules will produce their own **provenance** information, and include it in their output.
