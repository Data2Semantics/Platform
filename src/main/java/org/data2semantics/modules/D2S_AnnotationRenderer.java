package org.data2semantics.modules;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.data2semantics.recognize.D2S_Annotation;
import org.data2semantics.recognize.D2S_AnnotationWriter;
import org.data2semantics.recognize.D2S_BioPortalAnnotationHandler;
import org.data2semantics.recognize.D2S_OpenAnnotationWriter;
import org.data2semantics.util.Vocab;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



public class D2S_AnnotationRenderer extends AbstractModule {
	
	private Logger log = LoggerFactory.getLogger(D2S_AnnotationRenderer.class);
	
	HashMap<String, String> originalFileSources = new HashMap<String, String>();
	String bioportalResultDir, outputFile, sourceFile, type, annotationTimestamp;
	
	Vocab vocab;
	String DEFAULT_ANNOTATION_TYPE = "http://www.w3.org/ns/openannotation/core/";
	String ANNOTATION_TYPE;
	
	public D2S_AnnotationRenderer(Repository repo, URI graph, URI resource) {
		super(repo, graph, resource);
		
		this.vocab = new Vocab(repo.getValueFactory());
		
		try {
			RepositoryConnection con = repo.getConnection();

			try {
				RepositoryResult<Statement> annotationTypeIterator = con
						.getStatements(resource,
								vocab.d2s("annotationType"), null, true);
				// Set bioportal_dir to the default value
				String annotationType = DEFAULT_ANNOTATION_TYPE;

				while (annotationTypeIterator.hasNext()) {
					Statement s = annotationTypeIterator.next();

					annotationType = s.getObject().stringValue();

					// We only need one bioportal_dir location
					break;
				}

				ANNOTATION_TYPE = annotationType;

				log.info("Annotation type will be <" + ANNOTATION_TYPE + ">");

			} finally {
				con.close();
			}

		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	
	public Repository start() {

		try {
			RepositoryConnection con = repo.getConnection();


			try {
				RepositoryResult<Statement> documentIterator = con
						.getStatements(resource, vocab.d2s("resource"), null,
								true);

				while (documentIterator.hasNext()) {
					Statement docStatement = documentIterator.next();
					URI documentURI = (URI) docStatement.getObject();
					log.info("Converting annotations of document "	+ documentURI.stringValue());

					if (ANNOTATION_TYPE.equals("http://www.w3.org/ns/openannotation/core/")) {
						
						D2S_AnnotationWriter writer = new D2S_OpenAnnotationWriter(con, documentURI);
						
						if (writer.hasAnnotations()) {
							log.info("Starting renderer...");
							render(writer);
						}
					} else {
						
						log.error("No compatible annotation type specified!");
						
					}

				}

			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				con.close();
			}

		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return repo;
	}
	

    public void render(D2S_AnnotationWriter writer) throws SAXException, IOException, ParserConfigurationException{
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		
		
		D2S_BioPortalAnnotationHandler bioPortalAnnotationSAXHandler;
		SAXParser parser ;
		Reader annotationReader ;
		InputSource is; 
		
		String annotationFileName = writer.getAnnotationFileName();
		String annotationSourceLocation = writer.getAnnotationSourceLocation();
		String documentURI = writer.getDocumentURI().stringValue();
		
		
		log.info("Reading annotations from file "+annotationFileName);
		
		File annotationFile = new File(annotationFileName);
		parser = saxParserFactory.newSAXParser();
		annotationReader = new InputStreamReader(new FileInputStream(annotationFile),"UTF-8");
		is = new InputSource(annotationReader);
		is.setEncoding("UTF-8");
		
		bioPortalAnnotationSAXHandler = new D2S_BioPortalAnnotationHandler(annotationSourceLocation, documentURI, writer);
		parser.parse(is, bioPortalAnnotationSAXHandler);

// Bioportal SAX handler now have access to writer, as it supposed to be instead of collection annotations in memory first.
//		currentAnnotations = bioPortalAnnotationSAXHandler.getAnnotations();
//		for(D2S_Annotation currentAnnotation : currentAnnotations)
//			writer.addAnnotation(currentAnnotation);

	}
	

}
