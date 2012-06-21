package org.data2semantics.modules;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.data2semantics.recognize.D2S_BioportalClient;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 * @author wibisono Class that will call the bioportal client, to provide
 *         annotations in xml format. Works on the snapshot that is already
 *         generated using D2S_CreateSnapshots.
 * 
 */
public class D2S_CallBioportal {

	String SNAPSHOT_DIRECTORY = "results/snapshots";
	String BIOPORTAL_OUTPUT_DIRECTORY = "results/bioportal";
	String snapshotTimestamp = "";

	
	D2S_CallBioportal(String snapshotDirectory, String bioportalDirectory){
		File snapDir = new File(snapshotDirectory);
		
		File[] timeDirs = snapDir.listFiles((FileFilter) DirectoryFileFilter.INSTANCE);
		
		Arrays.sort(timeDirs, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
		
		SNAPSHOT_DIRECTORY = timeDirs[0].getPath();
		System.out.println(SNAPSHOT_DIRECTORY);
		
		snapshotTimestamp = SNAPSHOT_DIRECTORY.substring(SNAPSHOT_DIRECTORY.lastIndexOf('/') + 1 );
		
		// Make sure that we have a timestamp for the annotations
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		 
		String timestamp = sdf.format(new Date());
		
		BIOPORTAL_OUTPUT_DIRECTORY += "/" + timestamp;

		File bpDir = new File(BIOPORTAL_OUTPUT_DIRECTORY);
		if(!bpDir.exists()){
			bpDir.mkdirs();
		}
		
		try {
			FileWriter writer = new FileWriter(BIOPORTAL_OUTPUT_DIRECTORY + "/snapshotTimestamp" );
			writer.write(snapshotTimestamp);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Calling bioportal for files from directory: "+SNAPSHOT_DIRECTORY+" storing output into: "+BIOPORTAL_OUTPUT_DIRECTORY);
		
	}

	public void processSnapshot() throws SAXException, IOException, ParserConfigurationException, InterruptedException{
			processSnapshotUsingBioportal(SNAPSHOT_DIRECTORY, BIOPORTAL_OUTPUT_DIRECTORY);
	}

	private void processSnapshotUsingBioportal(String snapshotDir,
			String bioportalOutputDirectory) throws SAXException, IOException,
			ParserConfigurationException, InterruptedException {

		File snapshotDirectory = new File(snapshotDir);

		FilenameFilter xmlFileFilter = new FilenameFilter() {
			public boolean accept(File arg0, String name) {
				return name.endsWith("xml");
			}
		};

		FilenameFilter htmlFileFilter = new FilenameFilter() {
			public boolean accept(File arg0, String name) {
				return name.endsWith("html");
			}
		};

		File[] xmlFiles = snapshotDirectory.listFiles(xmlFileFilter);
		File[] htmlFiles = snapshotDirectory.listFiles(htmlFileFilter);

		processXMLFiles(bioportalOutputDirectory, xmlFiles);
		processHTMLFiles(bioportalOutputDirectory, htmlFiles);
	}

	private void processHTMLFiles(String bioportalOutputDirectory, File[] htmlFiles) throws FileNotFoundException, InterruptedException {
		
		for (File currentHTMLFile : htmlFiles) {
			String outputName = currentHTMLFile.getName();
			outputName=outputName.replaceAll("html","xml");
			String outputFilePath = bioportalOutputDirectory+"/"+outputName;
			
			// IF this file already created, move on.
			if(new File(outputFilePath).exists()) continue;
			
			Scanner fileScanner = new Scanner(new FileInputStream(currentHTMLFile));
			
			StringBuilder stringBuilder = new StringBuilder();
			D2S_BioportalClient client = new D2S_BioportalClient();
			
			try {
				System.out.println("Start writing "+outputFilePath);
				while (fileScanner.hasNextLine()) {
					stringBuilder.append(fileScanner.nextLine() + "\n");
				}
				String textToAnnotate = Jsoup.clean(stringBuilder.toString(), Whitelist.none());
				client.annotateToFile(textToAnnotate,"xml",new File(outputFilePath));
				System.out.println("Done writing "+outputFilePath);
			} finally {
				fileScanner.close();
			}
			System.out.println("Going to sleep for a while... (20 secs)");
			Thread.sleep(20000);
			System.out.println("Back to work!");
		}
	}

	/**
	 * Processing xml files, getting from the body part to be annotated using bioportal.
	 * */
	private void processXMLFiles(String bioportalOutputDirectory, File[] xmlFiles)
			throws ParserConfigurationException, SAXException, IOException,
			InterruptedException {

		for (File currentXMLFile : xmlFiles) {

			// Append output- to the content file name
			String outputFilePath = bioportalOutputDirectory + "/" + currentXMLFile.getName();

			// IF this file already created, move on.
			if (new File(outputFilePath).exists())
				continue;

			DocumentBuilderFactory builderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document doc = builder.parse(currentXMLFile);

			// We are annotating only the body part
			NodeList list = doc.getElementsByTagName("body");
			Element el = (Element) list.item(0);
			String textContent = el.getTextContent();

			FileWriter writer = new FileWriter(outputFilePath);
			D2S_BioportalClient client = new D2S_BioportalClient();
			String annotationResult = client.annotateText(textContent, "xml");
			writer.write(annotationResult);
			writer.close();

			Thread.sleep(20000);
		}
	}

	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, InterruptedException {
		if (args.length < 2) {
			System.out
					.println("Please provide the directory to read the snapshot of files to be annotated, and the output directory");
			return;
		}

		File snapshotDirectory = new File(args[0]);
		if (!snapshotDirectory.exists()) {
			System.out
					.println("Please run first mvn -P create-snapshot, since there is no snapshot to be processed");
			return;
		}
		
		File bioportalDirectory = new File(args[1]);
		if(!bioportalDirectory.exists()){
			bioportalDirectory.mkdirs();
		}
		
		D2S_CallBioportal bioportalCaller = new D2S_CallBioportal(args[0], args[1]);
		bioportalCaller.processSnapshot();

	}
}
