package com.ttsr.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ServerOutput implements Serializable {

    private String output;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ServerOutput(@JsonProperty("output") String output){
        this.output = output;
    }
}
