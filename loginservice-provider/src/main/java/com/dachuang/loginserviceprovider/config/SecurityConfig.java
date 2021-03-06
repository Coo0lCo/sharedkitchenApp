package com.dachuang.loginserviceprovider.config;

import com.dachuang.loginserviceprovider.feign.GeneratorTokenFeign;
import com.dachuang.loginserviceprovider.filter.LoginVerifyFilter;
import com.dachuang.loginserviceprovider.pojo.LoginVo;
import com.dachuang.loginserviceprovider.utils.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;

/**
 * @Author:SCBC_LiYongJie
 * @time:2021/11/25
 */

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource
    @Lazy
    private LoginVo loginVo;

    @Resource
    private GeneratorTokenFeign generatorTokenFeign;

    @Resource
    private RedisUtil redisUtil;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/sharedkitchen/login").authenticated()
                .anyRequest().permitAll()
                .and()
                .cors()
                .and()
                .addFilterBefore(new LoginVerifyFilter(authenticationManagerBean(),objectMapper(),generatorTokenFeign,redisUtil), UsernamePasswordAuthenticationFilter.class)
                .csrf().disable()               //CRSF????????????????????????session
                .sessionManagement().disable()      //??????session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider()).userDetailsService(loginVo).passwordEncoder(passwordEncoder());
    }

    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @Bean
    protected AuthenticationProvider daoAuthenticationProvider() throws Exception{
        //?????????????????????BCryptPasswordEncoder???????????????????????????????????????createUser???????????????
        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
        daoProvider.setUserDetailsService(loginVo);
        daoProvider.setPasswordEncoder(passwordEncoder());
        return daoProvider;
    }
}
