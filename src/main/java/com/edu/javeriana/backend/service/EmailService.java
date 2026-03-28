package com.edu.javeriana.backend.service;

import com.edu.javeriana.backend.service.interfaces.*;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void enviarInvitacion(String destinatario, String passwordTemporal, String empresaNombre, String rol) {
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(fromEmail); 

            mensaje.setTo(destinatario);
            mensaje.setSubject("Has sido invitado a " + empresaNombre);
            
            String texto = "¡Hola!\n\n"
                         + "Has sido invitado como " + rol + " para colaborar en la documentación de procesos de la empresa " + empresaNombre + ".\n\n"
                         + "Tus credenciales de acceso son las siguientes:\n"
                         + "Email: " + destinatario + "\n"
                         + "Contraseña temporal: " + passwordTemporal + "\n\n"
                         + "Por favor, ingresa a la plataforma utilizando estas credenciales y cambia tu contraseña.\n\n"
                         + "El equipo de Aventureros.";
            
            mensaje.setText(texto);
            
            log.info("Enviando correo de invitación a: {}", destinatario);
            mailSender.send(mensaje);
            log.info("Correo de invitación enviado exitosamente a: {}", destinatario);

        } catch (Exception e) {
            log.error("Error al enviar el correo de invitación a {}: {}", destinatario, e.getMessage());
            // Dependiendo de tu regla de negocio, podrías relanzar la excepción para revertir la creación del usuario
            // throw new RuntimeException("No se pudo enviar el correo de invitación", e);
        }
    }
}
