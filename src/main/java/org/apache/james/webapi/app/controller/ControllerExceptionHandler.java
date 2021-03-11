package org.apache.james.webapi.app.controller;

import org.apache.james.webapi.app.service.InvalidTokenException;
import org.apache.james.webapi.app.service.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler
{

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseMessage handleFormValidationError(InvalidTokenException ex)
    {
        return new ResponseMessage(ex.getMessage(), 403);
    }

    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseMessage handleFormValidationError(ServiceException ex)
    {
        return new ResponseMessage(ex.getMessage(), 400);
    }

    
    
    
}
