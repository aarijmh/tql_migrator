package com.turing.tql.migrator.extractor;

import java.util.List;

import com.turing.tql.migrator.models.Region;

public interface FunctionExtractor {

	String extractFunctionFromFile(String transactionName, String functionName, String fileName);
	List<Region> extractRegionPiecesFromFile(String transactionName, String functionName, String fileName);
}
