package dk.scheduling.schedulingfrontend.exceptions

class UnauthorizedException(message: String, val authToken: String) : Throwable(message)
