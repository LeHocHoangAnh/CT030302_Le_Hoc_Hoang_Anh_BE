package com.hrm.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResultPageResponse {
    private Integer totalItems;
    private Integer totalPages;
    private List<?> items;
    private Integer currentPage;
}
