package org.data2semantics.modules;

import org.openrdf.model.URI;
import org.openrdf.repository.Repository;

public abstract class AbstractModule {

	private Repository repo;
	private URI graph;
	private URI resource;
	
	public AbstractModule(Repository repo, URI graph, URI resource){
		this.repo = repo;
		this.graph = graph;
		this.resource = resource;
		
	}
	
	public abstract Repository start();

	
}
