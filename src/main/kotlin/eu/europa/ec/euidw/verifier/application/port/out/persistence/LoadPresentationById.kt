package eu.europa.ec.euidw.verifier.application.port.out.persistence

import eu.europa.ec.euidw.verifier.domain.Presentation
import eu.europa.ec.euidw.verifier.domain.PresentationId

/**
 * Loads a [Presentation] from a storage
 */
fun interface LoadPresentationById {
    suspend operator fun invoke(presentationProcessById: PresentationId): Presentation?
}