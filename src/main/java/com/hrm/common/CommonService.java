package com.hrm.common;

import com.hrm.security.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class CommonService {

    public UserDetails userDetails() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal);
        } else {
            return null;
        }
    }

    public Integer idUserAccountLogin() {
        UserDetailsImpl userDetails = (UserDetailsImpl) userDetails();
        if (userDetails == null) {
            return null;
        }
        return userDetails.getId();
    }

}
