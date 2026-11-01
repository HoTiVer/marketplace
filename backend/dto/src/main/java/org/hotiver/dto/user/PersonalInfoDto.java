package org.hotiver.dto.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Builder
@Getter
@Setter
public class PersonalInfoDto {

    private String email;
    private String displayName;
    private Date registerDate;
    //private Integer countOfOrders;
    private Boolean isSeller;
    private String sellerNickname;

}
