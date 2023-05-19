package com.hrm.security;

import com.hrm.entity.Employee;
import com.hrm.model.ERole;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class UserDetailsImpl implements UserDetails {

    private Integer id;
    private String email;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Integer id,String email, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(Employee employee) {
        Set<String> roles = new HashSet<>();
        Boolean hrFlag = employee.getRoleGroup().getHrFlag();
        Boolean leaderFlag = employee.getRoleGroup().getLeaderFlag();
        Boolean subLeaderFlag = employee.getRoleGroup().getSubLeaderFlag();
        Boolean comtorFlag = employee.getRoleGroup().getComtorFlag();
        Boolean customerFlag = employee.getRoleGroup().getCustomerFlag();
        if (Boolean.TRUE.equals(hrFlag)) {
            roles.add(ERole.HR_FLAG.toString());
        }
        if (Boolean.TRUE.equals(leaderFlag)) {
            roles.add(ERole.LEADER_FLAG.toString());
        }
        if (Boolean.TRUE.equals(subLeaderFlag)) {
            roles.add(ERole.SUB_LEADER_FLAG.toString());
        }
        if (Boolean.TRUE.equals(comtorFlag)) {
            roles.add(ERole.COMTOR_FLAG.toString());
        }
        if (Boolean.TRUE.equals(customerFlag)) {
            roles.add(ERole.CUSTOMER_FLAG.toString());
        }
        List<GrantedAuthority> listRoles = roles.stream().map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return new UserDetailsImpl(employee.getId(), employee.getEmail(), employee.getPassword(), listRoles);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
