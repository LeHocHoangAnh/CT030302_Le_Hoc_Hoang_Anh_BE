package com.hrm.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hrm.common.Constants;
import com.hrm.entity.Employee;
import com.hrm.repository.EmployeeRepository;



@Component
public class AutoOnLeaveService {
    
    @Autowired
    EmployeeRepository employeeRepository;
    
 // * * * * * * -> (second, minute, hour, day of month, month, day(s) of week)
    @Scheduled(cron = "0 0 0 1 * *")
    public void plusOnPaidLeave() {
        List<Employee> listEmployee = employeeRepository.findAllByDeleteFlag(Constants.DELETE_NONE);
        for (Employee employee : listEmployee) {
            if(employee.getPaidLeave() == null) {
                employee.setPaidLeave((float) Constants.COMPLETED);
            }else {
                employee.setPaidLeave(employee.getPaidLeave() + Constants.COMPLETED);
            }
           employee.setCommonUpdate();
        }
        employeeRepository.saveAll(listEmployee);
    }
}
