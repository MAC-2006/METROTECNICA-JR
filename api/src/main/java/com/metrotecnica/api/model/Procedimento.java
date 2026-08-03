package com.metrotecnica.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "procedimentos")
public class Procedimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String codigo;

    @Column(length = 100)
    private String nome;

    @Column(name = "texto_base", columnDefinition = "TEXT")
    private String textoBase;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTextoBase() { return textoBase; }
    public void setTextoBase(String textoBase) { this.textoBase = textoBase; }
}