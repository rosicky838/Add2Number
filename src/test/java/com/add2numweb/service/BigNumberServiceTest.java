package com.add2numweb.service;

import com.add2numweb.dto.CalculationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BigNumberServiceTest {

    private BigNumberService service;

    @BeforeEach
    void setUp() {
        service = new BigNumberService();
    }

    @Test
    void testBasicAddition() {
        CalculationResult result = service.calculate("1234", "897");
        assertEquals("2131", result.getResult());
        assertFalse(result.getSteps().isEmpty());
        // Kiểm tra bước đầu tiên phải có nhớ(0), không bị gán đè thành nhớ(1)
        assertTrue(result.getSteps().get(0).contains("nhớ(0)"), "Bước 1 phải có số nhớ ban đầu là 0");
    }

    @Test
    void testContinuousCarry() {
        CalculationResult result = service.calculate("999", "1");
        assertEquals("1000", result.getResult());
        assertEquals(4, result.getSteps().size());
        assertTrue(result.getSteps().get(0).contains("nhớ(0)"));
        assertTrue(result.getSteps().get(1).contains("nhớ(1)"));
    }

    @Test
    void testZeroAddition() {
        CalculationResult result = service.calculate("0", "0");
        assertEquals("0", result.getResult());
    }

    @Test
    void testVeryBigNumber() {
        String num1 = "99999999999999999999";
        String num2 = "1";
        CalculationResult result = service.calculate(num1, num2);
        assertEquals("100000000000000000000", result.getResult());
    }

    @Test
    void testTrimWhitespace() {
        CalculationResult result = service.calculate("  1234  ", " 897 ");
        assertEquals("2131", result.getResult());
    }

    private boolean checkThrows(String n1, String n2) {
        try {
            service.calculate(n1, n2);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    @Test
    void testNullOrEmptyInput() {
        assertTrue(checkThrows(null, "123"));
        assertTrue(checkThrows("123", ""));
        assertTrue(checkThrows("   ", "123"));
        assertFalse(checkThrows("123", "456"));
    }

    @Test
    void testInvalidCharacters() {
        assertTrue(checkThrows("123a", "456"));
        assertTrue(checkThrows("-100", "50"));
        assertTrue(checkThrows("12.3", "45"));
    }

    @Test
    void testExceedMaxLength() {
        String overLimit = "1".repeat(5001);
        assertTrue(checkThrows(overLimit, "1"));
        assertTrue(checkThrows("1", overLimit));
    }

    @Test
    void testCustomConstructor() {
        BigNumberService customService = new BigNumberService(new main.java.com.add2num.MyBigNumber());
        assertNotNull(customService.calculate("1", "2"));
    }

    @Test
    void testCalculationResultDTO() {
        CalculationResult cr1 = new CalculationResult("12", "34", "46", null);
        assertEquals("12", cr1.getNum1());
        assertEquals("34", cr1.getNum2());
        assertEquals("46", cr1.getResult());
        assertTrue(cr1.getSteps().isEmpty());
    }
}
