package io.dav033.maroconstruction.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contacts {
    private Long id;
    private String companyName;
    private String name;
    private String occupation;
    private String product;
    private String phone;
    private String email;
    private String address;
    private LocalDateTime lastContact;
}
