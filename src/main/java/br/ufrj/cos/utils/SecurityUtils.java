package br.ufrj.cos.utils;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public final class SecurityUtils {
    private SecurityUtils() {
        // Utility class should not be instantiated
    }

    /**
     * Checks if the user is logged in.
     *
     * @return true if the user is logged in
     */
    public static boolean isUserLoggedIn() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) return false;

        Authentication authentication = context.getAuthentication();
        return authentication != null
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.isAuthenticated();
    }

    /**
     * Gets the current authenticated user.
     *
     * @return the UserDetails of the current user, or null if not authenticated
     */
    public static UserDetails getAuthenticatedUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null || context.getAuthentication() == null) {
            return null;
        }

        Object principal = context.getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return (UserDetails) principal;
        }
        return null;
    }

    /**
     * Gets the username of the current authenticated user.
     *
     * @return the username of the current user, or null if not authenticated
     */
    public static String getUsername() {
        UserDetails user = getAuthenticatedUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * Checks if the current user has a specific authority/role.
     *
     * @param role the role to check
     * @return true if the user has the role
     */
    public static boolean hasRole(String role) {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) return false;

        Authentication authentication = context.getAuthentication();
        return authentication != null &&
                authentication.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Gets all authorities/roles of the current user.
     *
     * @return Collection of GrantedAuthority, empty if not authenticated
     */
    public static Collection<? extends GrantedAuthority> getAuthorities() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) return Collections.emptyList();

        Authentication authentication = context.getAuthentication();
        return authentication != null ?
                authentication.getAuthorities() : Collections.emptyList();
    }
}