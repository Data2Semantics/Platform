package org.data2semantics.modules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

	
	D2S_CallBioportal(String snapshotDirectory, String bioportalDirectory){
		SNAPSHOT_DIRECTORY = snapshotDirectory;
		BIOPORTAL_OUTPUT_DIRECTORY = bioportalDirectory;
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

	private void processHTMLFiles(String bioportalOutputDirectory, File[] xmlFiles) throws FileNotFoundException, InterruptedException {
		
		for (File currentHTMLFile : xmlFiles) {
			String outputName = currentHTMLFile.getName();
			outputName=outputName.replaceAll("html","xml");
			outputName="output-"+outputName;
			String outputFilePath = bioportalOutputDirectory+"\\"+outputName;
			
			// IF this file already created, move on.
			if(new File(outputFilePath).exists()) continue;
			
			Scanner fileScanner = new Scanner(new FileInputStream(currentHTMLFile));
			
			StringBuilder stringBuilder = new StringBuilder();
			D2S_BioportalClient client = new D2S_BioportalClient();
			
			try {
				System.out.println("Start writing"+outputFilePath);
				while (fileScanner.hasNextLine()) {
					stringBuilder.append(fileScanner.nextLine() + "\n");
				}
				String textToAnnotate = Jsoup.clean(stringBuilder.toString(), Whitelist.none());
				client.annotateToFile(textToAnnotate,"xml",new File(outputFilePath));
				System.out.println("Done writing"+outputFilePath);
			} finally {
				fileScanner.close();
			}
			Thread.sleep(20000);
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
			String outputFilePath = bioportalOutputDirectory + "\\output-"
					+ currentXMLFile.getName();

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

	public static void main(String[] args) {
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
