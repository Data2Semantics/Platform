package org.data2semantics.modules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

/**
 * This is the class responsible for getting local snapshot/files from original
 * html sources Assumes an existing source.txt files containing <local name>
 * <remote url> on every line
 * 
 * @author wibisono
 * 
 */
public class D2S_CreateSnapshot {

	// List of local files and urls from which snapshot will be created
	String SNAPSHOT_DIRECTORY = "results/snapshots";
	HashMap<String, String> sourceMap = new HashMap<String, String>();

	/**
	 * 
	 * @param sourcePath
	 *            the path to source files containing space/tab separated local
	 *            snapshot file + url Result will be generated in default
	 *            snapshot directory.
	 */

	public D2S_CreateSnapshot(String sourcePath) {
		loadSourceFile(sourcePath);
	}

	/**
	 * 
	 * @param sourcePath
	 *            the path to source files containing space/tab separated local
	 *            snapshot file + url
	 * @param snapshotDirectory
	 *            is where the snapshot directory will be generated. By default
	 *            is in results/snapshots
	 */
	public D2S_CreateSnapshot(String sourcePath, String snapshotDirectory) {

		this.SNAPSHOT_DIRECTORY = snapshotDirectory;
		loadSourceFile(sourcePath);

	}

	private void loadSourceFile(String sourceFile) {
		Yaml loader = new Yaml();

		try {
			sourceMap = (HashMap<String, String>) loader.load(new FileInputStream(sourceFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Generating snapshot using apache commons-io copyURLToFile
	 */
	public void generateSnapshot() {
		File resultDir = new File(SNAPSHOT_DIRECTORY);
		if (!resultDir.exists()) {
			resultDir.mkdirs();
		}
		for (String localFileName : sourceMap.keySet()) {

			try {
				URL sourceURL = new URL(sourceMap.get(localFileName));
				String suffix = getFileSuffixBasedOnContentType(sourceURL);

				File localFile = new File(resultDir, localFileName + "."
						+ suffix);
				// not recreating existing files
				if (localFile.exists())
					continue;

				System.out.println("Creating snapshot for " + localFile
						+ " from " + sourceURL);
				FileUtils.copyURLToFile(sourceURL, localFile);

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private String getFileSuffixBasedOnContentType(URL url) {
		HttpURLConnection urlc;
		try {
			urlc = (HttpURLConnection) url.openConnection();
			urlc.setAllowUserInteraction(false);
			urlc.setDoInput(true);
			urlc.setDoOutput(false);
			urlc.setUseCaches(true);
			urlc.setRequestMethod("HEAD");
			urlc.connect();
			String mime = urlc.getContentType();

			if (mime != null && mime.contains("xml")) // Either text/xml or application/xml
				return "xml";
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "html";
	}

	/**
	 * Source file argument actually provided by maven pom, profile. It will
	 * actually be the file on src/main/resources/sources.txt Leave it as
	 * parameter here in case we wanted to change, just change the pom.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out
					.println("\nPlease supplly the \n[1] path to source file (localFile, remote URL in YAML format"
							+ "\n[2] snapshot directory to be generated");
			return;
		}

		String sourceFile = args[0];
		String snapshotDirectory = args[1];
		System.out.println("Creating snapshots using source file from : "
				+ sourceFile + " into directory: " + snapshotDirectory);

		D2S_CreateSnapshot snapshot = new D2S_CreateSnapshot(sourceFile,
				snapshotDirectory);
		snapshot.generateSnapshot();

	}

}
