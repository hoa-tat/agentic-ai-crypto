package com.hoatat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemoryMessage {
    private String role;
    private String content;

    @Override
    public String toString() {
        return role + ": " + content;
    }
}
