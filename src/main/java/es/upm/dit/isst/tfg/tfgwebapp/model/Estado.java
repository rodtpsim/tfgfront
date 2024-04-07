package es.upm.dit.isst.tfg.tfgwebapp.model;

public enum Estado {

    SOLICITADO, 
    ACEPTADOPORTUTOR, 
    APROBADOPORCOA,
    RECHAZADO, 
    SOLICITADADEFENSA,
    PROPUESTADEFENSA,
    PROGRAMADADEFENSA,
    CALIFICADO;

    public static Estado getEstado(int i) {
        return Estado.values()[i];
    }

}
