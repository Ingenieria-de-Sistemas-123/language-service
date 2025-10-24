package com.snippetsearcher.language.config;

public class SecurityConfig {
    //SecurityConfig tiene que requerir autenticacion en todos los endpoints excepto en health
    //Extrae el email del Jwt cuando sea necesario (por ahora no almacenamos snippets aca, pero si vamos a validar permisos en otros servicios).
}
