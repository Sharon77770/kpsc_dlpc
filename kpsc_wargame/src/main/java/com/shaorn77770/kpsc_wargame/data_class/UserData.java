package com.shaorn77770.kpsc_wargame.data_class;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
public class UserData {

    @Id
    private String apiKey;

    private String userName;
    private String phone;
    private String major;
    private int studentNumber;
    private boolean allow;
    private String jupyterUrl;
    
    public String getPort() {
        if(jupyterUrl == null || jupyterUrl.isEmpty())
            return null;

        return jupyterUrl.split(":")[2].split("/")[0]; 
    }
}
