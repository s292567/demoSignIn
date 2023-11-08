package com.example.demo

import org.apache.xml.security.stax.ext.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class testController {
    @GetMapping("/test")
    fun test():String{
        var loggedInfo= SecurityContextHolder.getContext().authentication;
        println(loggedInfo.name)
        return "It works!!!";
    }
}