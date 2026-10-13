package com.sto.autoservice.web.controller;

import com.sto.autoservice.core.entity.DeadlineType;
import com.sto.autoservice.web.dto.response.DeadlineOptionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/meta")
@Tag(name = "Meta", description = "Справочники")
public class MetaController {

    @GetMapping("/deadlines")
    @Operation(summary = "Варианты сроков выполнения и их коэффициенты")
    public List<DeadlineOptionDto> deadlines() {
        return Arrays.stream(DeadlineType.values()).map(DeadlineOptionDto::from).toList();
    }
}
