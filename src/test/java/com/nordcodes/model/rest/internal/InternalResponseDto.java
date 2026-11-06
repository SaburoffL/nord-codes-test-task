package com.nordcodes.model.rest.internal;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
@Setter
public class InternalResponseDto {
    StatusCode result;
    String message;

    public enum StatusCode {
        OK, ERROR
    }
}
