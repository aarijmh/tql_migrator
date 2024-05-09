package com.turing.tql.migrator.datasources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.turing.tql.migrator.models.CustomerInformation;
import com.turing.tql.migrator.models.Transaction;

@Service
public class POCExcelCustomerInformationReader implements CustomerInformationReader {

	private static List<String> transactionList = List.of("204", "990", "214", "210", "997");

	@Override
	public CustomerInformation readCustomerInformation(Integer tpGroupId) {

		File file = null;
		try {
			Resource classPathResource = new ClassPathResource("poc_excel/testdata.xlsx");
			file = classPathResource.getFile();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		CustomerInformation customerInformation = null;

		try (Workbook workbook = new XSSFWorkbook(file)) {

			Sheet sheet = workbook.getSheet("Query Results");

			Row header = sheet.getRow(0);

			Map<String, Integer> columnIndexMap = new HashMap<String, Integer>();

			int columnCount = 0;
			for (Cell cell : header) {
				columnIndexMap.put(cell.getStringCellValue().strip().toLowerCase(), columnCount++);
			}

			boolean customerFound = false;
			for (Row row : sheet) {
				if (row.getRowNum() == 0)
					continue;

				if (customerFound)
					break;

				int id = ((Double) (row.getCell(15).getNumericCellValue())).intValue();

				if (id == tpGroupId) {
					customerFound = true;
				} else {
					continue;
				}

				customerInformation = new CustomerInformation();

				customerInformation.setGroupNameIn(getStringCellValue(row.getCell(columnIndexMap.get("groupnamein"))));
				customerInformation.setTPInGroupID(getStringCellValue(row.getCell(columnIndexMap.get("tpingroupid"))));
				customerInformation.setTpProfileId(getStringCellValue(row.getCell(columnIndexMap.get("tpprofileid"))));
				customerInformation.setTradingPartnerName(getStringCellValue(row.getCell(columnIndexMap.get("tradingpartnername"))));
				// customerInformation.setTradingPartnerName(getStringCellValue(
				// row.getCell(columnIndexMap.get("TradingPartnerName"))));

				for (String transactionId : transactionList) {
					Transaction transaction = new Transaction();
					transaction.setName(transactionId);
					if (columnIndexMap.containsKey("needs" + transactionId))
						transaction.setIncluded(getBooleanCellValue(row.getCell(columnIndexMap.get("needs" + transactionId))));
					if (columnIndexMap.containsKey("tp" + transactionId + "groupid"))
						transaction.setGroupId(getStringCellValue(row.getCell(columnIndexMap.get("tp" + transactionId + "groupid"))));
					if (columnIndexMap.containsKey("connectionid" + transactionId))
						transaction.setConnectionId(getStringCellValue(row.getCell(columnIndexMap.get("connectionid" + transactionId))));
					if (columnIndexMap.containsKey("connectionname" + transactionId))
						transaction.setConnectionName(getStringCellValue(row.getCell(columnIndexMap.get("connectionname" + transactionId))));
					if (columnIndexMap.containsKey("uri" + transactionId))
						transaction.setUri(getStringCellValue(row.getCell(columnIndexMap.get("uri" + transactionId))));
					if (columnIndexMap.containsKey("mapname" + transactionId))
						transaction.setMapName(getStringCellValue(row.getCell(columnIndexMap.get("mapname" + transactionId))));
					if (columnIndexMap.containsKey("schema" + transactionId))
						transaction.setSchema(getStringCellValue(row.getCell(columnIndexMap.get("schema" + transactionId))));
					if (columnIndexMap.containsKey("targetnamespace" + transactionId))
						transaction.setTargetNameSpace(getStringCellValue(row.getCell(columnIndexMap.get("targetnamespace" + transactionId))));
					
					customerInformation.getTransactions().add(transaction);
				}
				Sheet ruleSheet = null;
				try {
					ruleSheet = workbook.getSheet("TP"+tpGroupId);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				if(ruleSheet != null)
					setRulesForTransaction(tpGroupId, customerInformation.getTransactions(), ruleSheet);

				break;
			}

		} catch (InvalidFormatException | IOException e) {
			e.printStackTrace();
		}
		return customerInformation;
	}

	public void setRulesForTransaction(Integer tpProfileId, List<Transaction> transactions, Sheet sheet) {
		Row header = sheet.getRow(0);
		Map<String, Integer> columnNamesIndexMap = new HashMap<String, Integer>();
		int count = 0;
		for(Cell cell : header) {
			columnNamesIndexMap.put(getStringCellValue(cell), count++);
		}
		
		for(Transaction transaction : transactions) {
			if(columnNamesIndexMap.containsKey(transaction.getName()+" Group Rules")) {
				Row mRow = sheet.getRow(1);
				String value = getStringCellValue(mRow.getCell(columnNamesIndexMap.get(transaction.getName()+" Group Rules")));
				if(value == null || value.equalsIgnoreCase("n/a") || value.isBlank()) {
					
				}
				else {
					transaction.setGroupRuleMethodName(value);
					
					mRow = sheet.getRow(2);
					value = getStringCellValue(mRow.getCell(columnNamesIndexMap.get(transaction.getName()+" Group Rules")));
					if(value == null || value.equalsIgnoreCase("n/a") || value.isBlank()) {
						
					}
					else {
						transaction.setGroupRuleNamespace(value);
					}
					
					mRow = sheet.getRow(3);
					value = getStringCellValue(mRow.getCell(columnNamesIndexMap.get(transaction.getName()+" Group Rules")));
					if(value == null || value.equalsIgnoreCase("n/a") || value.isBlank()) {
						
					}
					else {
						transaction.setGroupRuleFileName(value);
					}
				}
				
				
			}
			if(columnNamesIndexMap.containsKey(transaction.getName()+" Customer Rules")) {
				Row mRow = sheet.getRow(1);
				String value = getStringCellValue(mRow.getCell(columnNamesIndexMap.get(transaction.getName()+" Customer Rules")));
				if(value == null || value.equalsIgnoreCase("n/a") || value.isBlank()) {
					
				}
				else {
					transaction.setCustomerRuleMethodName(value);
					mRow = sheet.getRow(2);
					value = getStringCellValue(mRow.getCell(columnNamesIndexMap.get(transaction.getName()+" Customer Rules")));
					if(value == null || value.equalsIgnoreCase("n/a") || value.isBlank()) {
						
					}
					else {
						transaction.setCustomerRuleNamespace(value);
					}
					
					mRow = sheet.getRow(3);
					value = getStringCellValue(mRow.getCell(columnNamesIndexMap.get(transaction.getName()+" Customer Rules")));
					if(value == null || value.equalsIgnoreCase("n/a") || value.isBlank()) {
						
					}
					else {
						transaction.setCustomerRuleFileName(value);
					}
				}
				

			}
		}
	}

	public String getStringCellValue(Cell cell) {
		switch (cell.getCellType()) {
		case NUMERIC:
			return Double.toString(cell.getNumericCellValue()).replace(".0", "");
		case STRING:
			return cell.getStringCellValue();
		default:
			break;
		}
		return null;
	}

	public Boolean getBooleanCellValue(Cell cell) {
		switch (cell.getCellType()) {
		case NUMERIC:
			return cell.getNumericCellValue() == 1.0 ? true : false;
		case STRING:
			return Boolean.valueOf(cell.getStringCellValue());
		case BOOLEAN:
			return cell.getBooleanCellValue();
		default:
			break;
		}
		return false;
	}
}
