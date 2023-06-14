package com.homevision.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record House(Long id, String address, String homeOwner, Long price, String photoUrl) {
    @JsonCreator
    public House(@JsonProperty("id") Long id, @JsonProperty("address") String address,
                 @JsonProperty("homeowner") String homeOwner, @JsonProperty("price") Long price,
                 @JsonProperty("photoURL") String photoUrl) {
        this.id = id;
        this.address = address;
        this.homeOwner = homeOwner;
        this.price = price;
        this.photoUrl = photoUrl;
    }
}
