package oficina.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "enderecos")
public class Endereco implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String rua;

    @Column(nullable = false, length = 20)
    private String numero;

    @Column(nullable = false, length = 60)
    private String bairro;

    @Column(nullable = false, length = 60)
    private String cidade;

    @Column(length = 20)
    private String cep;

    @OneToOne(mappedBy = "endereco")
    private Cliente cliente;

    public Endereco() {}

    public Endereco(String rua, String numero, String bairro, String cidade, String cep) {
        this.rua = rua;
        this.numero = numero;
        this.bairro = bairro;
        this.cidade = cidade;
        this.cep = cep;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (rua != null && !rua.isEmpty()) sb.append(rua);
        if (numero != null && !numero.isEmpty()) sb.append(", ").append(numero);
        if (bairro != null && !bairro.isEmpty()) sb.append(" - ").append(bairro);
        if (cidade != null && !cidade.isEmpty()) sb.append(", ").append(cidade);
        if (cep != null && !cep.isEmpty()) sb.append(" (CEP: ").append(cep).append(")");
        return sb.toString();
    }
}

