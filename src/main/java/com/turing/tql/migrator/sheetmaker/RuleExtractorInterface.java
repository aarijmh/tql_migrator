package com.turing.tql.migrator.sheetmaker;

import org.apache.poi.ss.usermodel.Workbook;

import com.turing.tql.migrator.models.CustomerInformation;

public interface RuleExtractorInterface {

	void attachRulesSheet(Workbook workbook, CustomerInformation customerInformation);
}
