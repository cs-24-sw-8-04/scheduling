package dk.scheduling.schedulingfrontend.exceptions

import okhttp3.Response

class DeletionFailedException(message: String, val response: Response) : Throwable(message)
