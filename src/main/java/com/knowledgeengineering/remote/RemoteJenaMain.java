package com.knowledgeengineering.remote;

// IO
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

// Jena
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;

public class RemoteJenaMain {
	
	private final static String SERVER_URL = "http://localhost:8089/parliament/sparql";
	private final static String queryDir = "query";
	
	public static void main(String[] args) {
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

		// Get the selected query ID from the front end
		int queryId = getQueryIdFromFrontend();
		
		// Read in the query string for the received query ID
		String query= readQuery(queryId);
		
		System.out.println(query);
		
		// Send the query to Parliament and get the results
		System.out.println("Sending query to Parliament...");
		String[] queryResults = issueSelectQuery(query);
		if(queryResults == null) {
			System.err.println("A query result is not a resource or literal, exiting...");
			System.exit(2);
		}
		
		// Send the results back to the front end
		System.out.println("Sending query results to front end...");
		sendQueryResults(queryResults);
		
	}
	
	private static int getQueryIdFromFrontend() {
		// TODO: get request from front end
		return 1;
	}
	
	private static void sendQueryResults(String[] queryResults) {
		// TODO: send result to front end		
	}
	
	private static String[] issueSelectQuery(String query) {
		QueryExecution exec = QueryExecutionFactory.sparqlService(SERVER_URL, query);
		ResultSet rs = exec.execSelect();
		
		ArrayList<String> queryResults = new ArrayList<String>();
		String result = "";
		
		while(rs.hasNext()) {
			// Get current query result set
		    QuerySolution qs = rs.next() ;
		    
		    // Get column names from SPARQL SELECT
	    	Iterator<String> columns = qs.varNames();
	    	
	    	// For the current row, get the value at each column (resource or literal)
	    	while(columns.hasNext()) {
		    	String column = columns.next();
		    	//System.out.println("column: " + column);	
		    	
		    	RDFNode node = qs.get(column);
		    	if(node.isLiteral()) {
		    		result = result + qs.getLiteral(column).toString() + " | ";
		    	} else if(node.isResource()){
		    		result = result + qs.getResource(column).toString() + " | ";	
		    	} else {
		    		return null;
		    	}	
	    	}
	    	
		    System.out.println(result);
		    queryResults.add(result);
		    result = "";
		}
		exec.close() ;

		return queryResults.toArray(new String[0]);
	}
	
	private static String readQuery(int queryId) {
		String query = queryDir + "\\" + String.valueOf(queryId) + ".txt";
		final Path queryPath = Paths.get(query);
		if (!Files.isReadable(queryPath)) {
			System.out.println(queryPath.toAbsolutePath() + " does not exist or is not readable, please check the path");
			System.exit(1);
		}
		
		byte[] fileBytes = null;
		try {
			fileBytes = Files.readAllBytes(queryPath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		return new String(fileBytes);
	}

}
