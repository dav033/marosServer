package io.dav033.maroconstruction.dto.requests;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetContactByNameRequest {
    private String name;
}
