package com.turing.tql.migrator.btm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Service
public class BTMParser {


    static Map<String, Node> functoidsMaps = new LinkedHashMap<>();
    static Map<String, Node> linkMap = new LinkedHashMap<>();
    static Map<String, Node> linkFromMap = new LinkedHashMap<>();
    

    public  void parseBTMFile(String fileName) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Resource classPathResource = new ClassPathResource(fileName);
        Document doc = builder.parse(classPathResource.getFile());
        doc.getDocumentElement().normalize();

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet();
            Sheet functionSheet = workbook.createSheet("Function Sheet");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Source Element");
            header.createCell(1).setCellValue("Mapping");
            header.createCell(2).setCellValue("Target Element");
            int rowCount = 1;
            int functionRowCount = 1;
            NodeList nodeList = doc.getElementsByTagName("Link");
            NodeList functiodsList = doc.getElementsByTagName("Functoid");

            for (int i = 0; i < nodeList.getLength(); i++) {
                linkMap.put(nodeList.item(i).getAttributes().getNamedItem("LinkID").getNodeValue(),
                        nodeList.item(i));
                linkFromMap.put(nodeList.item(i).getAttributes().getNamedItem("LinkFrom").getNodeValue(),
                        nodeList.item(i));
            }

            for (int i = 0; i < functiodsList.getLength(); i++) {
                functoidsMaps.put(functiodsList.item(i).getAttributes().getNamedItem("FunctoidID").getNodeValue(),
                        functiodsList.item(i));
            }

            for (int i = 0; i < nodeList.getLength(); i++) {

                String toLink = nodeList.item(i).getAttributes().getNamedItem("LinkTo").getNodeValue();
                String fromLink = nodeList.item(i).getAttributes().getNamedItem("LinkFrom").getNodeValue();
                String linkId = nodeList.item(i).getAttributes().getNamedItem("LinkID").getNodeValue();

                if (!StringUtils.isNumeric(toLink) && !StringUtils.isNumeric(fromLink)) {
                    createElementToElement(sheet, rowCount++, toLink, fromLink);
                }
                if (StringUtils.isNumeric(toLink) && !StringUtils.isNumeric(fromLink)) {
                    createElementToFunction(functionSheet, functionRowCount++, toLink, fromLink, linkId);
                }
            }
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);

            workbook.write(new FileOutputStream(new File("d:\\output\\commonMappings.xlsx")));
        }
    }

    public static void createElementToFunction(Sheet sheet, int rowCount, String toLink, String fromLink,
            String linkId) {
        Row row = sheet.createRow(rowCount++);

        Node function = functoidsMaps.get(toLink);
        NodeList functionChildren = function.getChildNodes();

        String inputs = "";
        String scripterCode = "";
        for (int i = 0; i < functionChildren.getLength(); i++) {
            Node functionChild = function.getChildNodes().item(i);
            if (functionChild.getNodeName().equals("Input-Parameters")) {
                for (int j = 0; j < functionChild.getChildNodes().getLength(); j++) {
                    Node functionChildChild = functionChild.getChildNodes().item(j);
                    if (functionChildChild.getNodeName().equals("Parameter")) {
                        if (functionChildChild.getAttributes().getNamedItem("Type").getNodeValue().equals("link")) {

                            String reqName = functionChildChild.getAttributes().getNamedItem("Value").getNodeValue();
                            if (!linkMap.containsKey(reqName))
                                continue;
                            String reqValue = linkMap
                                    .get(reqName)
                                    .getAttributes().getNamedItem("LinkFrom").getNodeValue();
                            if (StringUtils.isNumeric(reqValue))
                                continue;
                            inputs += extractNodePath(reqValue) + "\n";
                            linkMap.remove(functionChildChild.getAttributes().getNamedItem("Value").getNodeValue());
                        }
                        if (functionChildChild.getAttributes().getNamedItem("Type").getNodeValue().equals("constant")) {
                            inputs += functionChildChild.getAttributes().getNamedItem("Value") + "\n";
                        }
                    }
                }
            }
            if (functionChild.getNodeName().equals("ScripterCode")) {
                scripterCode += functionChild.getChildNodes().item(1).getTextContent();
            }
        }

        Node fromNode = linkFromMap.get(toLink);
        String output = "";
        String fromNodeValue = fromNode.getAttributes().getNamedItem("LinkTo").getNodeValue();
        if (!StringUtils.isNumeric(fromNodeValue)) {
            output = extractNodePath(fromNodeValue);
            linkMap.remove(fromNode.getAttributes().getNamedItem("LinkID").getNodeValue());
        }

        row.createCell(0).setCellValue(inputs);
        row.createCell(1).setCellValue("Function");
        row.createCell(2)
                .setCellValue(scripterCode.isBlank() ? function.getAttributes().getNamedItem("Functoid-FID").toString()
                        : scripterCode);
        row.createCell(3).setCellValue(output);
    }

    public static void createElementToElement(Sheet sheet, int rowCount, String toLink, String forLink) {
        Row row = sheet.createRow(rowCount++);
        row.createCell(0).setCellValue(extractNodePath(forLink));
        row.createCell(1).setCellValue("Direct Mapping");
        row.createCell(2).setCellValue(extractNodePath(toLink));
    }

    public static String extractNodePath(String value) {
        String[] splits = value.split("/\\*");
        String returnValue = "";
        for (String spli : splits) {
            if (spli.isEmpty())
                continue;
            returnValue += spli.split("=")[1].replaceAll("]", "") + "/";
        }
        return returnValue;
    }
}
