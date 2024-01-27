package com.inn.cafe.serviceImpl;

import com.inn.cafe.JWT.CustomerUsersDetailsService;
import com.inn.cafe.JWT.JwtFilter;
import com.inn.cafe.JWT.JwtUtil;
import com.inn.cafe.POJO.User;
import com.inn.cafe.constants.cafeConstants;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.service.UserService;
import com.inn.cafe.utils.CafeUtils;
import com.inn.cafe.utils.EmailUtils;
import com.inn.cafe.wrapper.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.net.Authenticator;
import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomerUsersDetailsService customerUsersDetailsService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    EmailUtils emailUtils;

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {

        try {
            log.info("Inside SignUp {}", requestMap);
            if (validateSignUp(requestMap)) {
                User user = userDao.findByEmailId(requestMap.get("email"));
                if (Objects.isNull(user)) {
                    userDao.save(getUserFromMap(requestMap));
                    return CafeUtils.getResponseEntity("Successfully Registered", HttpStatus.ACCEPTED);
                } else {
                    return CafeUtils.getResponseEntity("Email Exists.", HttpStatus.BAD_REQUEST);
                }
            } else {
                return CafeUtils.getResponseEntity(cafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return CafeUtils.getResponseEntity(cafeConstants.SOMETHING_WENT_WRONG, HttpStatus.BAD_REQUEST);


    }


    private boolean validateSignUp(Map<String, String> requestMap) {
        if (requestMap.containsKey("name") && requestMap.containsKey("contact") &&
                requestMap.containsKey("email") && requestMap.containsKey("password")) {

            return true;

        }
        return false;

    }

    private User getUserFromMap(Map<String, String> requestMap) {
        User user = new User();
        user.setName(requestMap.get("name"));
        user.setContact(requestMap.get("name"));
        user.setEmail(requestMap.get("email"));
        user.setPassword(requestMap.get("password"));
        user.setStatus("false");
        user.setRole("user");

        return user;

    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        try {
            log.info("Inside Login ");

            Authentication authentication = authenticationManager.
                    authenticate(new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password")));

            if (authentication.isAuthenticated()){
                if(customerUsersDetailsService.getUserDetail().getStatus().equalsIgnoreCase("true")){
                    return new ResponseEntity<String>("{\"token\":\""+ jwtUtil.generatrToken(customerUsersDetailsService.getUserDetail().getEmail(),
                            customerUsersDetailsService.getUserDetail().getRole()) + "\"}", HttpStatus.OK);
                }
                else {
                    new ResponseEntity<String>("{\"message\":\"" + "Wait for admin approval" + "\"}", HttpStatus.BAD_REQUEST);
                }
            }

        } catch (Exception e) {
            log.error("{}", e);
        }
        return CafeUtils.getResponseEntity(cafeConstants.INVALID_CREDENTIALS, HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUser() {
        try {
            if(jwtFilter.isAdmin()){

                return new ResponseEntity<>(userDao.getAllUser(), HttpStatus.OK);

            }else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try {
            if(jwtFilter.isAdmin()){
                Optional<User> userOp = userDao.findById(Integer.parseInt(requestMap.get("id")));
                if (! userOp.isEmpty()) {
                    userDao.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
                    sendMailTOAllUsers(requestMap.get("ststus"), userOp.get().getEmail(), userDao.getAllAdmin());
                    return CafeUtils.getResponseEntity("User status updated successfully.", HttpStatus.OK);
                }else {
                    return CafeUtils.getResponseEntity("User Id doesn't exist.", HttpStatus.OK);
                }
            }else {
                return CafeUtils.getResponseEntity(cafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(cafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void sendMailTOAllUsers(String status, String user, List<String> allAdmin) {
        allAdmin.remove(jwtFilter.getCurrentUser());
        if(status!=null && status.equalsIgnoreCase("true")){
           emailUtils.remaider(jwtFilter.getCurrentUser(), "Account Approved.", "USER:- " +user+ "\n is Approved By \n ADMIN:-" +jwtFilter.getCurrentUser(), allAdmin);
        }else
        {
            emailUtils.remaider(jwtFilter.getCurrentUser(), "Account Disabled.", "USER:- " +user+ "\n is Disabled By \n ADMIN:-" +jwtFilter.getCurrentUser(), allAdmin);
        }

    }
}