package me.nanjingchj.imgrepo.dto;

import lombok.Value;

import java.util.Date;

@Value
public class SessionStatusDto {
    boolean isLoggedIn;
    Date expiry;
}
