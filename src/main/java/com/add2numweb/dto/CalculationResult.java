package com.add2numweb.dto;

import java.util.Collections;
import java.util.List;

public class CalculationResult {
    private final String num1;
    private final String num2;
    private final String result;
    private final List<String> steps;

    public CalculationResult(String num1, String num2, String result, List<String> steps) {
        this.num1 = num1;
        this.num2 = num2;
        this.result = result;
        this.steps = steps != null ? Collections.unmodifiableList(steps) : Collections.emptyList();
    }

    public String getNum1() {
        return num1;
    }

    public String getNum2() {
        return num2;
    }

    public String getResult() {
        return result;
    }

    public List<String> getSteps() {
        return steps;
    }
}
