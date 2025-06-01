package com.example.masterprojectandroid.fhir.upload

import ca.uhn.fhir.rest.client.api.IGenericClient
import com.example.masterprojectandroid.fhir.interfaces.IFHIRUploader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hl7.fhir.r4.model.*

/**
 * Responsible for uploading FHIR Observation resources to a remote FHIR server.
 */
class FHIRUploader(private val fhirClient: IGenericClient) : IFHIRUploader {

    /**
     * Uploads a list of Observation resources to the FHIR server using a transaction bundles.
     */
        override suspend fun uploadObservations(observations: List<Observation>) {
            withContext(Dispatchers.IO) {
                val bundle = Bundle().apply {
                    type = Bundle.BundleType.TRANSACTION
                    observations.forEach { obs ->
                        addEntry(Bundle.BundleEntryComponent().apply {
                            resource = obs
                            request = Bundle.BundleEntryRequestComponent().apply {
                                method = Bundle.HTTPVerb.POST
                                url = "Observation"
                            }
                        })
                    }
                }
                fhirClient.transaction().withBundle(bundle).execute()
            }
        }

}
