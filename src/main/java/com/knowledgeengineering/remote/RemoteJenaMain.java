package com.knowledgeengineering.remote;

// IO
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

// Jena
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class RemoteJenaMain {
	
	private final static String SERVER_URL = "http://localhost:8089/parliament/sparql";
	private final static String queryDir = "query";
	
	public static void main(String[] args) {
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

		// Get the selected query ID from the front end
		int queryId = getQueryIdFromFrontend();
		
		// Read in the query string for the received query ID
		String query= readQuery(queryId);
		
		// Get the parameters that are returned from the query
		String[] selectParams = getQuerySelectParams(query); 
		
		
		// Testing
		System.out.println(query);
		for(String param : selectParams) {
			System.out.println("Param: " + param);
		}
		
		
		// Send the query to Parliament and get the results
		System.out.println("Sending query to Parliament...");
		String[] queryResults = issueSelectQuery(query, selectParams);
		
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

	private static String[] getQuerySelectParams(String queryText) {		
		String selectParams = queryText.split("SELECT")[1];
		selectParams = selectParams.split("WHERE")[0];
		return selectParams.trim().replaceAll("\\?", "").split(" ");		
	}
	
	private static String[] issueSelectQuery(String query, String[] selectParams) {
		QueryExecution exec = QueryExecutionFactory.sparqlService(SERVER_URL, query);
		ResultSet rs = exec.execSelect();
		
		ArrayList<String> queryResults = new ArrayList<String>();
		String result = "";
		
		while(rs.hasNext()) {
			// Get current query result set
		    QuerySolution qs = rs.next() ;
		    
		    // For each parameter to the query SELECT, retrieve the returned value for that parameter
		    for(String param : selectParams) {
		    	result = result + qs.getLiteral(param).toString() + " | ";
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
