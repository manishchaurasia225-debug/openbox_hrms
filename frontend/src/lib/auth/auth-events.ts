/**
 * Bridges the non-React axios layer to React navigation. When a request fails
 * auth unrecoverably (refresh failed / no refresh token), the API client calls
 * emitUnauthorized(); a top-level component registers a handler that clears
 * state and routes to /login without a full page reload.
 */
type UnauthorizedHandler = () => void

let handler: UnauthorizedHandler | null = null

export function setUnauthorizedHandler(fn: UnauthorizedHandler | null): void {
  handler = fn
}

export function emitUnauthorized(): void {
  if (handler) {
    handler()
  } else {
    // Fallback before the app registers a handler: hard redirect.
    window.location.assign('/login')
  }
}
