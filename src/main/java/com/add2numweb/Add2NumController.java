package com.add2numweb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import main.java.com.add2num.MyBigNumber;

import java.util.ArrayList;
import java.util.List;

@Controller
public class Add2NumController {

    private static final Logger log = LoggerFactory.getLogger(Add2NumController.class);
    private final MyBigNumber myBigNumber = new MyBigNumber(); // ← dùng Task 1

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/calculate")
    public String calculate(@RequestParam String num1,
                            @RequestParam String num2,
                            Model model) {
        if (!num1.matches("\\d+") || !num2.matches("\\d+")) {
            model.addAttribute("error", "Vui lòng nhập số hợp lệ!");
            return "index";
        }
        
        String result = myBigNumber.sum(num1, num2); // ← gọi hàm Task 1
        
        // Tính tiến trình riêng
        List<String> steps = new ArrayList<>();
        int i = num1.length() - 1, j = num2.length() - 1;
        int carry = 0, step = 1;

        while (i >= 0 || j >= 0 || carry != 0) {
            int d1 = i >= 0 ? num1.charAt(i--) - '0' : 0;
            int d2 = j >= 0 ? num2.charAt(j--) - '0' : 0;
            int total = d1 + d2 + carry;
            carry = total / 10;
            steps.add(String.format("Bước %d: %d + %d + nhớ(%d) = %d → ghi %d, nhớ %d",
                    step++, d1, d2, carry, total, total % 10, carry));
        }
        
        log.info("{} + {} = {}", num1, num2, result);

        model.addAttribute("num1", num1);
        model.addAttribute("num2", num2);
        model.addAttribute("result", result);
        model.addAttribute("steps", steps);
        return "index";
    }
}
