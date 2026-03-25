package com.lothuspay.events.dto.email;

import lombok.*;

import java.util.HashMap;
import java.util.List;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailSend {

    private String from;
    private String to;

    private String slug;

    private HashMap<String, String> variables;

}
