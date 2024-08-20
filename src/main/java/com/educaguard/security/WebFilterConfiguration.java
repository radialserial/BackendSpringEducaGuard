package com.educaguard.security;

import com.educaguard.domain.enums.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class WebFilterConfiguration {

	@Autowired
	private InterceptorFilter interceptorFilter;

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.addAllowedOrigin("*"); // Permite todas as origens
		configuration.addAllowedMethod("*"); // Permite todos os métodos HTTP
		configuration.addAllowedHeader("*"); // Permite todos os cabeçalhos

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.cors().configurationSource(corsConfigurationSource()); // Aplica a configuração CORS

		http.csrf(AbstractHttpConfigurer::disable); // Desativa CSRF
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // Sem sessões

		http.authorizeHttpRequests(auth -> auth
				// Permissões para outros endpoints
				.requestMatchers(HttpMethod.POST, "/login/*").permitAll()
				.requestMatchers(HttpMethod.POST, "/login/*/recfacial").permitAll()
				.requestMatchers(HttpMethod.GET, "/email/confirmation/*").permitAll()
				.requestMatchers(HttpMethod.GET, "/recover/recover-account/*").permitAll()
				.requestMatchers(HttpMethod.POST, "/recover/new-password").permitAll()

				.requestMatchers(HttpMethod.POST, "/user/new").hasAnyAuthority(
						Roles.ROLE_ADMIN.name(),
						Roles.ROLE_ESTUDANTE.name(),
						Roles.ROLE_OPERADOR_MONITORAMENTO.name(),
						Roles.ROLE_PROFESSOR.name()
				)
				.requestMatchers(HttpMethod.GET, "/user/find/*")
				.hasAnyAuthority(
						Roles.ROLE_ADMIN.name(),
						Roles.ROLE_ESTUDANTE.name(),
						Roles.ROLE_OPERADOR_MONITORAMENTO.name(),
						Roles.ROLE_PROFESSOR.name()
				)

				.requestMatchers(HttpMethod.POST, "/user/new").permitAll()
				.requestMatchers(HttpMethod.POST, "user/*/uploadImage").hasAnyAuthority(
						Roles.ROLE_ADMIN.name(),
						Roles.ROLE_OPERADOR_MONITORAMENTO.name()
				)
				.anyRequest().authenticated());

		http.addFilterBefore(this.interceptorFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
