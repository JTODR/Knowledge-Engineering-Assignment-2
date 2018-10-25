package com.knowledgeengineering.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class RemoteJenaMain {
	
	private final static String SERVER_URL = "http://localhost:8089/parliament/sparql";
	
	public static void main(String[] args) {
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

		String queryPath = "query/query.txt";

		final Path dataDir = Paths.get(queryPath);
		if (!Files.isReadable(dataDir)) {
			System.out.println("Data directory '" + dataDir.toAbsolutePath()
					+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}
		StringBuilder query= readFile(dataDir);
		//System.out.println(query.toString());
		System.out.println("Sending query to Parliament...");
		issueSelectQuery(query.toString());

	}

	private static void issueSelectQuery(String query) {
		QueryExecution exec = QueryExecutionFactory.sparqlService(SERVER_URL, query);
		ResultSet rs = exec.execSelect();
		
		String areaName = "";
		String wkt = "";
		
		// print out results individually
		while(rs.hasNext()) {
		    QuerySolution qs = rs.next() ;
		    areaName = qs.getLiteral("areaLabel").toString();
		    wkt = qs.getLiteral("areaWkt").toString();
		    
		    System.out.println(areaName + " - " + wkt);
		}
		exec.close() ;
	}
	
	private static StringBuilder readFile(Path path) {
		InputStream stream = null;
		try {
			stream = Files.newInputStream(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String line = null;
		StringBuilder fileContent = new StringBuilder();
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));

		// read in file
		try {
			while((line = in.readLine()) != null) {
				fileContent.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Query read successfully...");
		
		return fileContent;
	}

}
