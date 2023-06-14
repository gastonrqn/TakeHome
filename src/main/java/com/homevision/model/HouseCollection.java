package com.homevision.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record HouseCollection(List<House> houses, boolean ok) {
    @JsonCreator
    public HouseCollection(@JsonProperty("houses") List<House> houses, @JsonProperty("ok") boolean ok) {
        this.houses = houses;
        this.ok = ok;
    }
}
