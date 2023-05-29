package eu.europa.ec.eudi.verifier.endpoint.port.`in`

import eu.europa.ec.eudi.verifier.endpoint.domain.*
import eu.europa.ec.eudi.verifier.endpoint.port.`in`.QueryResponse.*
import eu.europa.ec.eudi.verifier.endpoint.port.out.jose.SignRequestObject
import eu.europa.ec.eudi.verifier.endpoint.port.out.persistence.LoadPresentationByRequestId
import eu.europa.ec.eudi.verifier.endpoint.port.out.persistence.StorePresentation
import java.time.Clock

/**
 * Given a [RequestId] it returns a RFC9101 Request Object
 * encoded as JWT, if the [Presentation] is in state [Presentation.Requested].
 * In this case, the [Presentation] is updated to [Presentation.RequestObjectRetrieved]
 * in order to guarantee that only once the Request Object can be retrieved by
 * the wallet
 */
fun interface GetRequestObject {
    suspend operator fun invoke(requestId: RequestId): QueryResponse<Jwt>
}

class GetRequestObjectLive(
    private val loadPresentationByRequestId: LoadPresentationByRequestId,
    private val storePresentation: StorePresentation,
    private val signRequestObject: SignRequestObject,
    private val verifierConfig: VerifierConfig,
    private val clock: Clock
) : GetRequestObject {

    override suspend operator fun invoke(requestId: RequestId): QueryResponse<Jwt> =
        when (val presentation = loadPresentationByRequestId(requestId)) {
            null -> NotFound
            is Presentation.Requested -> Found(requestObjectOf(presentation))
            else -> InvalidState
        }

    private suspend fun requestObjectOf(presentation: Presentation.Requested): Jwt {
        val jwt = signRequestObject(verifierConfig, clock, presentation).getOrThrow()
        val updatedPresentation = presentation.retrieveRequestObject(clock).getOrThrow()
        storePresentation(updatedPresentation)
        return jwt
    }
}


