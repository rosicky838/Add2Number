package com.add2numweb.service;

import com.add2numweb.dto.CalculationResult;
import main.java.com.add2num.MyBigNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BigNumberService {

    private static final Logger log = LoggerFactory.getLogger(BigNumberService.class);
    private static final int MAX_LENGTH = 5000;
    private static final java.util.regex.Pattern DIGIT_PATTERN = java.util.regex.Pattern.compile("^\\d+$");

    private final MyBigNumber myBigNumber;

    public BigNumberService() {
        this.myBigNumber = new MyBigNumber();
    }

    public BigNumberService(MyBigNumber myBigNumber) {
        this.myBigNumber = myBigNumber;
    }

    public CalculationResult calculate(String num1, String num2) {
        if (num1 == null || num2 == null) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ cả hai số!");
        }

        String n1 = num1.trim();
        String n2 = num2.trim();

        if (n1.isEmpty() || n2.isEmpty()) {
            throw new IllegalArgumentException("Các ô nhập không được để trống!");
        }

        if (!DIGIT_PATTERN.matcher(n1).matches() || !DIGIT_PATTERN.matcher(n2).matches()) {
            throw new IllegalArgumentException("Dữ liệu không hợp lệ! Vui lòng chỉ nhập các chữ số (0-9).");
        }

        if (n1.length() > MAX_LENGTH || n2.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Độ dài mỗi số không được vượt quá " + MAX_LENGTH + " chữ số.");
        }

        // Tái sử dụng logic cộng chuỗi từ thư viện MyBigNumber
        String result = myBigNumber.sum(n1, n2);

        // Sinh danh sách tiến trình từng bước với logic số nhớ chính xác (Pre-sized & StringBuilder tối ưu hiệu năng)
        int estimatedSteps = Math.max(n1.length(), n2.length()) + 1;
        List<String> steps = new ArrayList<>(estimatedSteps);
        int carry = 0;

        for (int i = n1.length() - 1, j = n2.length() - 1, step = 1;
             i >= 0 || j >= 0 || carry != 0;
             step++) {
            int d1 = i >= 0 ? n1.charAt(i--) - '0' : 0;
            int d2 = j >= 0 ? n2.charAt(j--) - '0' : 0;
            int total = d1 + d2 + carry;
            int prevCarry = carry;
            carry = total / 10;

            StringBuilder sb = new StringBuilder(64);
            sb.append("Bước ").append(step)
              .append(": ").append(d1).append(" + ").append(d2)
              .append(" + nhớ(").append(prevCarry).append(") = ").append(total)
              .append(" → ghi ").append(total % 10).append(", nhớ ").append(carry);
            steps.add(sb.toString());
        }

        log.info("Phép tính: {} + {} = {} (Tổng số bước: {})", n1, n2, result, steps.size());
        return new CalculationResult(n1, n2, result, steps);
    }
}
