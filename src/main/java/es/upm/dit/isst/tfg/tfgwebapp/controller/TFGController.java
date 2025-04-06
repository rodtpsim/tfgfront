package es.upm.dit.isst.tfg.tfgwebapp.controller;

import java.security.Principal;
import java.util.*;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import es.upm.dit.isst.tfg.tfgwebapp.model.Estado;
import es.upm.dit.isst.tfg.tfgwebapp.model.TFG;

@Controller
@RequestMapping
public class TFGController {
    private static final Logger log = Logger.getLogger(TFGController.class.getName());
    public final String TFGMANAGER_STRING;
    public static final String VISTA_LISTA = "lista";
    public static final String VISTA_FORMULARIO = "formulario";

    private RestTemplate restTemplate = new RestTemplate();

    public TFGController(@Value("${tfgmanager.server}") String TFGMANAGER_STRING) {
        this.TFGMANAGER_STRING = TFGMANAGER_STRING;
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:/" + VISTA_LISTA;
    }

    @GetMapping("/login")
    public String login() {
        return "redirect:/" + VISTA_LISTA;
    }

    @GetMapping("/lista")
    public String lista(Model model, Principal principal) {
        log.info("lista");
        List<TFG> lista = new ArrayList<>();
        if (principal == null || principal.getName().equals("") || principal.getName().equals("admin@upm.es"))
            lista = Arrays.asList(restTemplate.getForEntity(TFGMANAGER_STRING, TFG[].class).getBody());
        else if (principal.getName().contains("@upm.es"))
            lista = Arrays.asList(restTemplate.getForEntity(
                    TFGMANAGER_STRING + "?tutor=" + principal.getName(), TFG[].class).getBody());
        else if (principal.getName().contains("@alumnos.upm.es")) {
            try {
                TFG tfg = restTemplate.getForObject(TFGMANAGER_STRING + "/" + principal.getName(), TFG.class);
                if (tfg != null)
                    lista.add(tfg);
            } catch (Exception e) {
                log.info("Error en lista: " + e);
            }
        }
        model.addAttribute("tfgs", lista);
        return VISTA_LISTA;
    }

    @GetMapping("/crear")
    public String crear(Map<String, Object> model) {
        TFG TFG = new TFG();
        model.put("TFG", TFG);
        model.put("accion", "guardar");
        return VISTA_FORMULARIO;
    }

    @PostMapping("/guardar")
    public String guardar(@Validated TFG TFG, BindingResult result) {
        log.info("guardar TFG: " + TFG);
        if (result.hasErrors())
            return VISTA_FORMULARIO;
        try {
            restTemplate.postForObject(TFGMANAGER_STRING, TFG, TFG.class);
        } catch (Exception e) {
            log.info("Error en guardar: " + e);
        }
        return "redirect:" + VISTA_LISTA;
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable String id, Map<String, Object> model, Principal principal) {
        log.info("editar");
        if (principal == null || (!principal.getName().equals(id) && !principal.getName().equals("admin@upm.es")))
            return "redirect:/" + VISTA_LISTA;
        TFG tfg = null;
        try {
            tfg = restTemplate.getForObject(TFGMANAGER_STRING + "/" + id, TFG.class);
        } catch (HttpClientErrorException.NotFound ex) {
            log.info("Error en editar: " + ex);
        }
        model.put("TFG", tfg);
        model.put("accion", "../actualizar");
        return tfg != null ? VISTA_FORMULARIO : "redirect:/" + VISTA_LISTA;
    }

    @PostMapping("/actualizar")
    public String actualizar(@Validated TFG tfg, BindingResult result) {
        if (result.hasErrors()) {
            return VISTA_FORMULARIO;
        }
        try {
            restTemplate.put(TFGMANAGER_STRING + "/" + tfg.getAlumno(), tfg, TFG.class);
        } catch (Exception e) {
            log.info("Error en actualizar: " + e);
        }
        return "redirect:" + VISTA_LISTA;
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id) {
        restTemplate.delete(TFGMANAGER_STRING + "/" + id);
        return "redirect:/" + VISTA_LISTA;
    }

    @GetMapping("/aceptar/{id}")
    public String aceptar(@PathVariable String id, Map<String, Object> model, Principal principal) {
        if (principal != null) {
            try {
                TFG tfg = restTemplate.getForObject(TFGMANAGER_STRING + "/" + id, TFG.class);
                if (tfg != null && principal.getName().equals(tfg.getTutor())) {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<TFG> requestEntity = new HttpEntity<>(tfg, headers);
                    restTemplate.exchange(
                            TFGMANAGER_STRING + "/" + tfg.getAlumno() + "/estado/" + Estado.ACEPTADOPORTUTOR,
                            HttpMethod.PUT, requestEntity, TFG.class);
                    model.put("TFG", tfg);
                }
            } catch (HttpClientErrorException.NotFound ex) {
                log.info("Error en aceptar: " + ex);
            }
        }
        return "redirect:/" + VISTA_LISTA;
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("alumno") String alumno,
                             @RequestParam("file") MultipartFile file, Principal principal) {
        log.info("uploadFile: " + alumno + " " + file.getOriginalFilename() + " " + file.getSize() + " " + file.getContentType());
        if (principal == null || principal.getName() == null || file.isEmpty())
            return "redirect:/" + VISTA_LISTA;

        if (principal.getName().equals(alumno) || SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            try {
                TFG tfg = restTemplate.getForObject(TFGMANAGER_STRING + "/" + alumno, TFG.class);
                if (tfg != null && tfg.getEstado() == Estado.APROBADOPORCOA) {
                    tfg.setEstado(Estado.SOLICITADADEFENSA);
                    ResponseEntity<TFG> respuestaEstado = restTemplate.exchange(
                            TFGMANAGER_STRING + "/" + alumno + "/estado/" + tfg.getEstado(),
                            HttpMethod.PUT, null, TFG.class);
                    if (respuestaEstado.getStatusCode() == HttpStatus.OK) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(file.getContentType().isEmpty() ?
                                MediaType.APPLICATION_OCTET_STREAM :
                                MediaType.parseMediaType(file.getContentType()));
                        restTemplate.exchange(
                                TFGMANAGER_STRING + "/" + alumno + "/memoria",
                                HttpMethod.PUT,
                                new HttpEntity<>(file.getBytes(), headers),
                                byte[].class);
                    }
                }
            } catch (Exception e) {
                log.info("Error en upload: " + e);
            }
        }
        return "redirect:/" + VISTA_LISTA;
    }

    @GetMapping("/download/{alumno}")
    @ResponseBody
    public ResponseEntity<ByteArrayResource> getFile(@PathVariable String alumno) {
        try {
            TFG tfg = restTemplate.getForObject(TFGMANAGER_STRING + "/" + alumno, TFG.class);
            if (tfg != null && tfg.getMemoria() != null) {
                ResponseEntity<byte[]> respuestaMemoria = restTemplate.exchange(
                        TFGMANAGER_STRING + "/" + alumno + "/memoria",
                        HttpMethod.GET, null, byte[].class);
                ByteArrayResource resource = new ByteArrayResource(respuestaMemoria.getBody());
                if (respuestaMemoria.getStatusCode() == HttpStatus.OK) {
                    HttpHeaders header = new HttpHeaders();
                    header.setContentType(new MediaType("application", "force-download"));
                    header.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"TFG.pdf\"");
                    return new ResponseEntity<>(resource, header, HttpStatus.OK);
                }
            }
        } catch (Exception e) {
            log.info("Error en getFile: " + e);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
