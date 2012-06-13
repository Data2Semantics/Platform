package org.data2semantics.modules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.data2semantics.recognize.D2S_Annotation;
import org.data2semantics.recognize.D2S_AnnotationOntologyWriter;
import org.data2semantics.recognize.D2S_BioPortalAnnotationHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class D2S_CreateAnnotationOntology {
	
	HashMap<String, String> originalFileSources = new HashMap<String, String>();
	String bioportalResultDir, outputFile, sourceFile;
	public D2S_CreateAnnotationOntology(String bioportalResultDir, String sourceFile, String outputFile){
	   initializeSourceFiles(sourceFile);
	   this.bioportalResultDir = bioportalResultDir;
	   this.sourceFile = sourceFile;
	   this.outputFile = outputFile;
	   
    }
    public void createAnnotationOntology() throws SAXException, IOException, ParserConfigurationException{
    	File processedDir = new File(bioportalResultDir);
    	
		File [] bpresults = processedDir.listFiles();
		
		D2S_AnnotationOntologyWriter aoWriter = new D2S_AnnotationOntologyWriter(outputFile);
		
		aoWriter.startWriting();
		aoWriter.addFileAndURLs(Arrays.asList(bpresults),originalFileSources);
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		
		
		D2S_BioPortalAnnotationHandler bioPortalAnnotationSAXHandler;
		SAXParser parser ;
		Reader reader ;
		InputSource is; 
		List<D2S_Annotation> currentAnnotations;
	
		
		
		for(File currentResultFile : bpresults){
			
			parser = saxParserFactory.newSAXParser();
			reader = new InputStreamReader(new FileInputStream(currentResultFile),"UTF-8");
			is = new InputSource(reader);
			is.setEncoding("UTF-8");
			
			String originalSource = originalFileSources.get(currentResultFile.getName());
			bioPortalAnnotationSAXHandler = new D2S_BioPortalAnnotationHandler(currentResultFile.getName(), originalSource);
			parser.parse(is, bioPortalAnnotationSAXHandler);
			
			currentAnnotations = bioPortalAnnotationSAXHandler.getAnnotations();
			for(D2S_Annotation currentAnnotation : currentAnnotations)
				aoWriter.addAnnotation(currentAnnotation);
			
		}
		
		aoWriter.stopWriting();
	}
	
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
		if(args.length < 3){
			System.out.println("\nThree arguments required: "+
								"\n[1] directory where bioportal annotation xml results resides, "+
								"\n[2] initial source file (local cache + original URL) list " +
								"\n[3] and the output file name ");
			return;
		}
		
		File bioportalResultDirectory = new File(args[0]);
		if (!bioportalResultDirectory.exists()) {
			System.out
					.println("Please run first mvn -P call-bioportal, since there is no snapshot to be processed");
			return;
		}
		
		D2S_CreateAnnotationOntology createAO = new D2S_CreateAnnotationOntology(args[0], args[1], args[2]);
		createAO.createAnnotationOntology();

   }
	
	// Load source file information into a hashmap, mapping file name to original URL
	// To be used when generation annotation ontology.
	// We will replace this with YAML when I am in the mood.
	private void initializeSourceFiles(String sourceFile) {
		File sourceList = new File(sourceFile);
		try {
			Scanner scanner = new Scanner(sourceList);
			while(scanner.hasNextLine()){
				String fileAndURL = scanner.nextLine();
				String fileURL[] = fileAndURL.split(" ");
				originalFileSources.put("output-"+fileURL[0]+".xml", fileURL[1]);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
