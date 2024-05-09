package com.turing.tql.migrator.extractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.xml.sax.SAXException;

import com.turing.tql.migrator.models.Region;

@Service
public class POCFunctionExtractor implements FunctionExtractor {

	public static String workingFolder = "d:\\output";
	
	@Autowired
	private POCRegionExtractor pocRegionExtractor;
	public File findFileFromDirectory(String consoloditatedName) {
		File dir = null;
		try {
			dir = ResourceUtils.getFile("classpath:cs");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		File foundFile = null;
		for (File f : dir.listFiles()) {

			if (f.getName().contains(consoloditatedName)) {
				foundFile = f;
				break;
			}
		}
		return foundFile;
	}
	
	public Boolean convertToXML(File foundFile, String outputFileName) {
		try {

			ProcessBuilder processBuilder = new ProcessBuilder(List.of("srcml", foundFile.getAbsolutePath(), "-o", workingFolder + File.separator + outputFileName));
			processBuilder.directory(new File("d:\\"));
			Process process;

			process = processBuilder.start();

			process.getInputStream();

			Thread.sleep(3000);
			process.destroy();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public String extractFunctionFromFile(String transactionName, String functionName, String fileName) {
		
		String consoloditatedName = fileName + "_Rules" + transactionName;

		/*
		 * Find Files
		 */
		File foundFile = findFileFromDirectory(consoloditatedName);
		if (foundFile == null)
			return null;
		/*
		 * 
		 */

		

		/*
		 * Convert File to XML
		 */
		String outputFileName = foundFile.getName() + ".xml";
		if(!convertToXML(foundFile, outputFileName)) {
			return null;
		}

		/*
		 * 
		 */

		/*
		 * Extract Function
		 */

		try {
			File file = new File(workingFolder + File.separator + outputFileName);
			List<String> allLines = Files.readAllLines(Paths.get(file.getPath()));

			List<String> finalLines = new ArrayList<String>();

			finalLines.add(allLines.get(0));
			finalLines.add(allLines.get(1));
			for (int i = 2; i < allLines.size(); i++) {
				String line = allLines.get(i);

				if (line.contains("<function>") && line.contains(functionName)) {

					for (; i < allLines.size(); i++) {
						if (allLines.get(i).contains("</function>")) {
							finalLines.add(allLines.get(i));
							break;
						}
						finalLines.add(allLines.get(i));
					}
					break;
				}
			}
			finalLines.add("</unit>");
			/*
			 * 
			 */

			/*
			 * Write to a temp file
			 */
			File tempFile = new File("d:\\output\\temp.xml");
			tempFile.createNewFile();

			FileWriter fileWriter = new FileWriter(tempFile);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			finalLines.forEach(x -> printWriter.write(x + "\n"));
			printWriter.close();
			/*
			 * 
			 */

			/*
			 * Convert to source code
			 */
			ProcessBuilder processBuilder = new ProcessBuilder(List.of("srcml", tempFile.getAbsolutePath()));
			processBuilder.directory(new File("d:\\"));
			Process process = processBuilder.start();
			InputStream inputStream = process.getInputStream();

			List<String> resultLines = null;
			try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))){
				resultLines = br.lines().collect(Collectors.toList());
				resultLines.remove(0);
			}
			

			// System.out.println(resultLines.stream().collect(Collectors.joining("\n")));
			inputStream.close();
			process.destroy();
			/*
			 * 
			 * 
			 */

			return resultLines.stream().collect(Collectors.joining("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void main(String[] args) throws FileNotFoundException {

	}

	@Override
	public List<Region> extractRegionPiecesFromFile(String transactionName, String functionName, String fileName) {
		String consoloditatedName = fileName + "_Rules" + transactionName;

		/*
		 * Find Files
		 */
		File foundFile = findFileFromDirectory(consoloditatedName);
		if (foundFile == null)
			return null;
		System.out.println("Processing File : "+foundFile.getName());
		/*
		 * 
		 */
		
		/*
		 * Convert File to XML
		 */
		String outputFileName = foundFile.getName() + ".xml";
		if(!convertToXML(foundFile, outputFileName)) {
			return null;
		}
		/*
		 * 
		 */
		File xmlFile = new File(workingFolder + File.separator + outputFileName);
		
		List<Region> regions = null;
		try {
			regions = pocRegionExtractor.extractCodeRegions(functionName, xmlFile, foundFile);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		
		return regions;
	}

}
