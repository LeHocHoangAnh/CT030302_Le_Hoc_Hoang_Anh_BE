package com.hrm.model.request;

import com.hrm.common.Constants;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = Constants.VALIDATE_THE_FIELD)
    private String email;

    private String password;
        
    private String token;

}
