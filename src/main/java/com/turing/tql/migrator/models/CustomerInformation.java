package com.turing.tql.migrator.models;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CustomerInformation {
	private String tPInGroupID;
	private String groupNameIn;
	private String tPOutGroupID;
	private String groupNameOut;
	private List<Transaction> transactions = new ArrayList<>();
	private String cid;
	private String tpProfileId;
	private String gsVersion;
	private String tradingPartnerName;

}
