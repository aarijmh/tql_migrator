package com.turing.tql.migrator.sheetmaker;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.turing.tql.migrator.models.CustomerInformation;
import com.turing.tql.migrator.models.Transaction;

@Service
public class POCSheetMaker implements ReverseEngineeringSheetMaker {

	@Override
	public Workbook makeCustomerInformationWorkbook(CustomerInformation customerInformation) {

		Workbook workbook = new XSSFWorkbook();
		
		String transactionsName = String.join("_", customerInformation.getTransactions().stream().map(x->x.getName()).collect(Collectors.toList()));
		Sheet sheet = workbook.createSheet(customerInformation.getTradingPartnerName().substring(0,10)+" "+transactionsName); 
		
		List<String> columnNames = new ArrayList<>();
					

		columnNames.add("Group Name");
		columnNames.add("tpingroupID");
		columnNames.add("CID");
		columnNames.add("TPProfile_id");
		columnNames.add("Trading partner’s name");
		columnNames.add("GS Version");
		for(Transaction transaction : customerInformation.getTransactions()) {
			columnNames.add(transaction.getName()+" Required?");
			columnNames.add(transaction.getName()+" - Sample File");
		}
		for(Transaction transaction : customerInformation.getTransactions()) {
			columnNames.add("Group Rules " +transaction.getName());
			columnNames.add("Customer Rules " +transaction.getName());
		}
		
		int count = 0;
		Row header = sheet.createRow(0);
		
		for(String col : columnNames) {
			header.createCell(count++).setCellValue(col);
		}
		
		Row row = sheet.createRow(1);
		count  = 0;
		row.createCell(count++).setCellValue(customerInformation.getGroupNameIn());
		row.createCell(count++).setCellValue(customerInformation.getTPInGroupID());
		row.createCell(count++).setCellValue("");
		row.createCell(count++).setCellValue(customerInformation.getTpProfileId());
		row.createCell(count++).setCellValue(customerInformation.getTradingPartnerName());
		row.createCell(count++).setCellValue("");
		for(Transaction transaction : customerInformation.getTransactions()) {
			row.createCell(count++).setCellValue(transaction.getIncluded());
			row.createCell(count++).setCellValue("Yes");
		}
		for(Transaction transaction : customerInformation.getTransactions()) {
			row.createCell(count++).setCellValue(transaction.getGroupRuleMethodName());
			row.createCell(count++).setCellValue(transaction.getCustomerRuleMethodName());
		}
		
		return workbook;
	}

}
