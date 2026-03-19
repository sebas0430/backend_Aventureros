package com.edu.javeriana.backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void enviarInvitacion(String destinatario, String passwordTemporal, String empresaNombre, String rol) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        // El setFrom debe coincidir con el correo configurado en application.properties
        mensaje.setFrom("p45910458@gmail.com"); 

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
        
        // Ejecutar el envío de correo eléctrico
        mailSender.send(mensaje);
    }
}
