package com.shaorn77770.kpsc_wargame.data_class;

import lombok.Data;

import java.net.URL;

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
        try {
            URL url = new URL(jupyterUrl);
            return "" + url.getPort();
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
