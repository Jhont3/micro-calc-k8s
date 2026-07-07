package com.mauricio.calculator;

public class CalculationResponse {
	private int a;
	private int b;
	private int result;
	private String error = "NO";

	public CalculationResponse() {
	}

	public CalculationResponse(int a, int b, int result) {
		this.a = a;
		this.b = b;
		this.result = result;
	}

	public CalculationResponse(int a, int b, String error) {
		this.a = a;
		this.b = b;
		this.error = error;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public int getA() {
		return a;
	}

	public void setA(int a) {
		this.a = a;
	}

	public int getB() {
		return b;
	}

	public void setB(int b) {
		this.b = b;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

}
