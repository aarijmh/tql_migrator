package com.turing.tql.migrator.models;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Functoid {
    private String functoidId;
    private String functionId;
    private List<String> elements = new ArrayList<>();
    private List<String> constants = new ArrayList<>();
    private List<String> functions = new ArrayList<>();
}
