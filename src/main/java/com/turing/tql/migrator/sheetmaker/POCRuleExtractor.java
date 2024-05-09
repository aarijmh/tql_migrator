package com.turing.tql.migrator.sheetmaker;

import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.turing.tql.migrator.extractor.FunctionExtractor;
import com.turing.tql.migrator.models.CustomerInformation;
import com.turing.tql.migrator.models.Region;
import com.turing.tql.migrator.models.Transaction;

@Service
public class POCRuleExtractor implements RuleExtractorInterface {
	
	@Autowired
	private FunctionExtractor functionExtractor;
//	
//	private String convertToJavascript(String cSharpCode) {
//		JSONObject jo = new JSONObject();
//		jo.put("inputCodeText", cSharpCode);
//		jo.put("inputLang", "Csharp");
//		jo.put("outputLang", "JavaScript");
//		 
//		HttpClient client = HttpClient.newHttpClient();
//		HttpRequest request = HttpRequest.newBuilder()
//				  .uri(URI.create("https://www.codeconvert.ai/api/free-convert"))
//				  .POST(HttpRequest.BodyPublishers.ofString(jo.toString()))
//				  .build();
//		try {
//			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//			return response.body();
//		} catch (IOException | InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//				
//				
//		return "";
//	}

	@Override
	public void attachRulesSheet(Workbook workbook, CustomerInformation customerInformation) {
		
		 CellStyle style = workbook.createCellStyle(); //Create new style
         style.setWrapText(true); //Set wordwrap
         
         
         
		for(Transaction transaction : customerInformation.getTransactions()) {
			if(transaction.getCustomerRuleMethodName() != null && transaction.getCustomerRuleNamespace() != null) {
				Sheet sheet = workbook.createSheet(transaction.getName()+"_Customer Rules");
				
				
				
//				String value = functionExtractor.extractFunctionFromFile(transaction.getName(), transaction.getCustomerRuleMethodName(), transaction.getCustomerRuleNamespace());
//				if(value != null) {
//					if(value.length() > 32000) {
//						row.createCell(0).setCellValue(value.substring(0,32000));
//						row.getCell(0).setCellStyle(style);
//						Row newRow = sheet.createRow(1);
//						newRow.createCell(0).setCellValue(value.substring(32000));
//						newRow.getCell(0).setCellStyle(style);
//					}
//					else
//						row.createCell(0).setCellValue(value);
//					row.getCell(0).setCellStyle(style);
//				}
				List<Region> regions = functionExtractor.extractRegionPiecesFromFile(transaction.getName(), transaction.getCustomerRuleMethodName(), transaction.getCustomerRuleNamespace());
				if(regions != null) {
					
					int i = 1;
					
					for(Region region : regions) {
						Row row = sheet.createRow(i++);
						row.createCell(0).setCellValue(region.getSourceElement());
						row.createCell(1).setCellValue(region.getSubElement());
						//String jsCode = convertToJavascript(region.getCode());
						
						row.createCell(2).setCellValue(region.getCode());
//						if(!jsCode.isBlank())
//							row.createCell(4).setCellValue(jsCode);
						row.createCell(3).setCellValue(region.getTargetElements());
						for(int j = 0; j < 4; j++) {
							row.getCell(j).setCellStyle(style);
						}
					}

				}
				sheet.autoSizeColumn(0);
				sheet.autoSizeColumn(1);
				sheet.autoSizeColumn(2);
				sheet.autoSizeColumn(3);
			}
			
			if(transaction.getGroupRuleMethodName() != null && transaction.getGroupRuleNamespace() != null) {
				Sheet sheet = workbook.createSheet(transaction.getName()+"_Group Rules");
				sheet.autoSizeColumn(0);
				
				
			//	String value = functionExtractor.extractFunctionFromFile(transaction.getName(), transaction.getGroupRuleMethodName(), transaction.getGroupRuleNamespace());
				List<Region> regions = functionExtractor.extractRegionPiecesFromFile(transaction.getName(), transaction.getGroupRuleMethodName(), transaction.getGroupRuleNamespace());
				if(regions != null) {
					
					int i = 1;
					
					for(Region region : regions) {
						Row row = sheet.createRow(i++);
						row.createCell(0).setCellValue(region.getSourceElement());
						row.createCell(1).setCellValue(region.getSubElement());
						row.createCell(2).setCellValue(region.getCode());
						row.createCell(3).setCellValue(region.getTargetElements());
						for(int j = 0; j < 4; j++) {
							row.getCell(j).setCellStyle(style);
						}
					}

				}
//				if(value != null) {
//					if(value.length() > 32000) {
//						row.createCell(0).setCellValue(value.substring(0,32000));
//						row.getCell(0).setCellStyle(style);
//						Row newRow = sheet.createRow(1);
//						newRow.createCell(0).setCellValue(value.substring(32000));
//						newRow.getCell(0).setCellStyle(style);
//					}
//					else
//					{
//						row.createCell(0).setCellValue(value);
//					row.getCell(0).setCellStyle(style);
//					}
//				}
				sheet.autoSizeColumn(0);
				sheet.autoSizeColumn(1);
				sheet.autoSizeColumn(2);
				sheet.autoSizeColumn(3);
			}
			
		}

	}

}
