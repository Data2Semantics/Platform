package org.data2semantics.modules;

import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.data2semantics.util.D2S_Utils;
import org.data2semantics.util.Vocab;
import org.openrdf.model.BNode;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the class responsible for getting local snapshot/files from original
 * html sources Assumes an existing source.txt files containing <local name>
 * <remote url> on every line
 * 
 * @author wibisono
 * 
 */
public class D2S_CreateSnapshot extends AbstractModule {

	private Logger log = LoggerFactory.getLogger(D2S_CreateSnapshot.class);
	
	// List of local files and urls from which snapshot will be created
	String DEFAULT_SNAPSHOT_DIRECTORY = "results/snapshots";
	String SNAPSHOT_DIRECTORY;
	String timestamp;
	HashMap<String, String> sourceMap = new HashMap<String, String>();
	String[] resources;
	Vocab vocab;
	
	public D2S_CreateSnapshot(Repository repo, URI graph, URI resource) {
		super(repo, graph, resource);
		
		SimpleDateFormat sdf = D2S_Utils.getSimpleDateFormat();
		timestamp = sdf.format(new Date());
		
		
		
		vocab = new Vocab(repo.getValueFactory());
		
		
		try {
			RepositoryConnection con = repo.getConnection();
			
			try {
				RepositoryResult<Statement> cacheIterator = con.getStatements(resource, vocab.d2s("cacheDirectory"), null, true);
				
				// Set cache to the default value
				String cache = DEFAULT_SNAPSHOT_DIRECTORY;

				while (cacheIterator.hasNext()) {
					Statement s = cacheIterator.next();
					
					cache = s.getObject().stringValue();	
					
					// We only need one cache location
					break;
				}
				
				SNAPSHOT_DIRECTORY = cache + "/" + timestamp;
				
			} finally {
				con.close();
			}
			
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	

	/**
	 * Generating snapshot using apache commons-io copyURLToFile
	 * @return 
	 */
	public Repository start() {
		File resultDir = new File(SNAPSHOT_DIRECTORY);
		if (!resultDir.exists()) {
			resultDir.mkdirs();
		}
		
		try {
			RepositoryConnection con = repo.getConnection();
			ValueFactory vf = repo.getValueFactory();
			
			try {
				RepositoryResult<Statement> resourceIterator = con.getStatements(resource, vocab.d2s("resource"), null, true);

				while (resourceIterator.hasNext()) {
					Statement s = resourceIterator.next();
					
					// Get the URI of the resource we want to cache
					URI resURI = (URI) s.getObject();
					// Turn it into a string
					String res = resURI.stringValue();	
					// Remove the 'http://' part of the URI
					String resFile = res.substring(7);
					
					try {
						URL sourceURL = new URL(res);
						String suffix = getFileSuffixBasedOnContentType(sourceURL);

						String cacheFileName = resultDir + "/" + URLEncoder.encode(resFile,"utf-8") + "."
								+ suffix;
						File localFile = new File(cacheFileName);
						
						// not recreating existing files
						if (localFile.exists())
							continue;

						log.info("Creating snapshot for " + localFile + " from " + sourceURL);
						FileUtils.copyURLToFile(sourceURL, localFile);
						log.info("Done");
						
						BNode cacheNode = vf.createBNode();
						
						Statement hasCacheStatement = vf.createStatement(resURI, vocab.d2s("hasCache"), cacheNode);
						
						Statement cacheLocationStatement = vf.createStatement(cacheNode, vocab.d2s("cacheLocation"), vf.createLiteral(cacheFileName, XMLSchema.STRING));
						Statement cacheTimeStatement = vf.createStatement(cacheNode, vocab.d2s("cacheTime"), vf.createLiteral(timestamp, XMLSchema.DATETIME));
						
						con.add(hasCacheStatement);
						con.add(cacheLocationStatement);
						con.add(cacheTimeStatement);

					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				

				
			} finally {
				con.close();
			}
			
		} catch (RepositoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		

		return repo;
	}

	private String getFileSuffixBasedOnContentType(URL url) {
		HttpURLConnection urlc;
		CookieHandler.setDefault(new CookieManager());
		
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
//	public static void main(String[] args) {
//		if (args.length < 2) {
//			System.out
//					.println("\nPlease supplly the \n[1] path to source file (localFile, remote URL in YAML format"
//							+ "\n[2] snapshot directory to be generated");
//			return;
//		}
//
//		String sourceFile = args[0];
//		String snapshotDirectory = args[1];
//		System.out.println("Creating snapshots using source file from : "
//				+ sourceFile + " into directory: " + snapshotDirectory);
//
//		D2S_CreateSnapshot snapshot = new D2S_CreateSnapshot(sourceFile,
//				snapshotDirectory);
//		snapshot.generateSnapshot();
//
//	}

}
