package com.snippetsearcher.language.controller;

public class ParserController {
    //POST /parser/version → cambia la versión del parser. Debe publicar un evento ParserVersionChanged en el bus de mensajes (ej. RabbitMQ).
    //Aplica @Valid para validar la entrada. Devuelve DTOs claros {valid:true/false, errors:[...]} o {stdout:[...]}.
}
