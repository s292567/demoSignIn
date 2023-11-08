package com.example.demo

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider.ResponseToken;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer.withDefaults


@Configuration
class SecurityConfiguration {
    @Bean
    @Throws(Exception::class)
    fun configure(http: HttpSecurity): SecurityFilterChain {
        val authenticationProvider = OpenSaml4AuthenticationProvider()
        authenticationProvider.setResponseAuthenticationConverter(groupsConverter())
        http.authorizeHttpRequests{
            it
                .anyRequest().authenticated()
        }
            .saml2Login { it
                    .authenticationManager(ProviderManager(authenticationProvider))
            }
            .saml2Logout(withDefaults())
        return http.build()
    }

    private fun groupsConverter(): Converter<ResponseToken, Saml2Authentication> {
        val delegate: Converter<ResponseToken, Saml2Authentication> =
            OpenSaml4AuthenticationProvider.createDefaultResponseAuthenticationConverter()
        return Converter<ResponseToken, Saml2Authentication> { responseToken ->
            val authentication: Saml2Authentication? = delegate.convert(responseToken)
            val principal = authentication!!.principal as Saml2AuthenticatedPrincipal
            val groups = principal.getAttribute<String>("groups")
            val authorities: MutableSet<GrantedAuthority> = HashSet()
            groups?.stream()?.map { role: String? ->
                SimpleGrantedAuthority(
                    role
                )
            }?.forEach { e: SimpleGrantedAuthority -> authorities.add(e) }
                ?: authorities.addAll(authentication.authorities)
            Saml2Authentication(principal, authentication.saml2Response, authorities)
        }
    }
}