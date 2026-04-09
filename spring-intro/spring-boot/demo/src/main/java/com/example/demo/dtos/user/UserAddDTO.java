package com.example.demo.dtos.user;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class UserAddDTO {
    private String name;
    private String username;
    private String password;
    private List<Long> roleIds = new ArrayList<>();
}