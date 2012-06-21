package org.data2semantics.modules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.data2semantics.recognize.D2S_Annotation;
import org.data2semantics.recognize.D2S_AnnotationOntologyWriter;
import org.data2semantics.recognize.D2S_AnnotationWriter;
import org.data2semantics.recognize.D2S_BioPortalAnnotationHandler;
import org.data2semantics.recognize.D2S_OpenAnnotationWriter;
import org.data2semantics.util.D2S_Utils;
import org.openrdf.repository.RepositoryException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



public class D2S_AnnotationRenderer {
	
	HashMap<String, String> originalFileSources = new HashMap<String, String>();
	String bioportalResultDir, outputFile, sourceFile, type, annotationTimestamp;
	
	public D2S_AnnotationRenderer(String bioportalResultDir, String sourceFile, String outputFile, String type){
	   
	   originalFileSources = D2S_Utils.loadSourceMap(sourceFile);
	   
	   File bpDir = new File(bioportalResultDir);
	   File[] bpDirs = bpDir.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
		
	   Arrays.sort(bpDirs, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
		
	   this.bioportalResultDir = bpDirs[0].getPath();
	   this.annotationTimestamp = this.bioportalResultDir.substring(this.bioportalResultDir.lastIndexOf('/') + 1);
	   this.sourceFile = sourceFile;
	   this.outputFile = outputFile;
	   this.type = type;
	   
    }
    public void render() throws SAXException, IOException, ParserConfigurationException{
    	File processedDir = new File(bioportalResultDir);
    	
    	// Get all results from the bioPortalResultDir, but exclude the snapshotTimestamp
		File [] bpresults = processedDir.listFiles((FileFilter) FileFilterUtils.notFileFilter(FileFilterUtils.nameFileFilter("snapshotTimestamp")));
		
		
		D2S_AnnotationWriter writer ;
		
		if (type == "AO") {
			writer = new D2S_AnnotationOntologyWriter(outputFile);
			writer.startWriting();
			
			
			((D2S_AnnotationOntologyWriter) writer).addFileAndURLs(Arrays.asList(bpresults),originalFileSources);
		} else {
			try {
				FileReader reader = new FileReader(bioportalResultDir + "/snapshotTimestamp");
				BufferedReader br = new BufferedReader(reader); 
				String snapshotTimestamp; 
				snapshotTimestamp = br.readLine();
				
				writer = new D2S_OpenAnnotationWriter(outputFile, annotationTimestamp, snapshotTimestamp);
				writer.startWriting();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
		
		
		
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		
		
		D2S_BioPortalAnnotationHandler bioPortalAnnotationSAXHandler;
		SAXParser parser ;
		Reader reader ;
		InputSource is; 
		List<D2S_Annotation> currentAnnotations;
	
		
		
		for(File currentResultFile : bpresults){
			System.out.println("Current file: "+currentResultFile);
			
			parser = saxParserFactory.newSAXParser();
			reader = new InputStreamReader(new FileInputStream(currentResultFile),"UTF-8");
			is = new InputSource(reader);
			is.setEncoding("UTF-8");
			
			String currentResultFileName = currentResultFile.getName();
			
			// Strip the '.xml' of the currentResultFileName
			String currentResultFileNameBase = currentResultFileName.substring(0,currentResultFileName.lastIndexOf('.'));
			String originalSource = originalFileSources.get(currentResultFileNameBase);
			
			bioPortalAnnotationSAXHandler = new D2S_BioPortalAnnotationHandler(currentResultFile.getName(), originalSource);
			parser.parse(is, bioPortalAnnotationSAXHandler);
			
			currentAnnotations = bioPortalAnnotationSAXHandler.getAnnotations();
			for(D2S_Annotation currentAnnotation : currentAnnotations)
				writer.addAnnotation(currentAnnotation);
			
		}
		
		writer.stopWriting();
	}
	
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
		if(args.length < 4){
			System.out.println("\n Four arguments required: "+
								"\n[1] directory where bioportal annotation xml results resides, "+
								"\n[2] initial source file (local cache + original URL) list " +
								"\n[3] the output file name " +
								"\n[4] the type of ontology (AO or OA)");
			return;
		}
		
		File bioportalResultDirectory = new File(args[0]);
		if (!bioportalResultDirectory.exists()) {
			System.out
					.println("Please run first mvn -P call-bioportal, since there is no snapshot to be processed");
			return;
		}
		
		String type = args[3];
		if (type != "AO") {
			type = "OA";
		}
		System.out.println("Creating RDF of type "+args[3]+" based on bioportal output from: "+args[0]+" using source file: "+args[1]+" into directory : "+args[2]);
		D2S_AnnotationRenderer renderer = new D2S_AnnotationRenderer(args[0], args[1], args[2], type);
		renderer.render();

   }
	
	// Load source file information into a hashmap, mapping file name to original URL
	// To be used when generation annotation ontology.
	// We will replace this with YAML when I am in the mood.
//	private void initializeSourceFiles(String sourceFile) {
//	      	Yaml loader = new Yaml();
//			try {
//				originalFileSources  = (HashMap<String,String>) loader.load(new FileInputStream(sourceFile));
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			// Either appending here or on local name
//			HashMap<String,String> appendedFiles = new HashMap<String, String>();
//			for(String fileName : originalFileSources.keySet()){
//				appendedFiles.put("output-"+fileName+".xml", originalFileSources.get(fileName));
//			}
//			originalFileSources.putAll(appendedFiles);
//	}
	
	// Now reading from turtle format
}
