package dk.scheduling.schedulingfrontend.exceptions

import okhttp3.Response

class UnsuccessfulRequestException(message: String, val response: Response) : Throwable(message = message)
