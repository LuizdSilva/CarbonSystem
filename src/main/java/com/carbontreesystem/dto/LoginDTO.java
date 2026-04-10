package com.carbontreesystem.dto;

public class LoginDTO {
    private String usuario;
    private String senha;

    // Construtor padrão
    public LoginDTO() {}

    // Getters e Setters
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}