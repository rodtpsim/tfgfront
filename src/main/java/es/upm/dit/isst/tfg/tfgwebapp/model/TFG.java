package es.upm.dit.isst.tfg.tfgwebapp.model;

import java.net.URI;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.DecimalMax;

public class TFG {
    @Email
    private String alumno;

    @Email
    private String tutor;

    @NotEmpty
    private String titulo;

    private String resumen;
    private Estado estado;
    private URI memoria;

    @PositiveOrZero
    @DecimalMax("10.0")
    private Double calificacion;

    private Boolean matriculaHonor;
    private Sesion sesion;

    // Getters y Setters
    public String getAlumno() { return alumno; }
    public void setAlumno(String alumno) { this.alumno = alumno; }

    public String getTutor() { return tutor; }
    public void setTutor(String tutor) { this.tutor = tutor; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getResumen() { return resumen; }
    public void setResumen(String resumen) { this.resumen = resumen; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public URI getMemoria() { return memoria; }
    public void setMemoria(URI memoria) { this.memoria = memoria; }

    public Double getCalificacion() { return calificacion; }
    public void setCalificacion(Double calificacion) { this.calificacion = calificacion; }

    public Boolean getMatriculaHonor() { return matriculaHonor; }
    public void setMatriculaHonor(Boolean matriculaHonor) { this.matriculaHonor = matriculaHonor; }

    public Sesion getSesion() { return sesion; }
    public void setSesion(Sesion sesion) { this.sesion = sesion; }
}
