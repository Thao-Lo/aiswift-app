package com.aiswift.Exception;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.aiswift.DTO.Tenant.ErrorResponse;
import com.google.zxing.WriterException;
import com.paypal.base.rest.PayPalRESTException;

import jakarta.persistence.EntityNotFoundException;

@ControllerAdvice //Automatically applied to all controllers
public class GlobalExceptionHandler {

	 	@ExceptionHandler(EntityNotFoundException.class)
	    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
	        ErrorResponse error = new ErrorResponse(
	            HttpStatus.NOT_FOUND.value(), 
	            ex.getMessage(), 
	            System.currentTimeMillis()
	        );
	        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	    }
	 	
	 	 @ExceptionHandler({
	         IllegalArgumentException.class, 
	         FileProcessingException.class,
	         IllegalStateException.class
	     })
	     public ResponseEntity<ErrorResponse> handleBadRequestExceptions(Exception ex) {
	         ErrorResponse error = new ErrorResponse(
	             HttpStatus.BAD_REQUEST.value(), 
	             ex.getMessage(), 
	             System.currentTimeMillis()
	         );
	         return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	     }
	     
	     // Validation error handler, go with @Valid
	     @ExceptionHandler(MethodArgumentNotValidException.class)
	     public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
	         List<String> errors = ex.getBindingResult()
	             .getFieldErrors()
	             .stream()
	             .map(error -> error.getField() + ": " + error.getDefaultMessage())
	             .collect(Collectors.toList());

	         ErrorResponse error = new ErrorResponse(
	             HttpStatus.BAD_REQUEST.value(), 
	             "Validation failed: " + String.join(", ", errors), 
	             System.currentTimeMillis()
	         );
	         return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	     }
	     
	     @ExceptionHandler({
	    	    MethodArgumentTypeMismatchException.class,
	    	    MissingServletRequestParameterException.class
	    	})
	    	public ResponseEntity<ErrorResponse> handleArgumentExceptions(Exception ex) {
	    	    ErrorResponse error = new ErrorResponse(
	    	        HttpStatus.BAD_REQUEST.value(), 
	    	        "Invalid or missing request parameter: " + ex.getMessage(), 
	    	        System.currentTimeMillis()
	    	    );
	    	    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	    	}
	     
	     // Catch-all method for unexpected exceptions
	     @ExceptionHandler(Exception.class)
	     public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
	         ErrorResponse error = new ErrorResponse(
	             HttpStatus.INTERNAL_SERVER_ERROR.value(), 
	             "An unexpected error occurred: " + ex.getMessage(), 
	             System.currentTimeMillis()
	         );
	         return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	     }
	     
	     @ExceptionHandler(PayPalRESTException.class)
	     public ResponseEntity<ErrorResponse> handlePayPalRESTException(PayPalRESTException ex) {
	         ErrorResponse error = new ErrorResponse(
	             HttpStatus.BAD_REQUEST.value(), 
	             ex.getMessage(), 
	             System.currentTimeMillis()
	         );
	         return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	     }
	     
	     @ExceptionHandler(WriterException.class)
	     public ResponseEntity<ErrorResponse> handleQRCodeWriterException(WriterException ex) {
	         ErrorResponse error = new ErrorResponse(
	             HttpStatus.INTERNAL_SERVER_ERROR.value(), 
	             "Error generating QR code: " + ex.getMessage(), 
	             System.currentTimeMillis()
	         );
	         return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	     }

	     @ExceptionHandler(IOException.class)
	     public ResponseEntity<ErrorResponse> handleIOException(IOException ex) {
	         ErrorResponse error = new ErrorResponse(
	             HttpStatus.INTERNAL_SERVER_ERROR.value(), 
	             "IO Error processing QR code: " + ex.getMessage(), 
	             System.currentTimeMillis()
	         );
	         return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
	     }
	     

	     @ExceptionHandler(RuntimeException.class)
	     public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
	         ErrorResponse error = new ErrorResponse(
	             HttpStatus.BAD_REQUEST.value(), 
	             ex.getMessage(), 
	             System.currentTimeMillis()
	         );
	         return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
	     }
	     
	     @ExceptionHandler(NoDataFoundException.class)
	     public ResponseEntity<ErrorResponse> handleNoDataFoundException(NoDataFoundException ex) {
	         ErrorResponse error = new ErrorResponse(
	             HttpStatus.NOT_FOUND.value(), 
	             ex.getMessage(), 
	             System.currentTimeMillis()
	         );
	         return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	     }
	     
	     @ExceptionHandler(UnAuthorizedException.class)
	    	 public ResponseEntity<ErrorResponse> handleUnAuthorizedException(UnAuthorizedException ex) {
	    		 ErrorResponse error = new ErrorResponse(
	    				 HttpStatus.UNAUTHORIZED.value(),
	    				 ex.getMessage(),
	    				 System.currentTimeMillis()	    				 
	    	);
	    	return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);		 
	    }
	     
	     @ExceptionHandler(UnExpectedStatusException.class)
    	 public ResponseEntity<ErrorResponse> handleUnExpectedStatusException(UnExpectedStatusException ex) {
    		 ErrorResponse error = new ErrorResponse(
    				 HttpStatus.BAD_REQUEST.value(),
    				 ex.getMessage(),
    				 System.currentTimeMillis()	    				 
    	);
    	return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);		 
    }
	     
	     
}
