package dk.scheduling.schedulingfrontend.exceptions

class UserNotLoggedInException(message: String = "User is not logged in", val exception: Throwable? = null) : Exception(message)
