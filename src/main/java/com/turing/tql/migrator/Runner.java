package com.turing.tql.migrator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.xml.sax.SAXException;

import com.turing.tql.migrator.btm.BTMParser;
import com.turing.tql.migrator.datasources.CustomerInformationReader;
import com.turing.tql.migrator.models.CustomerInformation;
import com.turing.tql.migrator.sheetmaker.ReverseEngineeringSheetMaker;
import com.turing.tql.migrator.sheetmaker.RuleExtractorInterface;

@SpringBootApplication
public class Runner implements CommandLineRunner {
	private static Logger LOG = LoggerFactory.getLogger(Runner.class);

	@Autowired
	private CustomerInformationReader customerInformationReader;
	
	@Autowired
	private ReverseEngineeringSheetMaker reverseEngineeringSheetMaker;
	
	@Autowired
	private RuleExtractorInterface ruleExtractorInterface;
	
	@Autowired
	private BTMParser btmParser;
	
	public static void main(String[] args) {
		LOG.info("STARTING THE APPLICATION");
		SpringApplication.run(Runner.class, args);
		LOG.info("APPLICATION FINISHED");
	}

	@Override
	public void run(String... args) {
		LOG.info("EXECUTING : command line runner");

		for (int i = 0; i < args.length; ++i) {
			LOG.info("args[{}]: {}", i, args[i]);
		}
		System.out.println("****EXTRACTING CUSTOMER INFORMATION*****");
		CustomerInformation customerInformation = customerInformationReader.readCustomerInformation(3139);
		System.out.println("****MAKING CUSTOMER INFORMATION WORKBOOK*****");
		Workbook workbook = reverseEngineeringSheetMaker.makeCustomerInformationWorkbook(customerInformation);
		System.out.println("****EXTRACTING RULES*****");
		ruleExtractorInterface.attachRulesSheet(workbook, customerInformation);
		try {
			System.out.println("***PARSING BTM FILES*****");
			btmParser.parseBTMFile("btm/TQL.BZT.EDI.204v4010_to_Penske204.btm");
			
			System.out.println("****WRITING OUTPUT TO FILE*****");
			workbook.write(new FileOutputStream(new File("d:\\output\\"+customerInformation.getTradingPartnerName()+".xlsx")));
		} catch (IOException | SAXException | ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
}
