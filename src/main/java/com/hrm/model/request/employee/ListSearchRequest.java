package com.hrm.model.request.employee;

import com.hrm.model.request.hr.ListEmployeeRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ListSearchRequest {
    private List<Integer> idList;
    private ListEmployeeRequest searchValues;

    @Override
    public String toString() {
        return "ListSearchRequest{" +
                "idList=" + idList +
                ", searchValue=" + searchValues +
                '}';
    }
}
