package com.turing.tql.migrator.sheetmaker;

import org.apache.poi.ss.usermodel.Workbook;

import com.turing.tql.migrator.models.CustomerInformation;

public interface ReverseEngineeringSheetMaker {

	public Workbook makeCustomerInformationWorkbook(CustomerInformation customerInformation);
}
