package com.hrm.model.request.employee;

import com.hrm.common.Constants;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class ChangePasswordRequest {
    @NotBlank(message = Constants.VALIDATE_THE_FIELD)
    private String currentPassword;
    @NotBlank(message = Constants.VALIDATE_THE_FIELD)
    private String newPassword;
}
