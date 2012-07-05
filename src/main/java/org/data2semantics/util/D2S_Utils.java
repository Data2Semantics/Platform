package org.data2semantics.util;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

public class D2S_Utils {

		public static HashMap<String, String> loadSourceMap( String sourceFile){
			
			Repository repo = new SailRepository(new MemoryStore());
			HashMap<String, String> cacheNameToSourceMap = new HashMap<String, String>();
			
			try {
				repo.initialize();
				RepositoryConnection conn = repo.getConnection();
				
				conn.add(new File(sourceFile), "", RDFFormat.TURTLE);
				RepositoryResult<Statement> results = conn.getStatements(null, null, null, true);
				while(results.hasNext()){
					Statement curStatement = results.next();
					String origSource = curStatement.getSubject().toString();
					String localName = curStatement.getObject().toString().replaceAll("\"", "");
					cacheNameToSourceMap.put(localName,  origSource);
					

				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
			return cacheNameToSourceMap;
		}
		
		public static Resource getLatest(RepositoryConnection con, RepositoryResult<Statement> statementIterator, URI timeProperty) throws RepositoryException {
			Resource latestResource = null;
			SimpleDateFormat sdf = D2S_Utils.getSimpleDateFormat();
			
			while (statementIterator.hasNext()) {
				Statement s = statementIterator.next();
				Resource r = (Resource) s
						.getObject();

				RepositoryResult<Statement> timeIterator = con
						.getStatements(r,
								timeProperty, null, true);

				Date latest = null;
				while (timeIterator.hasNext()) {
					Statement timeStatement = timeIterator
							.next();

					String cacheTime = timeStatement.getObject()
							.stringValue();

					Date time;

					try {
						time = sdf.parse(cacheTime);

						if (latest == null) {
							latest = time;
							latestResource = r;

						} else if (time.after(latest)) {
							latest = time;
							latestResource = r;
						}
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}	
				
				
			}
			


			
			return latestResource ;
		}
		
		public static SimpleDateFormat getSimpleDateFormat(){
			SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			return simpleDateFormat;
		}

		public static void sleep(int i) {
			try {
				Thread.sleep(i);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
}
