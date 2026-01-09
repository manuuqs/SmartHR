export function decodeJwt(token) {
    try {
        const payload = token.split(".")[1];
        const decoded = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
        return JSON.parse(decoded);
    } catch (e) {
        console.error("Token inválido", e);
        return null;
    }
}
