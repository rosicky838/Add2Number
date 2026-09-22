package com.add2numweb;

import com.add2numweb.dto.CalculationResult;
import com.add2numweb.service.BigNumberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class Add2NumController {

    private final BigNumberService bigNumberService;

    public Add2NumController(BigNumberService bigNumberService) {
        this.bigNumberService = bigNumberService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/calculate")
    public String calculate(@RequestParam(required = false) String num1,
                            @RequestParam(required = false) String num2,
                            Model model) {
        // Giữ lại input đã nhập ngay cả khi có lỗi xảy ra
        model.addAttribute("num1", num1 != null ? num1.trim() : "");
        model.addAttribute("num2", num2 != null ? num2.trim() : "");

        try {
            CalculationResult result = bigNumberService.calculate(num1, num2);
            model.addAttribute("result", result.getResult());
            model.addAttribute("steps", result.getSteps());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
        }

        return "index";
    }

    @PostMapping(value = "/api/calculate", produces = "application/json")
    @ResponseBody
    public Map<String, Object> calculateApi(@RequestParam(required = false) String num1,
                                           @RequestParam(required = false) String num2) {
        Map<String, Object> response = new HashMap<>();
        try {
            CalculationResult result = bigNumberService.calculate(num1, num2);
            response.put("success", true);
            response.put("num1", result.getNum1());
            response.put("num2", result.getNum2());
            response.put("result", result.getResult());
            response.put("steps", result.getSteps());
        } catch (IllegalArgumentException ex) {
            response.put("success", false);
            response.put("error", ex.getMessage());
        }
        return response;
    }
}
