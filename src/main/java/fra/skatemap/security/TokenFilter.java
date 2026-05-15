package fra.skatemap.security;

import fra.skatemap.entities.User;
import fra.skatemap.exceptions.NotAuthorized;
import fra.skatemap.services.UsersService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TokenFilter extends OncePerRequestFilter {
    private final TokenTools tokenTools;
    private final UsersService usersService;

    public TokenFilter(TokenTools tokenTools, UsersService usersService) {
        this.tokenTools = tokenTools;
        this.usersService = usersService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "token is not correct");
            return;
        }
        String accessToken = authHeader.replace("Bearer ", "");
        try {
            tokenTools.verifyToken(accessToken);
           User authenticatedUser;
            UUID userId = this.tokenTools.extractIdFromToken(accessToken);
            authenticatedUser = this.usersService.findById(userId);
            System.out.println(">>> USER: " + authenticatedUser.getUsername());
            System.out.println(">>> AUTHORITIES: " + authenticatedUser.getAuthorities());
            Authentication authentication = new UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            authenticatedUser = this.usersService.findById(userId);
           Authentication auth = new UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (NotAuthorized ex) {
            System.out.println(ex.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return new AntPathMatcher().match("/auth/**", request.getServletPath());
    }

}
