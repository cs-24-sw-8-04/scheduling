package dk.scheduling.schedulingfrontend.exceptions

import okhttp3.Response

class CreationFailedException(message: String, val response: Response) : Throwable(message)
