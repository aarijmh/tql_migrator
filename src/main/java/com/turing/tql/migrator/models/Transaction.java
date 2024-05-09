package com.turing.tql.migrator.models;

import lombok.Data;

@Data
public class Transaction {
	private String name;
	private Boolean included;
	private String connectionId;
	private String connectionName;
	private String uri;
	private String mapName;
	private String schema;
	private String targetNameSpace;
	private String groupId;
	private String groupRuleMethodName;
	private String groupRuleNamespace;
	private String groupRuleFileName;
	private String customerRuleMethodName;
	private String customerRuleNamespace;
	private String customerRuleFileName;
}
