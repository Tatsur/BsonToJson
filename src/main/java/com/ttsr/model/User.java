package com.ttsr.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class User implements Serializable {
    @JsonIgnore
    private JsonNode _id;
    private String oid;
    private String name;
    private String lastConnectedDateRaw;
    private Date lastConnectedDate;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public User (@JsonProperty("_id") JsonNode _id, @JsonProperty("name") String name){
        this.oid = _id.path("$oid").textValue();
        this._id = _id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{oid='" + oid + '\'' +
                ", name='" + name + '\'' +
                ", lastConnectedDate='" + lastConnectedDateRaw + '\'' +
                '}';
    }
}
