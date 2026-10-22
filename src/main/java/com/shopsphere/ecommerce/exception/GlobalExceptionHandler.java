package com.shopsphere.ecommerce.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        log.warn("Resource not found: {}", ex.getMessage());
        model.addAttribute("errorTitle", "404 - Page or Resource Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("statusCode", 404);
        return "error";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleStaticResourceNotFound(NoResourceFoundException ex, Model model) {
        model.addAttribute("errorTitle", "404 - Page Not Found");
        model.addAttribute("errorMessage", "The requested page or static resource does not exist.");
        model.addAttribute("statusCode", 404);
        return "error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
        log.warn("Invalid argument: {}", ex.getMessage());
        model.addAttribute("errorTitle", "Invalid Request");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("statusCode", 400);
        return "error";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(IllegalStateException ex, Model model) {
        log.warn("Illegal state: {}", ex.getMessage());
        model.addAttribute("errorTitle", "Operation Not Allowed");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("statusCode", 400);
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        log.error("Unhandled application exception", ex);
        model.addAttribute("errorTitle", "500 - Internal Server Error");
        model.addAttribute("errorMessage", "An unexpected error occurred while processing your request. Please try again or contact support.");
        model.addAttribute("statusCode", 500);
        return "error";
    }
}
