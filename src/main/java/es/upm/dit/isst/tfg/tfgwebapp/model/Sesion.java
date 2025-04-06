package es.upm.dit.isst.tfg.tfgwebapp.model;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public class Sesion {
    private Long id;

    @Future
    private Date fecha;

    private String lugar;

    @Size(min = 3, max = 3)
    private List<@Email @NotEmpty String> tribunal;

    @JsonIgnore
    private List<@Valid TFG> tfgs;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }

    public List<String> getTribunal() { return tribunal; }
    public void setTribunal(List<String> tribunal) { this.tribunal = tribunal; }

    public List<TFG> getTfgs() { return tfgs; }
    public void setTfgs(List<TFG> tfgs) { this.tfgs = tfgs; }
}
