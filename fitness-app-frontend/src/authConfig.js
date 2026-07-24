export const authConfig = {
    clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'oauth2-pkce-client',
    authorizationEndpoint: import.meta.env.VITE_KEYCLOAK_AUTH_URL || 'http://localhost:8181/realms/fitness-app/protocol/openid-connect/auth',
    tokenEndpoint: import.meta.env.VITE_KEYCLOAK_TOKEN_URL || 'http://localhost:8181/realms/fitness-app/protocol/openid-connect/token',
    redirectUri: import.meta.env.VITE_AUTH_REDIRECT_URI || window.location.origin,
    scope: 'openid profile email offline_access',
    onRefreshTokenExpire: (event) => event.logIn(),
  }
