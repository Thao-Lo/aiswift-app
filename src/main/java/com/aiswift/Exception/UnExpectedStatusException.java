package com.aiswift.Exception;


public class UnExpectedStatusException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	public UnExpectedStatusException (String message) {
		super(message);
	}

}
