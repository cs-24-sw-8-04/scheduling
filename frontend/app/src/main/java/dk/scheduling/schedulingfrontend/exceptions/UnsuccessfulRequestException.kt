package dk.scheduling.schedulingfrontend.exceptions

import okhttp3.Response

class UnsuccessfulRequestException(s: String, response: Response) : Throwable(message = s)
