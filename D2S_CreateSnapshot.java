package org.data2semantics.modules;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


/**
 * This is the class responsible for getting local snapshot/files from original html sources
 * Assumes an existing source.txt files containing <local name> <remote url> on every line
 * @author wibisono
 *
 */
public class D2S_CreateSnapshot {
		
		// List of local files and urls from which snapshot will be created
		Vector<String[]> fileURLList = new Vector<String[]>();
		String SNAPSHOT_DIRECTORY ="results/snapshots";
		
		
		/**
		 * 
		 * @param sourcePath the path to source files containing space/tab separated local snapshot file + url
		 * Result will be generated in default snapshot directory.
		 */
		
		public D2S_CreateSnapshot(String sourcePath) {
			
			initializeFileURLList(new File(sourcePath));
		}
	
		/**
		 * 
		 * @param sourcePath the path to source files containing space/tab separated local snapshot file + url
		 * @param snapshotDirectory is where the snapshot directory will be generated. By default is in results/snapshots
		 */
		public D2S_CreateSnapshot(String sourcePath, String snapshotDirectory) {
			
			this.SNAPSHOT_DIRECTORY = snapshotDirectory;
			initializeFileURLList(new File(sourcePath));
			
		}

		/**
		 * Read from source file, split file and source URL into fileURLList
		 * @param sourceFile
		 */
		private void initializeFileURLList(File sourceFile) {
			try {
				Scanner scanner = new Scanner(sourceFile);
				while(scanner.hasNextLine()){
					String [] fileURL = scanner.nextLine().split(" ");
					fileURLList.add(fileURL);
				}
			} catch (FileNotFoundException e) {
				
				e.printStackTrace();
			}
		}
		
		public List<String[]> getFileList(){
			return fileURLList;
		}
		
		/**
		 * Generating snapshot using apache commons-io copyURLToFile
		 */
		public void generateSnapshot(){
			File resultDir =new File(SNAPSHOT_DIRECTORY);
			if(!resultDir.exists()){
				resultDir.mkdirs();
			}
			for(String[] fileURL : fileURLList){
				
				try {
				
					File localFile = new File(resultDir,fileURL[0]);
					URL  sourceURL = new URL(fileURL[1]);
					FileUtils.copyURLToFile(sourceURL, localFile);
				
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
	
			}
		}
		
		/**
		 * Source file argument actually provided by maven pom, profile.
		 * It will actually be the file on src/main/resources/sources.txt
		 * Leave it as parameter here in case we wanted to change, just change the pom.
		 * @param args
		 */
		public static void main(String[] args) {
			if(args.length < 1){
				System.out.println("Please supplly the path to source file that you wanted to process");
				return;
			}
			
			// If only the source file is supplied
			if(args.length == 1){
				String fileName = args[0];
				D2S_CreateSnapshot snapshot= new D2S_CreateSnapshot(fileName);
				snapshot.generateSnapshot();
			} else
			if(args.length == 2){
				String sourceFile = args[0];
				String snapshotDirectory = args[1];
				D2S_CreateSnapshot snapshot= new D2S_CreateSnapshot(sourceFile, snapshotDirectory);
				snapshot.generateSnapshot();
				
			}
			
		}
		
}
