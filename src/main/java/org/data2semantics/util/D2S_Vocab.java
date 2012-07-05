package org.data2semantics.util;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.URI;

public class D2S_Vocab {

	
	private ValueFactory vf;
	
	public D2S_Vocab(ValueFactory f){
		this.vf = f;
	}
	
	
	/*
	 * The Annotation Ontology namespaces
	 */
	
	public final String AO = "http://purl.org/ao/";
	
	public final String AO_CORE = AO + "core#";

	public final String AO_FOAF = AO + "foaf#";

	public final String AO_TYPE = AO + "types#";
	
	public final String AO_SELECTOR = AO + "selectors#";
	
	
	public URI ao(String suffix) {
		return vf.createURI(AO_CORE + suffix);
	}
	
	public URI aof(String suffix) {
		return vf.createURI(AO_FOAF + suffix);
	}
	
	public URI aot(String suffix) {
		return vf.createURI(AO_TYPE + suffix);
	}
	
	public URI aos(String suffix) {
		return vf.createURI(AO_SELECTOR + suffix);
	}
	
	
	/*
	 * The Open Annotation namespaces
	 */
	
	public final String OA = "http://www.w3.org/ns/openannotation/core/";
	
	public final String OAX = "http://www.w3.org/ns/openannotation/extension/";
	
	
	public URI oa(String suffix) {
		return vf.createURI(OA + suffix);
	}
	
	public URI oax(String suffix) {
		return vf.createURI(OAX + suffix);
	}	
	

	
	/*
	 * Various namespaces
	 */
	
	public final String ANNOTEA = "http://www.w3.org/2000/10/annotation-ns#";
	
	public final String FOAF = "http://xmlns.com/foaf/0.1#";

	public final String PAV = "http://purl.org/pav/";

	public final String OBO = "http://purl.obolibrary.org/obo#";
	
	public final String XSD = "http://www.w3.org/TR/xmlschema-2/#";
	
	public URI annotea(String suffix) {
		return vf.createURI(ANNOTEA + suffix);
	}
	
	public URI pav(String suffix) {
		return vf.createURI(PAV + suffix);
	}
	public URI foaf(String suffix) {
		return vf.createURI(FOAF + suffix);
	}
	public URI obo(String suffix) {
		return vf.createURI(OBO + suffix);
	}
	public URI xsd(String suffix) {
		return vf.createURI(XSD + suffix);
	}
	
	public URI d2s(String suffix){
		return vf.createURI(D2S + suffix);
	}
	
	/* 
	 * The SKOS namespace
	 */
	
	public final String SKOS = "http://www.w3.org/2004/02/skos/core#";
	
	public URI skos(String suffix) {
		return vf.createURI(SKOS + suffix);
	}
	
	/*
	 * The D2S namespace
	 */
	
	public final String D2S = "http://aers.data2semantics.org/resource/";
	
	public URI doc(String suffix){
		return d2s("document/"+suffix);
	}
	
	public URI selector(String suffix){
		return d2s("selector/"+suffix);
	}	
	
	public URI annotation(String suffix){
		return d2s("annotation/"+suffix);
	}
	
	public org.openrdf.model.URI state(String suffix){
		return d2s("state/"+suffix);
	}
	
	public URI target(String suffix){
		return d2s("target/"+suffix);
	}
	
	public URI annotator(String suffix){
		return d2s("D2SAnnotator/"+suffix);
	}


	
}
