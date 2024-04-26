package dk.scheduling.schedulingfrontend.exceptions

import okhttp3.Response

class CreationFailedException(s: String, response: Response) : Throwable() {

}
