package com.example.delogica.exceptions;

import com.example.delogica.config.exceptions.*;
import com.example.delogica.config.GlobalExceptionHandler;
import com.example.delogica.config.errors.ErrorCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Controlador dummy para provocar cada excepción
@RestController
@Validated
@RequestMapping("/_ex")
class DummyController {
    record CreateDTO(@NotNull Long customerId) {
    }

    @PostMapping("/valid")
    public void create(@Valid @RequestBody CreateDTO dto) {
    }

    @GetMapping("/violation")
    public void violation(@RequestParam @Min(1) int page) {
    }

    @GetMapping("/nf")
    public void notFound() {
        throw new ResourceNotFoundException("x");
    }

    @GetMapping("/conflict")
    public void conflict() {
        throw new EmailAlreadyInUseException("mail");
    }
}

@WebMvcTest(controllers = DummyController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerWebTest {

    @Autowired
    MockMvc mvc;

    @Test
    void methodArgumentNotValid_returns400_withDetails() throws Exception {
        // Falta customerId -> viola @NotNull
        mvc.perform(post("/_ex/valid").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.details[0].field").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/_ex/valid"));
    }

    @Test
    void constraintViolation_returns400() throws Exception {
        // page=0 viola @Min(1)
        mvc.perform(get("/_ex/violation").param("page", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));
    }

    @Test
    void notFound_returns404() throws Exception {
        mvc.perform(get("/_ex/nf"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value("x"));
    }

    @Test
    void conflict_returns409() throws Exception {
        mvc.perform(get("/_ex/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.CONFLICT.name()));
    }
}
