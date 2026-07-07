package com.mauricio.calculator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Configuration
public class CalculatorController {

	@Value("${app.message.error}")
	private String errorMessage;

	@Value("${db.server}")
	private String dbServer;

	@Value("${db.user}")
	private String dbUser;

	@GetMapping("/add/{a}/{b}")
	public CalculationResponse add(@PathVariable int a, @PathVariable int b) {
		CalculationResponse res = new CalculationResponse();
		res.setA(a);
		res.setB(b);
		res.setResult(a + b);
		return res;
	}

	@GetMapping("/subtract/{a}/{b}")
	public CalculationResponse subtract(@PathVariable int a, @PathVariable int b) {
		return new CalculationResponse(a, b, a - b);
	}

	@GetMapping("/divide/{a}/{b}")
	public CalculationResponse divide(@PathVariable int a, @PathVariable int b) {
		if (b == 0) {
			return new CalculationResponse(a, b, errorMessage);
		}
		return new CalculationResponse(a, b, a / b);
	}

	@GetMapping("/")
	public String getInfo() {
		return dbUser + "," + dbServer;
	}

}
