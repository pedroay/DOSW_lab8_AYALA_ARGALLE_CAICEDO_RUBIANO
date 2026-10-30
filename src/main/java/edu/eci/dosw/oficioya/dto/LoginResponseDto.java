package edu.eci.dosw.oficioya.dto;

public class LoginResponseDto {

    private String tipo;
    private String accessToken;
    private long expiraEn;

    public LoginResponseDto() {}

    public LoginResponseDto(String tipo, String accessToken, long expiraEn) {
        this.tipo = tipo;
        this.accessToken = accessToken;
        this.expiraEn = expiraEn;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getExpiraEn() {
        return expiraEn;
    }

    public void setExpiraEn(long expiraEn) {
        this.expiraEn = expiraEn;
    }
}

