package co.edu.uniquindio.entorno.domain.model

/**
 * Errores de autenticación del dominio: la UI no necesita conocer las excepciones de Firebase.
 */

class InvalidCredentialsException : Exception("Correo o contraseña incorrectos")
class EmailAlreadyInUseException : Exception("Ya existe una cuenta con este correo")
class WeakPasswordException : Exception("La contraseña es demasiado débil")
