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
		// int queryId = getQueryIdFromFrontend();
		// String params[] = getParamsFromFrontend();
		// Read in the query string for the received query ID
		String query = getQuery(1, null);

		System.out.println(query);

		// Send the query to Parliament and get the results
		System.out.println("Sending query to Parliament...");
		ArrayList<ArrayList<String>> queryResults = issueSelectQuery(query);
		if (queryResults == null) {
			System.err.println("A query result is not a resource or literal, exiting...");
			System.exit(2);
		}

		/*
		 * for(HashMap.Entry<String, ArrayList<String>> pair : queryResults.entrySet())
		 * { String columnName = pair.getKey(); System.out.println("Column: " +
		 * columnName); for(String result : pair.getValue()) {
		 * System.out.println(result); } }
		 */

		for (ArrayList<String> result : queryResults) {
			for (String item : result) {
				System.out.print(item + ", ");
			}
			System.out.println();
		}

		// Send the results back to the front end
		System.out.println("Sending query results to front end...");
		// sendQueryResults(queryResults);

	}

	private static int getQueryIdFromFrontend() {
		// TODO: get request from front end
		return 1;
	}

	private static String[] getParamsFromFrontend() {
		// TODO: get request from front end
		return new String[0];
	}

	private static void sendQueryResults(String[] queryResults) {
		// TODO: send result to front end
	}

	private static ArrayList<ArrayList<String>> issueSelectQuery(String query) {
		// Execute the SPARQL query and get the set of results
		QueryExecution exec = QueryExecutionFactory.sparqlService(SERVER_URL, query);
		ResultSet rs = exec.execSelect();

		// List of lists to store the results
		// Each inner list represents a row, containing the results for that row
		ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>>();

		while (rs.hasNext()) {
			// Get current query result set
			QuerySolution qs = rs.next();

			// Get column names from SPARQL SELECT
			Iterator<String> columns = qs.varNames();

			// Create a new list for the current result row
			ArrayList<String> result = new ArrayList<String>();

			// Get the value at each column (resource or literal) for the current row
			while (columns.hasNext()) {
				String column = columns.next();
				RDFNode node = qs.get(column);

				if (node.isLiteral()) {
					result.add(qs.getLiteral(column).toString());
				} else if (node.isResource()) {
					result.add(qs.getResource(column).toString());
				} else {
					return null;
				}
			}

			// Add the row's results to the total list of results
			results.add(result);
		}
		exec.close();

		return results;
	}

	private static String getQuery(int queryId, String params[]) {
		String query = queryDir + "\\" + String.valueOf(queryId) + ".sparql";
		final Path queryPath = Paths.get(query);
		if (!Files.isReadable(queryPath)) {
			System.out
					.println(queryPath.toAbsolutePath() + " does not exist or is not readable, please check the path");
			System.exit(1);
		}

		byte[] fileBytes = null;
		try {
			fileBytes = Files.readAllBytes(queryPath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String ret = new String(fileBytes);

		if (params != null) {
			// do the parameter replacement
			for (int i = 0; i < params.length; i++) {
				ret = ret.replace("%PARAM_" + i + "%", params[i]);
			}
		}

		return ret;
	}

}
