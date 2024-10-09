package io.leanddd.module.user.model;

public interface PassEncoder {

    String encode(String raw);

    boolean matches(String raw, String encoded);
}