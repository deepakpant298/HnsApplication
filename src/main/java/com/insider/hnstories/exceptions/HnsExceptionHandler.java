package com.insider.hnstories.exceptions;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.insider.hnstories.entity.ExceptionResponse;
import com.insider.hnstories.util.ApiConstants;


@ControllerAdvice
public class HnsExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(value = NoRecordFoundException.class)
	public ResponseEntity<Object> handleNoRecordFoundException(NoRecordFoundException ex) {
		return new ResponseEntity<>(new ExceptionResponse(new Date(), ex.getMessage()), HttpStatus.OK);
	}

	@ExceptionHandler(value = ConnectionException.class)
	public ResponseEntity<Object> handleConnectionException(ConnectionException ex) {
		return new ResponseEntity<>(new ExceptionResponse(new Date(), ApiConstants.FIREBASE_CONNECT_ERROR_MESSAGE),
				HttpStatus.BAD_GATEWAY);
	}

	@ExceptionHandler(value = RuntimeException.class)
	public ResponseEntity<Object> handleRuntimeExceptionRuntimeException(RuntimeException ex) {
		return new ResponseEntity<>(new ExceptionResponse(new Date(), ApiConstants.INTERNAL_SERVER_ERROR_MESSAGE),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
