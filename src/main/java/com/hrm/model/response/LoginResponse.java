package com.hrm.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private Integer id;
    private String profileName;
    private String email;
    private String token;
    private Long extAt;
    private List<String> roles;
}
