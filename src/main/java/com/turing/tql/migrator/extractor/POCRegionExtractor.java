package com.turing.tql.migrator.extractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.turing.tql.migrator.models.Region;

@Service
public class POCRegionExtractor {

	public List<Region> extractCodeRegions(String functionName, File xmlFile, File csFile) throws ParserConfigurationException, SAXException, IOException {

		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(xmlFile);
		doc.getDocumentElement().normalize();
		List<Region> regions = new ArrayList<>();
		try {
			List<String> allLines = Files.readAllLines(Paths.get(xmlFile.getAbsolutePath()));
			List<String> csAllLines = Files.readAllLines(Paths.get(csFile.getAbsolutePath()));

			for (int i = 0; i < allLines.size(); i++) {
				String line = allLines.get(i);
				if (line.contains("<function>") && line.contains("<name>"+functionName+"</name>")) {
					Pattern pattern = Pattern.compile("<name>([a-zA-Z]|\\d)*</name>");
					Matcher matcher = pattern.matcher(line);
					String parameter = "";
					while (matcher.find()) {
						parameter = matcher.group();
					}
					parameter = parameter.replaceAll("<name>", "").replaceAll("</name>", "").strip();
					for (; i < allLines.size(); i++) {
						String innerLine = allLines.get(i);
						Region region = new Region();
						if (innerLine.contains("<cpp:region>")) {

							region.setStartingLine(i - 1);
							String regionLine = "";
							while (!regionLine.contains("<cpp:endregion>")) {
								regionLine = allLines.get(++i);
								if (regionLine.contains("<foreach>")) {
									i = processForEachBlock(i, parameter, regionLine, allLines, csAllLines, region);
								}else if (regionLine.contains("<expr_stmt>") && regionLine.contains("RULEINFOCollection")) {
								//	region.setStartingLine(i - 1);
									while (!regionLine.contains("<forEach>") && !regionLine.contains("</function>") && !regionLine.contains("<cpp:endregion>")) {
										regionLine = allLines.get(++i);
										if (regionLine.contains("<expr_stmt>") && regionLine.contains("UpdateIncludeSegmentRule")) {
											region.setTargetElements(region.getTargetElements() + csAllLines.get(i - 1).split(",")[1] + "\n");
										}
									}
									region.setEndingLine(i);
									region.setCode(csAllLines.subList(region.getStartingLine(), region.getEndingLine()).stream().collect(Collectors.joining("\n")));
									regions.add(region);
								}
							}
							region.setEndingLine(i - 1);
							region.setCode(csAllLines.subList(region.getStartingLine(), region.getEndingLine()).stream().collect(Collectors.joining("\n")));
							regions.add(region);
						} else if (innerLine.contains("<foreach>")) {
							region.setStartingLine(i - 1);
							String regionLine = allLines.get(i);
							i = processForEachBlock(i, parameter, regionLine, allLines, csAllLines, region);
							region.setEndingLine(i);
							region.setCode(csAllLines.subList(region.getStartingLine(), region.getEndingLine()).stream().collect(Collectors.joining("\n")));
							regions.add(region);
						} else if (innerLine.contains("<if_stmt>")) {
							region.setStartingLine(i - 1);
							while (!innerLine.contains("</if_stmt>")) {
								innerLine = allLines.get(++i);
								if (innerLine.contains("<expr_stmt>") && innerLine.contains(parameter)) {
									region.setTargetElements(region.getTargetElements() + (csAllLines.get(i - 1).split("=")[0]).replace(parameter + ".", "") + "\n");
								}

							}
							region.setEndingLine(i);
							region.setCode(csAllLines.subList(region.getStartingLine(), region.getEndingLine()).stream().collect(Collectors.joining("\n")));
							regions.add(region);
						} else if (innerLine.contains("<expr_stmt>") && innerLine.contains("RULEINFOCollection")) {
							region.setStartingLine(i - 1);
							while (!innerLine.contains("<forEach>") && !innerLine.contains("</function>")) {
								innerLine = allLines.get(++i);
								if (innerLine.contains("<expr_stmt>") && innerLine.contains("UpdateIncludeSegmentRule")) {
									region.setTargetElements(region.getTargetElements() + csAllLines.get(i - 1).split(",")[1] + "\n");
								}
							}
							region.setEndingLine(i);
							region.setCode(csAllLines.subList(region.getStartingLine(), region.getEndingLine()).stream().collect(Collectors.joining("\n")));
							regions.add(region);
						}
						if (innerLine.contains("</function>")) {
							break;
						}
					}
				}
				// System.out.println(line);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return regions;
	}

	public int processForEachBlock(int i, String parameter, String regionLine, List<String> allLines, List<String> csAllLines, Region region) {
		String forEachVariableFirstSubstring = regionLine.substring(0, regionLine.indexOf("<range>"));
		String forEachVariable = forEachVariableFirstSubstring.substring(forEachVariableFirstSubstring.lastIndexOf("<name>"));
		forEachVariable = forEachVariable.replaceAll("<name>", "").replaceAll("</name>", "").strip();
		String[] forEachItem = csAllLines.get(i - 1).substring(csAllLines.get(i - 1).indexOf(parameter), csAllLines.get(i - 1).length() - 1).split("\\.");

		// String[] aI = forEachItem.split("\\.");
		region.setSourceElement(forEachItem[1]);
		for (int k = 2; k < forEachItem.length; k++)
			region.setSubElement(region.getSubElement() + forEachItem[k] + ".");

		while (!regionLine.contains("</foreach>")) {
			regionLine = allLines.get(++i);
			if (regionLine.contains("<expr_stmt>")) {
				if (csAllLines.get(i - 1).strip().startsWith(forEachVariable) || csAllLines.get(i - 1).strip().startsWith(parameter)) {
					region.setTargetElements(region.getTargetElements() + csAllLines.get(i - 1).split("=")[0] + "\n");
				}
			}
		}

		return i;
	}
}
