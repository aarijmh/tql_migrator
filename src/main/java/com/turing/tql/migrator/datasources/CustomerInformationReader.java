package com.turing.tql.migrator.datasources;

import com.turing.tql.migrator.models.CustomerInformation;

public interface CustomerInformationReader {
	CustomerInformation readCustomerInformation(Integer tpGroupId);

}
