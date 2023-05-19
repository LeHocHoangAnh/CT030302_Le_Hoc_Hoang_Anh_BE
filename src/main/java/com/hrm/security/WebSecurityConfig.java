package com.hrm.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.hrm.model.ERole;
import com.hrm.security.jwt.AuthEntryPointJwt;
import com.hrm.security.jwt.AuthTokenFilter;

@Configuration
@EnableWebSecurity
@EnableWebMvc
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt authEntryPointJwt;

    private static final String[] antMatchersEmployee = {
            "/api/employee/**,/api/bookingDayOff/**,"
    };

    private static final String[] antMatchersLeader = {
            "/api/leader/**"
    };
    private static final String[] antMatchersHR = {
            "/api/hr/**","/api/leader/**"
    };

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedMethods("OPTIONS", "GET", "POST", "PUT", "DELETE").maxAge(3600);
            }
        };
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() 
    {
      CorsConfiguration configuration = new CorsConfiguration();
      configuration.setAllowedOrigins(Arrays.asList("http://118.70.161.162:4200"," http://192.168.0.2:8100","http://localhost:8101" ,"http://192.168.1.158:8080","http://192.168.0.4:8080","http://192.168.157.224:8080","http://192.168.0.12:8080","http://192.168.0.123:8080", "https://localhost:8080","http://localhost:8080","http://localhost:8100", "http://localhost:4200", "http://192.168.150.14:4200", "http://192.168.1.14:4200"));
      configuration.setAllowedMethods(Arrays.asList("HEAD", "PATCH", "OPTIONS", "GET", "POST", "PUT", "DELETE"));
      configuration.setAllowCredentials(true);
      configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
      UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", configuration);
      return source;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().authorizeRequests()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/anonymous-api/**").permitAll()
                .antMatchers("/swagger-ui/**").permitAll()
                .antMatchers("/v2/**").permitAll()
                .antMatchers("/swagger-resources/**").permitAll()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .antMatchers(antMatchersEmployee).fullyAuthenticated()
                .antMatchers(antMatchersHR).hasAnyAuthority(ERole.HR_FLAG.toString())
                .antMatchers(antMatchersLeader).hasAnyAuthority(ERole.LEADER_FLAG.toString()).anyRequest().authenticated().and()
                .exceptionHandling().authenticationEntryPoint(authEntryPointJwt).and().httpBasic();
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}