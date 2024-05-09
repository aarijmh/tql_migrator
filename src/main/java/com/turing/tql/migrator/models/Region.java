package com.turing.tql.migrator.models;

import lombok.Data;

@Data
public class Region {
	private int startingLine;
	private int endingLine;
	private String sourceElement = "";
	private String subElement = "";
	private String code = "";
	private String targetElements = "";

	public void print() {
		System.out.println("#Region Source Element");
		System.out.println(this.sourceElement);
		System.out.println("#Region Sub Element");
		System.out.println(this.subElement);

		System.out.println("#Region Code");
		System.out.println(this.code);

		System.out.println("#Region Target Element");
		System.out.println(this.targetElements);
	}
}
